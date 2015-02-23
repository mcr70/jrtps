package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParameterFactory {
    private static final Logger log = LoggerFactory.getLogger(ParameterFactory.class);

    /**
     * see 9.4.2.11 ParameterList
     * 
     * @param bb
     * @return Parameter
     */
    public static Parameter readParameter(RTPSByteBuffer bb) {
        bb.align(4);

        short paramId = (short) bb.read_short();
        int paramLength = 0;

        if (paramId != 0x0001 && paramId != 0x0000) { // SENTINEL & PAD
            paramLength = bb.read_short();
        }

        log.trace("Parameter {}, length {}", paramId, paramLength);

        Parameter param = null;

        switch (paramId) {
        // table 9.12:
        case 0x0000:
            param = new Pad();
            break;
        case 0x0001:
            param = new Sentinel();
            break;
        case 0x0002:
            param = new ParticipantLeaseDuration();
            break;
        case 0x0004:
            param = new QosTimeBasedFilter();
            break;
        case 0x0005:
            param = new TopicName();
            break;
        case 0x0006:
            param = new QosOwnershipStrength();
            break;
        case 0x0007:
            param = new TypeName();
            break;
        case 0x000b:
            param = new MetatrafficMulticastIPAddress();
            break;
        case 0x000c:
            param = new DefaultUnicastIPAddress();
            break;
        case 0x000d:
            param = new MetatrafficUnicastPort();
            break;
        case 0x000e:
            param = new DefaultUnicastPort();
            break;
        case 0x0011:
            param = new MulticastIPAddress();
            break;
        case 0x0015:
            param = new ProtocolVersion();
            break;
        case 0x0016:
            param = new VendorId();
            break;
        case 0x001a:
            param = new QosReliability();
            break;
        case 0x001b:
            param = new QosLiveliness();
            break;
        case 0x001d:
            param = new QosDurability();
            break;
        case 0x001e:
            param = new QosDurabilityService();
            break;
        case 0x001f:
            param = new QosOwnership();
            break;
        case 0x0021:
            param = new QosPresentation();
            break;
        case 0x0023:
            param = new QosDeadline();
            break;
        case 0x0025:
            param = new QosDestinationOrder();
            break;
        case 0x0027:
            param = new QosLatencyBudget();
            break;
        case 0x0029:
            param = new QosPartition();
            break;
        case 0x002b:
            param = new QosLifespan();
            break;
        case 0x002c:
            param = new QosUserData();
            break;
        case 0x002d:
            param = new QosGroupData();
            break;
        case 0x002e:
            param = new QosTopicData();
            break;
        case 0x002f:
            param = new UnicastLocator();
            break;
        case 0x0030:
            param = new MulticastLocator();
            break;
        case 0x0031:
            param = new DefaultUnicastLocator();
            break;
        case 0x0032:
            param = new MetatrafficUnicastLocator();
            break;
        case 0x0033:
            param = new MetatrafficMulticastLocator();
            break;
        case 0x0034:
            param = new ParticipantManualLivelinessCount();
            break;
        case 0x0035:
            param = new ContentFilterProperty();
            break;
        case 0x0040:
            param = new QosHistory();
            break;
        case 0x0041:
            param = new QosResourceLimits();
            break;
        case 0x0043:
            param = new ExpectsInlineQos();
            break;
        case 0x0044:
            param = new ParticipantBuiltinEndpoints();
            break;
        case 0x0045:
            param = new MetatrafficUnicastIPAddress();
            break;
        case 0x0046:
            param = new MetatrafficMulticastPort();
            break;
        case 0x0048:
            param = new DefaultMulticastLocator();
            break;
        case 0x0049:
            param = new QosTransportPriority();
            break;
        case 0x0050:
            param = new ParticipantGuid();
            break;
        case 0x0051:
            param = new ParticipantEntityId();
            break;
        case 0x0052:
            param = new GroupGuid();
            break;
        case 0x0053:
            param = new GroupEntityId();
            break;
        case 0x0055:
            param = new ContentFilterInfo();
            break;
        case 0x0056:
            param = new CoherentSet();
            break;
        case 0x0057:
            param = new DirectedWrite();
            break;
        case 0x0058:
            param = new BuiltinEndpointSet();
            break;
        case 0x0059:
            param = new PropertyList();
            break;
        case 0x005a: 
            param = new BuiltinTopicKey(); // see x-types doc, description of builtin topics (PublicationBuiltinTopicData etc.)
            break;   // RTPS spec does not have 0x005a, but ospl uses it anyway
        case 0x0060:
            param = new TypeMaxSizeSerialized();
            break;
        case 0x0061:
            param = new OriginalWriterInfo();
            break;
        case 0x0062:
            param = new EntityName();
            break;
        case 0x0070:
            param = new KeyHash();
            break;
        case 0x0071:
            param = new StatusInfo();
            break;

            // From x-types
        case 0x0072:
            param = new TypeObject(); 
            break;
        case 0x0073:
            param = new QosDataRepresentation(); 
            break;
        case 0x0074:
            param = new QosTypeConsistencyEnforcement();
            break;
        case 0x0075:
            param = new EquivalentTypeName();
            break;
        case 0x0076:
            param = new BaseTypeName();
            break;
            
            // Table 9.17: deprecated ids
        case 0x0003:
            param = new DeprecatedParameter.Persistence();
            break;
        case 0x0008:
            param = new DeprecatedParameter.TypeChecksum();
            break;
        case 0x0009:
            param = new DeprecatedParameter.Type2Name();
            break;
        case 0x000a:
            param = new DeprecatedParameter.Type2Checksum();
            break;
        case 0x0010:
            param = new DeprecatedParameter.ExpectsAck();
            break;
        case 0x0012:
            param = new DeprecatedParameter.ManagerKey();
            break;
        case 0x0013:
            param = new DeprecatedParameter.SendQueueSize();
            break;
        case 0x0014:
            param = new DeprecatedParameter.ReliabilityEnabled();
            break;
        case 0x0017:
            param = new DeprecatedParameter.VargappsSequenceNumberLast();
            break;
        case 0x0018:
            param = new DeprecatedParameter.RecvQueueSize();
            break;
        case 0x0019:
            param = new DeprecatedParameter.ReliabilityOffered();
            break;
        case 0x1001:
            param = new IdentityToken();
            break;
//        case 0x1002:
//            param = new PermissionsToken();
//            break;
            
        // jRTPS specific parameters
        case (short) 0x8ff1:
            param = new X509Cert();
            break;
            
        default:
            if ((paramId & 0x8000) == 0x8000) {
                param = new VendorSpecificParameter(paramId);
            } 
            else if ((paramId & 0x4000) == 0x4000) { // 9.6.2.2.1 ParameterId space
                // Incompatible QoS
                log.error("Found unknown Parameter {}({}) -> Mark it as incompatible QoS", 
                		paramId, Integer.toHexString(paramId));
                // TODO: what is a clean way of rejecting
                return null; // throw new IncompatibleQosException(...);
                //param = new UnknownParameter(paramId);
            } 
            else {
                // Ignore
                param = new UnknownParameter(paramId);
                log.error("Ignoring unknown Parameter {}({})", paramId, Integer.toHexString(paramId));
            }
        }

        param.read(bb, paramLength);

        return param;
    }
}
