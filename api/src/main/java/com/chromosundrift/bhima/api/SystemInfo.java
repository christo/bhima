package com.chromosundrift.bhima.api;

import java.util.Collections;
import java.util.List;

/**
 * Represents rich system status information.
 */
public class SystemInfo {

    public Integer uptimeSeconds;
    public List<LedController> ledControllers;
    public ProgramInfo currentProgram;

    public SystemInfo(Integer uptimeSeconds, ProgramInfo currentProgram) {
        this.uptimeSeconds = uptimeSeconds;
        this.ledControllers = Collections.emptyList();
        this.currentProgram = currentProgram;
    }

    public Integer getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Integer uptimeSeconds) {
        this.uptimeSeconds = uptimeSeconds;
    }

    public List<LedController> getLedControllers() {
        return ledControllers;
    }

    public void setLedControllers(List<LedController> ledControllers) {
        this.ledControllers = ledControllers;
    }

    public ProgramInfo getCurrentProgram() {
        return currentProgram;
    }

    public void setCurrentProgram(ProgramInfo currentProgram) {
        this.currentProgram = currentProgram;
    }
}
