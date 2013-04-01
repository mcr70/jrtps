package alt.rtps.builtin;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.message.parameter.DefaultMulticastLocator;
import alt.rtps.message.parameter.DefaultUnicastLocator;
import alt.rtps.message.parameter.EndpointSet;
import alt.rtps.message.parameter.MetatrafficMulticastLocator;
import alt.rtps.message.parameter.MetatrafficUnicastLocator;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.ParticipantGuid;
import alt.rtps.message.parameter.ParticipantLeaseDuration;
import alt.rtps.message.parameter.ParticipantManualLivelinessCount;
import alt.rtps.message.parameter.ProtocolVersion;
import alt.rtps.message.parameter.VendorId;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
import alt.rtps.types.ProtocolVersion_t;
import alt.rtps.types.VendorId_t;

/**
 * 
 * @author mcr70
 * @see 8.5.3.2 SPDPdiscoveredParticipantData
 */
public class ParticipantData {
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ParticipantData.class);
	
	private ProtocolVersion_t protocolVersion = ProtocolVersion_t.PROTOCOLVERSION_2_1;
	private VendorId_t vendorId = VendorId_t.VENDORID_JRTPS;
	private GuidPrefix_t guidPrefix; 	
	private boolean expectsInlineQos = false; 

	/**
	 * List of unicast locators (transport, address, port combinations) that
	 * can be used to send messages to the built-in Endpoints contained in the Participant.
	 */
	private Locator_t metatrafficUnicastLocator;

	/**
	 * List of multicast locators (transport, address, port combinations)
	 * that can be used to send messages to the built-in Endpoints contained in the Participant.
	 */
	private Locator_t metatrafficMulticastLocator;

	/**
	 * Default list of unicast locators (transport, address, port combinations) that can be used 
	 * to send messages to the userdefined Endpoints contained in the Participant.
	 * 
	 * ???? 
	 * These are the unicast locators that will be used in case the Endpoint does not specify its 
	 * own set of Locators, so at least one Locator must be present.
	 * ????
	 * 
	 * (to SPDPbuiltinParticipantWriter? or SPPDPbuiltinParticipantReader? or Participant?)
	 */
	private Locator_t unicastLocator; 

	/**
	 * Default list of multicast locators (transport, address, port combinations) that can be used 
	 * to send messages to the userdefined Endpoints contained in the Participant.
	 * These are the multicast locators that will be used in case the Endpoint does not specify its 
	 * own set of Locators.
	 */
	private Locator_t multicastLocator;

	/**
	 * All Participants must support the SEDP. This attribute identifies the kinds of built-in 
	 * SEDP Endpoints that are available in the Participant. This allows a Participant to indicate 
	 * that it only contains a subset of the possible built-in Endpoints. See also Section 8.5.4.3.<p>
	 * 
	 * Possible values for BuiltinEndpointSet_t are: PUBLICATIONS_READER, PUBLICATIONS_WRITER,
	 * SUBSCRIPTIONS_READER, SUBSCRIPTIONS_WRITER, TOPIC_READER, TOPIC_WRITER<p>
	 * 
	 * Vendor specific extensions may be used to denote support for additional EDPs.
	 */
	private int availableBuiltinEndpoints;

	/**
	 * How long a Participant should be considered alive every time an announcement is received 
	 * from the Participant. If a Participant fails to send another announcement within this
	 * time period, the Participant can be considered gone. In that case, any resources associated 
	 * to the Participant and its Endpoints can be freed.
	 * 
	 * Default value is 100 seconds.
	 */
	private Duration_t leaseDuration = new Duration_t(15, 0); // TODO: 100 sec

	/**
	 * Used to implement MANUAL_BY_PARTICIPANT liveliness QoS.
	 * When liveliness is asserted, the manualLivelinessCount is incremented and 
	 * a new SPDPdiscoveredParticipantData is sent.
	 */
	private int manualLivelinessCount = 0;


	public ParticipantData(GuidPrefix_t prefix, int endpoints,
			Locator_t u_ucLocator, Locator_t u_mcLocator, 
			Locator_t m_ucLocator, Locator_t m_mcLocator) {
		guidPrefix = prefix;
		availableBuiltinEndpoints = endpoints;

		// TODO: So far, we have only one locator in list.		
		if (u_ucLocator != null) { 
			unicastLocator = u_ucLocator;
		}
		if (u_mcLocator != null) {
			multicastLocator = u_mcLocator;
		}
		if (m_ucLocator != null) {
			metatrafficUnicastLocator = m_ucLocator;
		}
		if (m_mcLocator != null) {
			metatrafficMulticastLocator = m_mcLocator;
		}
	}
	
	/**
	 * Reads SPDPdiscoveredParticipantData. It is expected that inputstream is 
	 * positioned to start of serializedData of Data submessage, aligned at 32 bit
	 * boundary.
	 * 
	 * @param is
	 * @throws IOException
	 */
	public ParticipantData(ParameterList parameterList) {
		Iterator<Parameter> iterator = parameterList.getParameters().iterator();
		
		while (iterator.hasNext()) {
			Parameter param = iterator.next();

			log.trace("{}", param);
			switch(param.getParameterId()) {
			case PID_PARTICIPANT_GUID:
				this.guidPrefix = ((ParticipantGuid)param).getParticipantGuid().prefix;
				break;
			case PID_PARTICIPANT_BUILTIN_ENDPOINTS: // Handle PARTICIPANT_BUILTIN_ENDPOINTS (opendds) and
			case PID_BUILTIN_ENDPOINT_SET:          // BUILTIN_ENDPOINT_SET (opensplice) the same way
				availableBuiltinEndpoints = ((EndpointSet)param).getEndpointSet();
				break;
			case PID_PROTOCOL_VERSION:
				protocolVersion = ((ProtocolVersion)param).getProtocolVersion();
				break;
			case PID_VENDORID:
				vendorId = ((VendorId)param).getVendorId();
				break;
			case PID_DEFAULT_UNICAST_LOCATOR:
				unicastLocator = ((DefaultUnicastLocator)param).getLocator();
				break;
			case PID_METATRAFFIC_UNICAST_LOCATOR:
				metatrafficUnicastLocator = ((MetatrafficUnicastLocator)param).getLocator();
				break;
			case PID_DEFAULT_MULTICAST_LOCATOR:
				multicastLocator = ((DefaultMulticastLocator)param).getLocator();
				break;
			case PID_METATRAFFIC_MULTICAST_LOCATOR:
				metatrafficMulticastLocator = ((MetatrafficMulticastLocator)param).getLocator();
				break;
			case PID_PARTICIPANT_LEASE_DURATION:
				this.leaseDuration = ((ParticipantLeaseDuration)param).getDuration();
				break;
			case PID_PARTICIPANT_MANUAL_LIVELINESS_COUNT:
				this.manualLivelinessCount = ((ParticipantManualLivelinessCount)param).getCount();
				break;
			case PID_SENTINEL:
				break;
			default:
				log.warn("Parameter {} not handled", param.getParameterId());
			}
		}
	}



	public ProtocolVersion_t getProtocolVersion() {
		return protocolVersion;
	}

	public GuidPrefix_t getGuidPrefix() {
		return guidPrefix;
	}

	public GUID_t getGuid() {
		return new GUID_t(guidPrefix, EntityId_t.PARTICIPANT);
	}
	
	public VendorId_t getVendorId() {
		return vendorId;
	}

	public boolean expectsInlineQos() {
		return expectsInlineQos;
	}

	public Locator_t getMetatrafficUnicastLocator() {
		return metatrafficUnicastLocator;
	}

	public Locator_t getMetatrafficMulticastLocator() {
		return metatrafficMulticastLocator;
	}
	public Locator_t getUnicastLocator() {
		return unicastLocator;
	}

	public Locator_t getMulticastLocator() {
		return multicastLocator;
	}

	public int getBuiltinEndpoints() {
		return availableBuiltinEndpoints;
	}

	public Duration_t getLeaseDuration() {
		return leaseDuration;
	}

	public int getManualLivelinessCount() {
		return manualLivelinessCount;
	}

	public Set<Locator_t> getUserLocators() {
		Set<Locator_t> userLocators = new HashSet<Locator_t>();
		userLocators.add(multicastLocator);
		userLocators.add(unicastLocator);
		
		return userLocators;
	}

	
	public Set<Locator_t> getMetatrafficLocators() {
		Set<Locator_t> metaLocators = new HashSet<Locator_t>();
		metaLocators.add(metatrafficMulticastLocator);
		metaLocators.add(metatrafficUnicastLocator);
		
		return metaLocators;
	}
	
	
	private Set<Locator_t> getAllLocators() {
		Set<Locator_t> allLocators = new HashSet<Locator_t>();
		allLocators.add(multicastLocator);
		allLocators.add(unicastLocator);
		allLocators.add(metatrafficMulticastLocator);
		allLocators.add(metatrafficUnicastLocator); 
		
		// TODO: SEDPbuiltinPublicationReader sends to getAllLocators()
		
		return allLocators;
	}
	
	public String toString() {
		return getGuidPrefix() + ": " + new BuiltinEndpointSet(getBuiltinEndpoints()) + 
				", lease duration " + getLeaseDuration() + ", locators " + getAllLocators();
	}
}
