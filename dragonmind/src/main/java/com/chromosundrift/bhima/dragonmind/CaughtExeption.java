package com.chromosundrift.bhima.dragonmind;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

public final class CaughtExeption {
    private final Instant timeOccurred;
    private final String message;
    private String stackTrace;

    public CaughtExeption(Throwable t) {
        this.timeOccurred = Instant.now();
        this.message = t.getMessage();
        // if PPs have been turned off randomly we will end up here, keep the error message and stack trace
        t.getMessage();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        try {
            pw.close();
            this.stackTrace = sw.toString();
        } catch (Exception ignored) {

        }

    }

    @Override
    public String toString() {
        return "Caught Exception at " + timeOccurred.toString() + " : " + message + " " + stackTrace;

    }

    public Instant getTimeOccurred() {
        return timeOccurred;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
