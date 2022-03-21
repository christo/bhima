package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * Derived from settings for pixelpushers configured through their DeviceRegistry via our PusherMan
 * interface.
 */
@JsonInclude(NON_EMPTY)
public final class Settings {
    private Double brightness;
    private Boolean luminanceCorrection;
    private Boolean autoThrottle;
    private Boolean mute;
    private Boolean liveVideo = true;

    public Settings() {
    }

    public Settings(Double brightness, Boolean luminanceCorrection, Boolean autoThrottle, Boolean mute) {
        this.brightness = brightness;
        this.luminanceCorrection = luminanceCorrection;
        this.autoThrottle = autoThrottle;
        this.mute = mute;
    }

    public Double getBrightness() {
        return brightness;
    }

    public void setBrightness(Double brightness) {
        this.brightness = brightness;
    }

    public Boolean isLuminanceCorrection() {
        return luminanceCorrection;
    }

    public void setLuminanceCorrection(Boolean luminanceCorrection) {
        this.luminanceCorrection = luminanceCorrection;
    }

    public Boolean isAutoThrottle() {
        return autoThrottle;
    }

    public void setAutoThrottle(Boolean autoThrottle) {
        this.autoThrottle = autoThrottle;
    }

    public Boolean isMute() {
        return mute;
    }

    public void setMute(Boolean mute) {
        this.mute = mute;
    }

    public Boolean isLiveVideo() {
        return liveVideo;
    }

    public void setLiveVideo(Boolean liveVideo) {
        this.liveVideo = liveVideo;
    }

    @JsonIgnore
    public Boolean isValid() {
        return brightness >= 0d && brightness <= 100d;
    }
}
