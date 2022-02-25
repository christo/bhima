package com.chromosundrift.bhima.api;

/**
 * Derived from settings for pixelpushers configured through their DeviceRegistry via our PusherMan
 * interface.
 */
public final class Settings {
    private double brightness;
    private boolean luminanceCorrection;
    private boolean autoThrottle;

    public Settings(double brightness, boolean luminanceCorrection, boolean autoThrottle) {
        this.brightness = brightness;
        this.luminanceCorrection = luminanceCorrection;
        this.autoThrottle = autoThrottle;
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    public boolean isLuminanceCorrection() {
        return luminanceCorrection;
    }

    public void setLuminanceCorrection(boolean luminanceCorrection) {
        this.luminanceCorrection = luminanceCorrection;
    }

    public boolean isAutoThrottle() {
        return autoThrottle;
    }

    public void setAutoThrottle(boolean autoThrottle) {
        this.autoThrottle = autoThrottle;
    }
}
