package com.chromosundrift.bhima.dragonmind;


import com.heroicrobot.dropbit.devices.pixelpusher.PixelPusher;
import com.heroicrobot.dropbit.devices.pixelpusher.Strip;
import com.heroicrobot.dropbit.registry.DeviceRegistry;

import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class for encapsulating common configuration and operations for Bhima's PusherMan
 */
public class PusherMan implements Observer {

    private final boolean debug;
    private DeviceRegistry registry;
    private AtomicReference<DeviceRegistry> observedRegistry = new AtomicReference<>();
    private boolean initialised;
    private boolean haveSetUpRegistry;

    public PusherMan(boolean debug) {
        this.debug = debug;
        registry = new DeviceRegistry();
        registry.setLogging(debug);
    }

    public void init() {
        if (!initialised) {
            registry.addObserver(this);
            initialised = true;
        }
    }

    @Override
    public void update(Observable observable, Object updatedDevice) {
        ensureReady();
        if (debug) {
            System.out.println("Registry changed!");
        }
        if (debug && updatedDevice != null) {
            if (updatedDevice instanceof PixelPusher) {
                System.out.println("Device change: " + updatedDevice);
            }
        }
        if (observable instanceof DeviceRegistry) {
            observedRegistry.set((DeviceRegistry) observable);
            Map<String, PixelPusher> pusherMap = registry.getPusherMap();
            for (Map.Entry<String, PixelPusher> entry : pusherMap.entrySet()) {
                PixelPusher pp = entry.getValue();

                String mac = entry.getKey();
                System.out.println("PP update period microsec: " + mac + " " + pp.getUpdatePeriod());
            }
        }
    }

    public String report() {
        return "Observed: " + report(observedRegistry.get())
                + " Direct: " + report(registry);
    }

    private String report(DeviceRegistry registry) {
        return numPixelPushersFound(registry) + " Pixel Pushers with " + numStripsFound(registry) + " strips";
    }

    public int numPixelPushersFound() {
        return numPixelPushersFound(registry);
    }

    public int numStripsFound() {
        return numStripsFound(registry);
    }

    private int numPixelPushersFound(DeviceRegistry registry) {
        if (registry != null)
            return registry.getPushers().size();
        else return -1;
    }

    private int numStripsFound(DeviceRegistry registry) {
        if (registry != null)
            return registry.getStrips().size();
        else return -1;
    }

    public void ensureReady() {
        init();
        if (!isReady()) {
            registry.setAntiLog(true);
            registry.setAutoThrottle(false);
            registry.startPushing();
        }

    }

    public void turnOffAllPixels() {
        if (!isReady()) {
            System.out.println("Can't turn off pixels, PP not ready");
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
        return initialised && observedRegistry.get() != null;
    }

    public List<Strip> getStrips() {
        ensureReady();
        return registry.getStrips();
    }

    public int numStrips() {
        ensureReady();
        return registry.getStrips().size();
    }

}
