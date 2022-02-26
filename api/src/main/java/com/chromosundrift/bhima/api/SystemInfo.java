package com.chromosundrift.bhima.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents rich system status information.
 */
@JsonInclude(NON_EMPTY)
public class SystemInfo {

    public Long uptimeSeconds;
    public List<LedController> ledControllers;
    public ProgramInfo currentProgram;
    public Settings settings;
    public List<ProgramType> programTypes;
    public Map<String, Set<Integer>> effectiveWiring;
    public String scrollText;

    public SystemInfo() {
        this(0L, ProgramInfo.getNullProgramInfo());
    }

    public SystemInfo(Long uptimeSeconds, ProgramInfo currentProgram) {
        this.uptimeSeconds = uptimeSeconds;
        this.ledControllers = Collections.emptyList();
        this.currentProgram = currentProgram;
    }

    public Long getUptimeSeconds() {
        return uptimeSeconds;
    }

    public void setUptimeSeconds(Long uptimeSeconds) {
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

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    public List<ProgramType> getProgramTypes() {
        return programTypes;
    }

    public void setProgramTypes(List<ProgramType> programTypes) {
        this.programTypes = programTypes;
    }

    public Map<String, Set<Integer>> getEffectiveWiring() {
        return effectiveWiring;
    }

    public void setEffectiveWiring(Map<String, Set<Integer>> effectiveWiring) {
        this.effectiveWiring = effectiveWiring;
    }

    public String getScrollText() {
        return scrollText;
    }

    public void setScrollText(String scrollText) {
        this.scrollText = scrollText;
    }
}
