package net.sf.jrtps.message.parameter;


/**
 * All Participants must support the SEDP. This attribute identifies the kinds of built-in SEDP Endpoints
 * that are available in the Participant. This allows a Participant to indicate that it only
 * contains a subset of the possible built-in Endpoints. See also Section 8.5.4.3.<p>
 * Possible values for BuiltinEndpointSet_t are:<br>
 * PUBLICATIONS_READER, PUBLICATIONS_WRITER, SUBSCRIPTIONS_READER, SUBSCRIPTIONS_WRITER, 
 * TOPIC_READER, TOPIC_WRITER<p>
 * Vendor specific extensions may be used to denote support for additional EDPs.
 * 
 * see 8.5.4.3.
 * 
 * @author mcr70
 * 
 */
public class BuiltinEndpointSet extends EndpointSet {
	public BuiltinEndpointSet(int endpoints) {
		super(ParameterEnum.PID_BUILTIN_ENDPOINT_SET, endpoints);
	}
	
	BuiltinEndpointSet() {
		super(ParameterEnum.PID_BUILTIN_ENDPOINT_SET, 0);
	}
}