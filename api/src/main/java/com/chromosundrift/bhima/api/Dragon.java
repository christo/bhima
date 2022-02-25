package com.chromosundrift.bhima.api;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Wiring;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Root domain model for Bhima.
 */
public interface Dragon {

    String getStatus();

    SystemInfo getSystemInfo();

    Config getConfig();

    Wiring getWiring();

    ProgramInfo getCurrentProgram();

    List<ProgramInfo> getPrograms();

    ProgramInfo runProgram(String id);

    Map<String, Set<Integer>> getEffectiveWiring();

    Settings getSettings();

    Settings setSettings(Settings settings);
}
