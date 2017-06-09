package net.sf.jrtps.rpc;

/**
 * This class represents endpoint configuration. It uses extracts DDSService
 * annotation from service class, or uses default values.
 */
class EndpointConfiguration {
    private String requestTopic;
    private String replyTopic;
    private String serviceName;
    private String instanceName;

    EndpointConfiguration(Class<?> sClass) {
	RPCConfiguration[] declaredAnnotationsByType = sClass.getDeclaredAnnotationsByType(RPCConfiguration.class);
	if (declaredAnnotationsByType != null && declaredAnnotationsByType.length > 0) {
	    RPCConfiguration ddss = declaredAnnotationsByType[0];
	    requestTopic = ddss.requestTopic();
	    replyTopic = ddss.replyTopic();
	    serviceName = ddss.serviceName();
	    instanceName = ddss.instanceName();
	}

	if (requestTopic == null || "".equals(requestTopic)) { 
	    requestTopic = sClass.getSimpleName() + "_Service_Request";
	}
	if (replyTopic == null || "".equals(replyTopic)) {
	    replyTopic = sClass.getSimpleName() + "_Service_Reply";
	}
	if (serviceName == null) {
	    serviceName = "";
	}
	if (instanceName == null) {
	    instanceName = "";
	}
	
	if (requestTopic.equals(replyTopic)) {
	    throw new IllegalArgumentException("requestTopic cannot be the same as replyTopic");
	}
    }

    String getRequestTopic() {
	return requestTopic;
    }

    String getReplyTopic() {
	return replyTopic;
    }

    String getServiceName() {
	return serviceName;
    }

    String getInstanceName() {
	return instanceName;
    }
}
