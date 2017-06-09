package net.sf.jrtps.webdds;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/dds")
public class WebDDSRestHandler {

	@GET
	@Path("/{param}")
	public List<String> getApplications(@PathParam("param") String msg) {
		List<String> l = new LinkedList<>();
		l.add(msg);
		return l;
	}

	@DELETE
	@Path("/applications")
	public void deleteApplication(String appName) {
		throw new RuntimeException("Not supported");
	}

	@POST
	@Path("/applications")
	public void createApplication(String application) {
		throw new RuntimeException("Not supported");
	}


	@GET
	@Path("/types")
	public List<String> getTypes() {
		throw new RuntimeException("Not supported");
	}
}
