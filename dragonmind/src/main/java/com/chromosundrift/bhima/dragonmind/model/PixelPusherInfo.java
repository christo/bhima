package com.chromosundrift.bhima.dragonmind.model;

public class PixelPusherInfo {
    /**
     * Unique identifier as spewed from the device itself.
     */
    private String macAddress;
    /**
     * Name in this model.
     */
    private String name;
    /**
     * Purpose or notes.
     */
    private String description;

    public PixelPusherInfo(String macAddress, String name, String description) {
        this.macAddress = macAddress;
        this.name = name;
        this.description = description;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
