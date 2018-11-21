package com.chromosundrift.bhima.dragonmind;

public class NearDeathExperience extends RuntimeException {

    public NearDeathExperience() {
    }

    public NearDeathExperience(String message) {
        super(message);
    }

    public NearDeathExperience(String message, Throwable cause) {
        super(message, cause);
    }

    public NearDeathExperience(Throwable cause) {
        super(cause);
    }

    public NearDeathExperience(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
