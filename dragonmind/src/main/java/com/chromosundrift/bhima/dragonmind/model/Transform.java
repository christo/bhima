package com.chromosundrift.bhima.dragonmind.model;

import java.util.Map;

@SuppressWarnings("WeakerAccess")
public final class Transform {
    private String type;
    private Map<String, Float> parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Float> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Float> parameters) {
        this.parameters = parameters;
    }
}
