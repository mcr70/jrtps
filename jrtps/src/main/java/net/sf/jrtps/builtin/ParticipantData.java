package net.sf.jrtps.builtin;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sf.jrtps.SPDPQualityOfService;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.DefaultMulticastLocator;
import net.sf.jrtps.message.parameter.DefaultUnicastLocator;
import net.sf.jrtps.message.parameter.EndpointSet;
import net.sf.jrtps.message.parameter.MetatrafficMulticastLocator;
import net.sf.jrtps.message.parameter.MetatrafficUnicastLocator;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.ParticipantGuid;
import net.sf.jrtps.message.parameter.ParticipantLeaseDuration;
import net.sf.jrtps.message.parameter.ParticipantManualLivelinessCount;
import net.sf.jrtps.message.parameter.ProtocolVersion;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.ProtocolVersion_t;
import net.sf.jrtps.types.VendorId_t;

import org.slf4j.LoggerFactory;

/**
 * see 8.5.3.2 SPDPdiscoveredParticipantData.
 * DDS::ParticipantBuiltinTopicData
 * 
 * @author mcr70
 * 
 */
public class ParticipantData extends DiscoveredData {
	public static final String BUILTIN_TOPIC_NAME = "DCPSParticipant";

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(ParticipantData.class);
	
	private ProtocolVersion_t protocolVersion = ProtocolVersion_t.PROTOCOLVERSION_2_1;
	private VendorId_t vendorId = VendorId_t.VENDORID_JRTPS;
	private GuidPrefix guidPrefix; 	
	private boolean expectsInlineQos = false; 

	/**
	 * List of unicast locators (transport, address, port combinations) that
	 * can be used to send messages to the built-in Endpoints contained in the Participant.
	 */
	private Locator metatrafficUnicastLocator;

	/**
	 * List of multicast locators (transport, address, port combinations)
	 * that can be used to send messages to the built-in Endpoints contained in the Participant.
	 */
	private Locator metatrafficMulticastLocator;

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
	private Locator unicastLocator; 

	/**
	 * Default list of multicast locators (transport, address, port combinations) that can be used 
	 * to send messages to the userdefined Endpoints contained in the Participant.
	 * These are the multicast locators that will be used in case the Endpoint does not specify its 
	 * own set of Locators.
	 */
	private Locator multicastLocator;

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
	private Duration leaseDuration = new Duration(15, 0); // TODO: 100 sec

	/**
	 * Used to implement MANUAL_BY_PARTICIPANT liveliness QoS.
	 * When liveliness is asserted, the manualLivelinessCount is incremented and 
	 * a new SPDPdiscoveredParticipantData is sent.
	 */
	private int manualLivelinessCount = 0;

	/**
	 * Time, that will mark remote participants lease as expired.
	 */
	private long leaseExpirationTime;


	public ParticipantData(GuidPrefix prefix, int endpoints,
			Locator u_ucLocator, Locator u_mcLocator, 
			Locator m_ucLocator, Locator m_mcLocator) {
		super();
		qos = new SPDPQualityOfService();
		
		guidPrefix = prefix;
		availableBuiltinEndpoints = endpoints;
		
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
		
		super.topicName = BUILTIN_TOPIC_NAME;
		super.typeName = ParticipantData.class.getName();
		
		renewLease();
	}
	
	/**
	 * Reads SPDPdiscoveredParticipantData. It is expected that inputstream is 
	 * positioned to start of serializedData of Data submessage, aligned at 32 bit
	 * boundary.
	 * 
	 * @param parameterList
	 */
	public ParticipantData(ParameterList parameterList) {
		qos = new SPDPQualityOfService();
		Iterator<Parameter> iterator = parameterList.getParameters().iterator();
		
		while (iterator.hasNext()) {
			Parameter param = iterator.next();

			log.trace("{}", param);
			switch(param.getParameterId()) {
			case PID_PARTICIPANT_GUID:
				this.guidPrefix = ((ParticipantGuid)param).getParticipantGuid().getPrefix();
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
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName();
				break;
			case PID_SENTINEL:
				break;
			default:
				log.warn("Parameter {} not handled", param.getParameterId());
			}
		}
		
		if (super.typeName == null) { // Other vendors may use different typeName
			super.typeName = ParticipantData.class.getName();
		}
		
		renewLease();
	}



	public ProtocolVersion_t getProtocolVersion() {
		return protocolVersion;
	}

	public GuidPrefix getGuidPrefix() {
		return guidPrefix;
	}

	/**
	 * Gets the guid of the partcipant.
	 * @return Guid
	 */
	public Guid getGuid() {
		return new Guid(guidPrefix, EntityId.PARTICIPANT);
	}
	
	public VendorId_t getVendorId() {
		return vendorId;
	}

	public boolean expectsInlineQos() {
		return expectsInlineQos;
	}

	public Locator getMetatrafficUnicastLocator() {
		return metatrafficUnicastLocator;
	}

	public Locator getMetatrafficMulticastLocator() {
		return metatrafficMulticastLocator;
	}
	public Locator getUnicastLocator() {
		return unicastLocator;
	}

	public Locator getMulticastLocator() {
		return multicastLocator;
	}

	public int getBuiltinEndpoints() {
		return availableBuiltinEndpoints;
	}

	/**
	 * Lease duration associated with remote participant represented by this ParticipantData. 
	 * Remote participant is expected to renew its lease during its lease duration.  
	 * 
	 * @return Lease duration
	 */
	public Duration getLeaseDuration() {
		return leaseDuration;
	}

	public int getManualLivelinessCount() {
		return manualLivelinessCount;
	}

	public Set<Locator> getUserLocators() {
		Set<Locator> userLocators = new HashSet<Locator>();
		userLocators.add(multicastLocator);
		userLocators.add(unicastLocator);
		
		return userLocators;
	}

	
	public Set<Locator> getMetatrafficLocators() {
		Set<Locator> metaLocators = new HashSet<Locator>();
		metaLocators.add(metatrafficMulticastLocator);
		metaLocators.add(metatrafficUnicastLocator);
		
		return metaLocators;
	}
	
	
	private Set<Locator> getAllLocators() {
		Set<Locator> allLocators = new HashSet<Locator>();
		allLocators.add(multicastLocator);
		allLocators.add(unicastLocator);
		allLocators.add(metatrafficMulticastLocator);
		allLocators.add(metatrafficUnicastLocator); 
		
		return allLocators;
	}
	
	/**
	 * Renews lease time that has been associated with this ParticipantData
	 * @see #getLeaseDuration()
	 */
	public void renewLease() {
		this.leaseExpirationTime = System.currentTimeMillis() + leaseDuration.asMillis();
	}

	/**
	 * Checks, if the lease has been expired or not.
	 * @return true, if lease has been expired
	 */
	public boolean isLeaseExpired() {
		long currentTimeMillis = System.currentTimeMillis();
		
		return currentTimeMillis > leaseExpirationTime;
	}

	/**
	 * Gets the time when the lease will be expired.
	 * @return the time lease will expire
	 */
	public long getLeaseExpirationTime() {
		return leaseExpirationTime;
	}

	public String toString() {
		return getGuidPrefix() + ": " + new BuiltinEndpointSet(getBuiltinEndpoints()) + 
				", lease duration " + getLeaseDuration() + ", locators " + getAllLocators() + 
				", " + qos;
	}
}
