package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.BuiltinEndpointSet_t;

/**
 * This Parameter identifies the kinds of built-in SEDP endpoints that are available in the Participant.
 * 
 * @author mika.riekkinen
 *
 */
public class EndpointSet extends Parameter {
	private int endpointSet;
	
	protected EndpointSet(ParameterEnum e, int endpoints) {
		super(e);
		
		endpointSet = endpoints;
	}
	
	public boolean hasParticipantAnnouncer() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER) != 0;
	}
	
	public boolean hasParticipantDetector() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR) != 0;
	}

	public boolean hasPublicationAnnouncer() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER) != 0;
	}
	
	public boolean hasPublicationDetector() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR) != 0;
	}

	public boolean hasSubscriptionAnnouncer() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER) != 0;
	}
	
	public boolean hasSubscriptionDetector() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR) != 0;
	}

	public boolean hasParticipantProxyAnnouncer() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_ANNOUNCER) != 0;
	}
	
	public boolean hasParticipantProxyDetector() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_PROXY_DETECTOR) != 0;
	}

	public boolean hasParticipantStateAnnouncer() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_ANNOUNCER) != 0;
	}
	
	public boolean hasParticipantStateDetector() {
		return (endpointSet &  BuiltinEndpointSet_t.DISC_BUILTIN_ENDPOINT_PARTICIPANT_STATE_DETECTOR) != 0;
	}
	
	public boolean hasParticipantMessageDataReader() {
		return (endpointSet &  BuiltinEndpointSet_t.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_READER) != 0;
	}
	
	public boolean hasParticipantMessageDataWriter() {
		return (endpointSet &  BuiltinEndpointSet_t.BUILTIN_ENDPOINT_PARTICIPANT_MESSAGE_DATA_WRITER) != 0;
	}

	/**
	 * Get the bitmap of the supported endpoints contained in participant.
	 * 
	 * @return bitmap of the supported endpoints
	 */
	public int getEndpointSet() {
		return endpointSet;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		endpointSet = bb.read_long();
	}

	@Override 
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(endpointSet);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(": ");
		sb.append(String.format("0x%04x", endpointSet));
		sb.append(": [ ");
		if (hasParticipantAnnouncer()) {
			sb.append("ParticipantAnnouncer ");
		}
		if (hasParticipantDetector()) {
			sb.append("ParticipantDetector ");
		}
		if (hasPublicationAnnouncer()) {
			sb.append("PublicationsAnnouncer ");
		}
		if (hasPublicationDetector()) {
			sb.append("PublicationsDetector ");
		}
		if (hasSubscriptionAnnouncer()) {
			sb.append("SubscriptionsAnnouncer ");
		}
		if (hasSubscriptionDetector()) {
			sb.append("SubscriptionsDetector ");
		}
		if (hasParticipantProxyAnnouncer()) {
			sb.append("ParticipantProxyAnnouncer ");
		}
		if (hasParticipantProxyDetector()) {
			sb.append("ParticipantProxyDetector ");
		}
		if (hasParticipantStateAnnouncer()) {
			sb.append("ParticipantStateAnnouncer ");
		}
		if (hasParticipantStateDetector()) {
			sb.append("ParticipantStateDetector ");
		}
		if (hasParticipantMessageDataReader()) {
			sb.append("ParticipantMessageDataReader ");
		}
		if (hasParticipantMessageDataWriter()) {
			sb.append("ParticipantMessageDataWriter ");
		}
		sb.append("]");
		
		return sb.toString();
	}

}
