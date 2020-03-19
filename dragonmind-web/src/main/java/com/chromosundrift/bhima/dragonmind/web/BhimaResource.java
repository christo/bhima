package com.chromosundrift.bhima.dragonmind.web;

import com.chromosundrift.bhima.api.Dragon;
import com.chromosundrift.bhima.api.ProgramInfo;
import com.chromosundrift.bhima.api.SystemInfo;
import com.chromosundrift.bhima.dragonmind.model.Wiring;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
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

    private Dragon dragon;

    public BhimaResource(Dragon dragon) {
        this.dragon = dragon;
    }

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

    @GET
    @Timed
    @Path("/systemInfo")
    public SystemInfo getSystemInfo() {
        return dragon.getSystemInfo();
    }
}
