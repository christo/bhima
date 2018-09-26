package com.chromosundrift.bhima.dragonmind.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

import static com.chromosundrift.bhima.dragonmind.model.Transform.Type.*;

@SuppressWarnings("WeakerAccess")
public final class Transform {

    // TODO this whole thing needs to be rethought
    public enum Type {

        TRANSLATE("translate"),
        SCALE("scale"),
        ROTATE("rotate");

        private final String name;

        Type(String name) {
            this.name = name;
        }
    }

    public static Transform rotate(float theta) {
        Map<String, Float> params = new HashMap<>();
        params.put("z", theta);
        return new Transform(ROTATE, params);
    }

    public static Transform translate(float x, float y) {
        return new Transform(TRANSLATE, xAndY(x, y));
    }

    public static Transform scale(float s) {
        return new Transform(SCALE, xAndY(s, s));
    }

    public static Transform scale(float x, float y) {
        return new Transform(SCALE, xAndY(x, y));
    }

    private static Map<String, Float> xAndY(float x, float y) {
        Map<String, Float> params = new HashMap<>();
        params.put("x", x);
        params.put("y", y);
        return params;
    }

    public Transform() {
    }

    public Transform(Type type, Map<String, Float> parameters) {
        this.type = type.name;
        this.parameters = parameters;
    }

    public Transform(Transform t) {
        this.type = t.type;
        this.parameters = new HashMap<>();
        for (Map.Entry<String, Float> kv : t.parameters.entrySet()) {
            this.parameters.put(kv.getKey(), kv.getValue());
        }
    }

    private String type;

    private Map<String, Float> parameters;

    public boolean is(Type type) {
        return this.type.equals(type.name);
    }

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

    @Override
    public String toString() {
        return "Transform{" +
                "type='" + type + '\'' +
                ", parameters=" + parameters +
                '}';
    }

    @JsonIgnore
    public Float get(String key) {
        return parameters.get(key);
    }

    @JsonIgnore
    public void set(String key, Float value) {
        parameters.put(key, value);
    }

}
