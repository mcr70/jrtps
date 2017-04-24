package net.sf.jrtps.message.parameter;

/**
 * ParameterIds for Data submessage. see tables 9.12, 9.14 and 9.17
 * 
 * @author mcr70
 * 
 */
public enum ParameterId {
    PID_PAD(0x0000), 
    PID_SENTINEL(0x0001), 
    PID_PARTICIPANT_LEASE_DURATION(0x0002), 
    PID_PERSISTENCE(0x0003), // Table 9.17: deprecated
    PID_TIME_BASED_FILTER(0x0004), 
    PID_TOPIC_NAME(0x0005), // string<256>
    PID_OWNERSHIP_STRENGTH(0x0006), 
    PID_TYPE_NAME(0x0007), // string<256>
    PID_TYPE_CHECKSUM(0x0008), // Table 9.17: deprecated
    PID_TYPE2_NAME(0x0009), // Table 9.17: deprecated
    PID_TYPE2_CHECKSUM(0x000a), // Table 9.17: deprecated
    PID_EXPECTS_ACK(0x0010), // Table 9.17: deprecated
    PID_METATRAFFIC_MULTICAST_IPADDRESS(0x000b), 
    PID_DEFAULT_UNICAST_IPADDRESS(0x000c), 
    PID_METATRAFFIC_UNICAST_PORT(0x000d), 
    PID_DEFAULT_UNICAST_PORT(0x000e), 
    PID_MULTICAST_IPADDRESS(0x0011), 
    PID_MANAGER_KEY(0x0012), // Table 9.17: deprecated
    PID_SEND_QUEUE_SIZE(0x0013), // Table 9.17: deprecated
    PID_RELIABILITY_ENABLED(0x0014), // Table 9.17: deprecated
    PID_PROTOCOL_VERSION(0x0015), 
    PID_VENDORID(0x0016), 
    PID_VARGAPPS_SEQUENCE_NUMBER_LAST(0x0017), // Table 9.17: deprecated
    PID_RECV_QUEUE_SIZE(0x0018), // Table 9.17: deprecated
    PID_RELIABILITY_OFFERED(0x0019),// Table 9.17: deprecated
    PID_RELIABILITY(0x001a), // ReliabilityQosPolicy
    PID_LIVELINESS(0x001b), // LivelinessQosPolicy
    PID_DURABILITY(0x001d), // DurabilityQosPolicy
    PID_DURABILITY_SERVICE(0x001e), // DurabilitServiceyQosPolicy
    PID_OWNERSHIP(0x001f), 
    PID_DEADLINE(0x0023), // DeadLineQosPolicy
    PID_PRESENTATION(0x0021), 
    PID_DESTINATION_ORDER(0x0025), 
    PID_LATENCY_BUDGET(0x0027), // LatencyBudgetQosPolicy
    PID_PARTITION(0x0029), 
    PID_LIFESPAN(0x002b), // LifeSpanQosPolicy
    PID_USER_DATA(0x002c), // UserDataQosPolicy
    PID_GROUP_DATA(0x002d), // GroupDataQosPolicy
    PID_TOPIC_DATA(0x002e), // TopicDataQosPolicy
    PID_UNICAST_LOCATOR(0x002f), 
    PID_MULTICAST_LOCATOR(0x0030), 
    PID_DEFAULT_UNICAST_LOCATOR(0x0031), 
    PID_METATRAFFIC_UNICAST_LOCATOR(0x0032), 
    PID_METATRAFFIC_MULTICAST_LOCATOR(0x0033), 
    PID_PARTICIPANT_MANUAL_LIVELINESS_COUNT(0x0034), 
    PID_CONTENT_FILTER_PROPERTY(0x0035), 
    PID_HISTORY(0x0040), 
    PID_RESOURCE_LIMITS(0x0041), 
    PID_EXPECTS_INLINE_QOS(0x0043), 
    PID_PARTICIPANT_BUILTIN_ENDPOINTS(0x0044), 
    PID_METATRAFFIC_UNICAST_IPADDRESS(0x0045), 
    PID_METATRAFFIC_MULTICAST_PORT(0x0046), 
    PID_DEFAULT_MULTICAST_LOCATOR(0x0048), 
    PID_TRANSPORT_PRIORITY(0x0049), 
    PID_PARTICIPANT_GUID(0x0050), 
    PID_PARTICIPANT_ENTITYID(0x0051), 
    PID_GROUP_GUID(0x0052), 
    PID_GROUP_ENTITYID(0x0053), 
    
    PID_CONTENT_FILTER_INFO(0x0055), // Table 9.14  
    PID_COHERENT_SET(0x0056), // Table 9.14
    PID_DIRECTED_WRITE(0x0057), // Table 9.14
    PID_BUILTIN_ENDPOINT_SET(0x0058), // Table 9.14
    PID_PROPERTY_LIST(0x0059), 
    PID_BUILTIN_TOPIC_KEY(0x005a),
    PID_TYPE_MAX_SIZE_SERIALIZED(0x0060), 
    PID_ORIGINAL_WRITER_INFO(0x0061), // Table 9.14
    PID_ENTITY_NAME(0x0062), 
    PID_KEY_HASH(0x0070), 
    PID_STATUS_INFO(0x0071),

    // from x-types:
    PID_TYPE_OBJECT(0x0072),
    PID_DATA_REPRESENTATION(0x0073),
    PID_TYPE_CONSISTENCY_ENFORCEMENT(0x0074),
    PID_EQUIVALENT_TYPE_NAME(0x0075),
    PID_BASE_TYPE_NAME(0x0076),
    // from DDS RPC:
    PID_SERVICE_INSTANCE_NAME(0x0080),
    PID_RELATED_ENTITY_GUID(0x0081),
    PID_TOPIC_ALIASES(0x0082),
    
    // from DDS Security:
    PID_IDENTITY_TOKEN(0x1001),
    PID_PERMISSIONS_TOKEN(0x1002),
    PID_DATA_TAGS(0x1003),
    
    // ----  JRTPS specific parameters  -----------------
    // PID_UNKNOWN_PARAMETER, PID_VENDOR_SPECIFIC are never sent by jRTPS
    // On reception, 
    PID_UNKNOWN_PARAMETER(0x8000), // 0x8000 is just invented, @see 9.6.2.2.1
    PID_VENDOR_SPECIFIC(0x8000),   // 0x8000 is just invented, @see 9.6.2.2.1
    PID_X509CERT(0x8ff1);
    
    // ParameterId space
    // ParameterId space

    private short kind;

    ParameterId(int kind) {
        this.kind = (short) kind;
    }

    public short kind() {
        return kind;
    }
}
