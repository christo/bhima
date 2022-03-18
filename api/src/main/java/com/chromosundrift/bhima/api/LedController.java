package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Map;

/**
 * Model for representing LED Controllers like PixelPusher.
 */
public class LedController {
    public String species;
    public String address;
    public String name;
    public Integer capacity;
    public Boolean online;
    public Map<String, String> stats;

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public Map<String, String> getStats() {
        return stats;
    }

    public void setStats(Map<String, String> stats) {
        this.stats = stats;
    }
}
