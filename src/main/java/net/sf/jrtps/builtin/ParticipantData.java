package net.sf.jrtps.builtin;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.DefaultMulticastLocator;
import net.sf.jrtps.message.parameter.DefaultUnicastLocator;
import net.sf.jrtps.message.parameter.EndpointSet;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.message.parameter.MetatrafficMulticastLocator;
import net.sf.jrtps.message.parameter.MetatrafficUnicastLocator;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.ParticipantGuid;
import net.sf.jrtps.message.parameter.ParticipantLeaseDuration;
import net.sf.jrtps.message.parameter.ParticipantManualLivelinessCount;
import net.sf.jrtps.message.parameter.PermissionsToken;
import net.sf.jrtps.message.parameter.ProtocolVersion;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosUserData;
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
 * ParticipantData represents data on topic "DCPSParticipants".
 * In addition to represent data of type ParticipantBuiltinTopicData,
 * this class also represents data of type ParticipantBuiltinTopicDataSecure,
 * which extends the former and adds IdentityToken and PermissionsToken 
 * parameters.<p>
 * 
 * See 8.5.3.2 SPDPdiscoveredParticipantData. DDS::ParticipantBuiltinTopicData
 * of RTPS specification
 * and 7.4.1.3 Extension to RTPS Standard DCPSParticipants Builtin Topic
 * of DDS Security
 * 
 * @author mcr70
 */
public class ParticipantData extends DiscoveredData {
    public static final String BUILTIN_TOPIC_NAME = "DCPSParticipant";
    public static final String BUILTIN_TYPE_NAME = "PARTICIPANT_BUILT_IN_TOPIC_TYPE";
    // TODO: for odds 3.5

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(ParticipantData.class);

    private ProtocolVersion_t protocolVersion = ProtocolVersion_t.PROTOCOLVERSION_2_1;
    private VendorId_t vendorId = VendorId_t.VENDORID_JRTPS;
    private GuidPrefix guidPrefix;
    private boolean expectsInlineQos = false;

    private final List<Locator> discoveryLocators;
    private final List<Locator> userdataLocators;

    private int availableBuiltinEndpoints;

    private Duration leaseDuration = new Duration(100, 0);

    /**
     * Used to implement MANUAL_BY_PARTICIPANT liveliness QoS. When liveliness
     * is asserted, the manualLivelinessCount is incremented and a new
     * SPDPdiscoveredParticipantData is sent.
     */
    private int manualLivelinessCount = 0; // TODO: check this

    /**
     * Time, that will mark remote participants lease as expired.
     */
    private long leaseExpirationTime;
	private IdentityToken identityToken;
	private PermissionsToken permissionsToken;

    public ParticipantData(GuidPrefix prefix, int endpoints, List<Locator> discoveryLocators,
            List<Locator> userdataLocators, QualityOfService participantQos) {
        super();
        if (participantQos == null) {
            participantQos = QualityOfService.getSPDPQualityOfService();
        }
        qos = participantQos;
        

        this.guidPrefix = prefix;
        this.availableBuiltinEndpoints = endpoints;
        this.discoveryLocators = discoveryLocators;
        this.userdataLocators = userdataLocators;

        super.topicName = BUILTIN_TOPIC_NAME;
        super.typeName = ParticipantData.class.getName();

        renewLease();
    }

