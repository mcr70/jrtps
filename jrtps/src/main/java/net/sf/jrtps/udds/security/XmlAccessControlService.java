package net.sf.jrtps.udds.security;

class XmlAccessControlService {
	private ParticipantSecurityAttributes pSecAttrs;
    private EndpointSecurityAttributes eSecAttrs;


    public XmlAccessControlService() {
	}
	
	
	public ParticipantSecurityAttributes getParticipantSecurityAttributes() {
	    return pSecAttrs;
	}

	public EndpointSecurityAttributes getEndpointSecurityAttributes() {
	    return eSecAttrs;
	}
}
