package net.sf.jrtps.webdds;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

public class WebDDSRestHandler {
    
    @DELETE
    @Path("/applications")
    void deleteApplication(String appName) {
	throw new RuntimeException("Not supported");
    }

    @POST
    @Path("/applications")
    void createApplication(String application) {
	throw new RuntimeException("Not supported");
    }

    @GET
    @Path("/applications")
    void getApplications() {
	throw new RuntimeException("Not supported");	
    }
    
    @GET
    @Path("/types")
    void getTypes() {
	throw new RuntimeException("Not supported");
    }
}
