package com.chromosundrift.bhima.api;

import com.chromosundrift.bhima.dragonmind.model.Config;
import com.chromosundrift.bhima.dragonmind.model.Wiring;

import java.util.List;

public interface Dragon {

    public String getStatus();

    public Config getConfig();

    public Wiring getWiring();

    ProgramInfo getCurrentProgram();

    List<ProgramInfo> getPrograms();

    ProgramInfo runProgram(String id);
}