    /**
     * Reads SPDPdiscoveredParticipantData. It is expected that inputstream is
     * positioned to start of serializedData of Data submessage, aligned at 32
     * bit boundary.
     * 
     * @param parameterList
     */
    public ParticipantData(ParameterList parameterList) {
        this.discoveryLocators = new LinkedList<>();
        this.userdataLocators = new LinkedList<>();
        super.qos = QualityOfService.getSPDPQualityOfService();
        
        Iterator<Parameter> iterator = parameterList.getParameters().iterator();

        while (iterator.hasNext()) {
            Parameter param = iterator.next();
            addParameter(param);
            
            switch (param.getParameterId()) {
            case PID_PARTICIPANT_GUID:
                this.guidPrefix = ((ParticipantGuid) param).getParticipantGuid().getPrefix();
                break;
            case PID_PARTICIPANT_BUILTIN_ENDPOINTS: // Handle PARTICIPANT_BUILTIN_ENDPOINTS (opendds)
            case PID_BUILTIN_ENDPOINT_SET:          // BUILTIN_ENDPOINT_SET (ospl) the same way
                availableBuiltinEndpoints = ((EndpointSet) param).getEndpointSet();
                break;
            case PID_PROTOCOL_VERSION:
                protocolVersion = ((ProtocolVersion) param).getProtocolVersion();
                break;
            case PID_VENDORID:
                vendorId = ((VendorId) param).getVendorId();
                break;
            case PID_DEFAULT_UNICAST_LOCATOR:
                userdataLocators.add(((DefaultUnicastLocator) param).getLocator());
                break;
            case PID_DEFAULT_MULTICAST_LOCATOR:
                userdataLocators.add(((DefaultMulticastLocator) param).getLocator());
                break;
            case PID_METATRAFFIC_UNICAST_LOCATOR:
                discoveryLocators.add(((MetatrafficUnicastLocator) param).getLocator());
                break;
            case PID_METATRAFFIC_MULTICAST_LOCATOR:
                discoveryLocators.add(((MetatrafficMulticastLocator) param).getLocator());
                break;
            case PID_PARTICIPANT_LEASE_DURATION:
                this.leaseDuration = ((ParticipantLeaseDuration) param).getDuration();
                break;
            case PID_PARTICIPANT_MANUAL_LIVELINESS_COUNT:
                this.manualLivelinessCount = ((ParticipantManualLivelinessCount) param).getCount();
                break;
            case PID_TYPE_NAME:
                super.typeName = ((TypeName) param).getTypeName();
                break;
            case PID_IDENTITY_TOKEN:
            	identityToken = (IdentityToken)param;
            	break;
            case PID_PERMISSIONS_TOKEN:
            	permissionsToken = (PermissionsToken)param;
            	break;
            case PID_SENTINEL:
                break;
            default:
                if (param instanceof QosPolicy) {
                    addQosPolicy((QosPolicy<?>) param);
                }
            }
        }

        if (super.typeName == null) { // Other vendors may use different typeName
            super.typeName = ParticipantData.class.getName();
        }

        log.debug("Parameters of discovered participant: {}", getParameters());

        renewLease();
    }

    /**
     * Gets the IdentityToken of ParticipantData if present.
     * @return IdentityToken, or null if it does not exist.
     */
    public IdentityToken getIdentityToken() {
		return identityToken;
	}
    
    /**
     * Gets the PermissionsToken of ParticipantData if present.
     * @return PermissionsToken, or null if it does not exist.
     */
    public PermissionsToken getPermissionsToken() {
		return permissionsToken;
	}
    
    /**
     * Gets the ProtocolVersion.
     * @return ProtocolVersion
     */
    public ProtocolVersion_t getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Gets the guid prefix.
     * @return GuidPrefix
     */
    public GuidPrefix getGuidPrefix() {
        return guidPrefix;
    }

    /**
     * Gets the guid of the partcipant.
     * 
     * @return Guid
     */
    public Guid getGuid() {
        return new Guid(guidPrefix, EntityId.PARTICIPANT);
    }

    /**
     * Get the VendorId
     * @return VendorId
     */
    public VendorId_t getVendorId() {
        return vendorId;
    }

    /**
     * Gets ExpectsInlineQos
     * @return ExpectsInlineQos
     */
    public boolean expectsInlineQos() {
        return expectsInlineQos;
    }

    public int getBuiltinEndpoints() {
        return availableBuiltinEndpoints;
    }

    /**
     * Lease duration associated with remote participant represented by this
     * ParticipantData. Remote participant is expected to renew its lease during
     * its lease duration.
     * 
     * @return Lease duration
     */
    public Duration getLeaseDuration() {
        return leaseDuration;
    }

    public int getManualLivelinessCount() {
        return manualLivelinessCount;
    }

    /**
     * Renews lease time that has been associated with this ParticipantData
     * 
     * @see #getLeaseDuration()
     */
    public void renewLease() {
        this.leaseExpirationTime = System.currentTimeMillis() + leaseDuration.asMillis();
    }

    public void setLeaseDuration(Duration duration) {
        this.leaseDuration = duration;
        renewLease();
    }

    /**
     * Checks, if the lease has been expired or not.
     * 
     * @return true, if lease has been expired
     */
    public boolean isLeaseExpired() {
        long currentTimeMillis = System.currentTimeMillis();

        return currentTimeMillis > leaseExpirationTime;
    }

    /**
     * Gets the time when the lease will be expired.
     * 
     * @return the time lease will expire
     */
    public long getLeaseExpirationTime() {
        return leaseExpirationTime;
    }

    /**
     * Gets the list of Locators that can be used for discovery(metadata).
     * @return List of Locators for discovery
     */
    public List<Locator> getDiscoveryLocators() {
        return discoveryLocators;
    }
    
    /**
     * Gets the list of Locators that can be used for user data
     * @return List of Locators for user data
     */
    public List<Locator> getUserdataLocators() {
        return userdataLocators;
    }

    public String toString() {
        return getGuidPrefix() + ": " + new BuiltinEndpointSet(getBuiltinEndpoints()) + ", lease duration "
                + getLeaseDuration() + ", userdata locators: " + userdataLocators + 
                ", discovery locators: " + discoveryLocators + ", " + qos;
    }

    /**
     * Gets the UserData QoS policy
     * @return QosUserData
     */
    public QosUserData getUserData() {
        return qos.getUserData();
    }
}
