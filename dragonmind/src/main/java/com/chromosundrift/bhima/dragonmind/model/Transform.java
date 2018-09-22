package com.chromosundrift.bhima.dragonmind.model;

public class Transform {
    private String type;
    private float[] parameters; // TODO doesn't match warp or rotate

    public Transform(String type, float[] parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float[] getParameters() {
        return parameters;
    }

    public void setParameters(float[] parameters) {
        this.parameters = parameters;
    }
}
