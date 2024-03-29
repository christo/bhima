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
public class SystemInfo {

    public Long uptimeSeconds;
    public Double loadAverage;
    public List<LedController> ledControllers;
    public ProgramInfo currentProgram;
    public Settings settings;
    public List<ProgramType> programTypes;
    public Map<String, Set<Integer>> effectiveWiring;
    public String name;
    public String scrollText;
    public String version;
    public String status;
    public String configProject;
    public String configVersion;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfigProject() {
        return configProject;
    }

    public void setConfigProject(String configProject) {
        this.configProject = configProject;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public void setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
    }

    public Double getLoadAverage() {
        return loadAverage;
    }

    public void setLoadAverage(Double loadAverage) {
        this.loadAverage = loadAverage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
