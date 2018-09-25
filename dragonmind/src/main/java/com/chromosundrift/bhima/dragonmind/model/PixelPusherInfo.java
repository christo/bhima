package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@SuppressWarnings("WeakerAccess")
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class PixelPusherInfo {

    // in a perfect world this generates pixel.rc / configures the pp via eeprom

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

    private Integer ordinal;

    /**
     * The list of which ports should be natively copied to which other. For the purpose of documentation and
     * simulation only, this configuration should go in the pixel.rc
     */
    private String copyList;

    public PixelPusherInfo() {
    }

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

    public Integer getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(Integer ordinal) {
        this.ordinal = ordinal;
    }

    public String getCopyList() {
        return copyList;
    }

    public void setCopyList(String copyList) {
        this.copyList = copyList;
    }
}
