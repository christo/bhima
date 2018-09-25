package com.chromosundrift.bhima.dragonmind;


import com.heroicrobot.dropbit.devices.pixelpusher.PixelPusher;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import com.heroicrobot.dropbit.registry.DeviceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class for encapsulating common configuration and operations for Bhima's PusherMan
 */
@SuppressWarnings("WeakerAccess")
public class PusherMan implements Observer {

    final static Logger logger = LoggerFactory.getLogger(PusherMan.class);

    private DeviceRegistry registry;

    /**
     * Possibly need this to prevent blocking inside the PixelPusher Processing library.
     */
    private final ReentrantLock registryLock = new ReentrantLock();

    private final AtomicReference<DeviceRegistry> observedRegistry = new AtomicReference<>();

    private final AtomicBoolean initialised = new AtomicBoolean(false);

    private final AtomicBoolean pendingUpdates = new AtomicBoolean(false);

    private final AtomicBoolean started = new AtomicBoolean(false);

    public PusherMan(boolean debug) {
        registry = new DeviceRegistry();
        registry.setLogging(debug);
    }

    private void withLock(Runnable runme) {
        try {
            registryLock.lock();
            runme.run();
        } finally {
            registryLock.unlock();
        }
    }

    public void init() {
        if (!initialised.get()) {
            withLock(() -> {
                registry.addObserver(PusherMan.this);
                initialised.set(true);
            });
        }
    }

    /**
     * Note this is executed by PixelPusher's thread.
     *
     * @param observable
     * @param updatedDevice
     */
    @Override
    public void update(Observable observable, Object updatedDevice) {
        this.pendingUpdates.set(true);

        logger.debug("Registry changed!");
        if (updatedDevice != null) {
            if (updatedDevice instanceof PixelPusher) {
                logger.debug("Device change: " + updatedDevice);
            }
        }

        if (observable instanceof DeviceRegistry) {
            observedRegistry.set((DeviceRegistry) observable);

        }

    }

    private void reportPixelPusherShiz() {
        if (pendingUpdates.get()) {
            pendingUpdates.set(false);
            Map<String, PixelPusher> pusherMap = observedRegistry.get().getPusherMap();
            for (Map.Entry<String, PixelPusher> entry : pusherMap.entrySet()) {
                PixelPusher pp = entry.getValue();

                String mac = entry.getKey();
                logger.info("PP update period microsec: " + mac + " " + pp.getUpdatePeriod());
            }
        }
    }

    public String report() {
        reportPixelPusherShiz();
        try {
            registryLock.lock();
            if (observedRegistry.get() != registry) {
                // don't know if the API makes any guarantees about this
                if (observedRegistry.get() != null && registry != null) {
                    logger.warn("observed and direct registries are not the same");
                }
                String observed = report(observedRegistry.get());
                String direct = report(registry);
                return "Observed: " + observed + " Direct: " + direct;
            } else {
                return "Registry: " + report(registry);
            }

        } finally {
            registryLock.unlock();
        }

    }

    // already covered by lock
    private String report(DeviceRegistry registry) {
        if (registry == null) {
            return "null";
        } else {

            StringBuilder sb = new StringBuilder();
            for (PixelPusher pusher : registry.getPushers()) {
                sb.append(" PP").append(pusher.getControllerOrdinal());
            }
            int result = registry.getStrips().size();

            return numPixelPushersFound(registry) + " Pixel Pushers " + result + " total strips"
                    + sb.toString();
        }
    }

    // already covered by lock
    private int numPixelPushersFound(DeviceRegistry registry) {
        if (registry != null) {
            return registry.getPushers().size();
        } else {
            return -1;
        }
    }

    public void ensureReady() {
        init();
        if (!this.started.get()) {
            withLock(() -> {
                logger.info("setting up registry");
                registry.setAntiLog(true);
                registry.setAutoThrottle(false);
                registry.startPushing();
                this.started.set(true);
            });
        }

    }

    public void turnOffAllPixels() {
        if (!isReady()) {
            logger.warn("Can't turn off pixels, PP not ready");
        } else {
            List<Strip> strips = registry.getStrips();
            for (Strip strip : strips) {
                int len = strip.getLength();
                for (int i = 0; i < len; i++) {
                    strip.setPixelRed((byte) 0, i);
                    strip.setPixelGreen((byte) 0, i);
                    strip.setPixelBlue((byte) 0, i);
                }
            }
        }
    }

    public boolean isReady() {
        return initialised.get() && observedRegistry.get() != null;
    }

    public List<Strip> getStrips() {
        ensureReady();
        try {
            registryLock.lock();
            return registry.getStrips();
        } finally {
            registryLock.unlock();
        }
    }

    public int numStrips() {
        ensureReady();
        try {
            registryLock.lock();
            return registry.getStrips().size();
        } finally {
            registryLock.unlock();
        }

    }

    public int numTotalLights() {
        List<Strip> strips = getStrips();
        int count = 0;
        for (Strip strip : strips) {
            count += strip.getLength();
        }
        return count;
    }

    public void addObserver(Observer observer) {
        withLock(() -> {
            registry.addObserver(observer);
        });
    }

    public int numPixelPushers() {
        try {
            registryLock.lock();
            return registry.getPushers().size();
        } finally {
            registryLock.unlock();
        }
    }

}
