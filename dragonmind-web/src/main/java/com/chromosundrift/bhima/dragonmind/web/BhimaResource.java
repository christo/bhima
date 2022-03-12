package com.chromosundrift.bhima.dragonmind.web;

import com.chromosundrift.bhima.api.Dragon;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.api.Settings;
import com.chromosundrift.bhima.api.SystemInfo;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main REST resource.
 */
@Path("/bhima")
@Produces(MediaType.APPLICATION_JSON)
public class BhimaResource {

    private final Dragon dragon;

    public BhimaResource(Dragon dragon) {
        this.dragon = dragon;
    }

    // TODO create getComposite (?) to return everything except program list to reduce requests

    @GET
    @Timed
    @Path("/status")
    public String getStatus() {
        return dragon.getStatus();
    }

    @GET
    @Timed
    @Path("/effectiveWiring")
    public Map<String, Set<Integer>> getEffectiveWiring() {
        return dragon.getEffectiveWiring();
    }

    @GET
    @Timed
    @Path("/program")
    public ProgramInfo getCurrentProgram() {
        return dragon.getCurrentProgram();
    }

    @GET
    @Timed
    @Path("/programs")
    public List<ProgramInfo> getPrograms() {
        return dragon.getPrograms();
    }

    @POST
    @Timed
    @Path("/runProgram")
    public ProgramInfo runProgram(@FormParam("id") String id) {
        return dragon.runProgram(id);
    }

    @POST
    @Timed
    @Path("/runProgram2")
    // TODO change to @Consume(MediaType.APPLICATION_JSON) with non-@QueryParam argument
    public ProgramInfo runProgram2(@QueryParam("id") String id) {
        return dragon.runProgram(id);
    }

    @GET
    @Timed
    @Path("/systemInfo")
    public SystemInfo getSystemInfo() {
        return dragon.getSystemInfo();
    }

    @GET
    @Timed
    @Path("/settings")
    public Settings getSettings() {
        return dragon.getSettings();
    }

    @POST
    @Timed
    @Path("/settings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Settings setSettings(Settings settings) {
        if (settings.isValid()) {
            return dragon.setSettings(settings);
        } else {
            return dragon.getSettings();
        }
    }
}
