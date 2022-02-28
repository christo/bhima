package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Derived from settings for pixelpushers configured through their DeviceRegistry via our PusherMan
 * interface.
 */
@JsonInclude(NON_EMPTY)
public final class Settings {
    private double brightness;
    private boolean luminanceCorrection;
    private boolean autoThrottle;
    private boolean mute;

    public Settings(double brightness, boolean luminanceCorrection, boolean autoThrottle, boolean mute) {
        this.brightness = brightness;
        this.luminanceCorrection = luminanceCorrection;
        this.autoThrottle = autoThrottle;
        this.mute = mute;
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

    public boolean isMute() {
        return mute;
    }

    public void setMute(boolean mute) {
        this.mute = mute;
    }
}
