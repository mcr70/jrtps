package net.sf.jrtps.builtin;

import java.util.Iterator;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.message.parameter.ExpectsInlineQos;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.ParticipantGuid;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.ContentFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents DDS::SubscriptionBuiltinTopicData
 * 
 * @author mcr70
 */
public class SubscriptionData extends DiscoveredData {
    public static final String BUILTIN_TOPIC_NAME = "DCPSSubscription";
    public static final String BUILTIN_TYPE_NAME = "SUBSCRIPTION_BUILT_IN_TOPIC_TYPE";
    // TODO: for odds 3.5

    private static final Logger log = LoggerFactory.getLogger(SubscriptionData.class);

    private Guid participantGuid;
    private boolean expectsInlineQos = false;

    private ContentFilterProperty contentFilter;

    public SubscriptionData(ParameterList parameterList) throws InconsistentPolicy {
        Iterator<Parameter> iter = parameterList.getParameters().iterator();
        while (iter.hasNext()) {
            Parameter param = iter.next();
            addParameter(param);

            log.trace("{}", param);
            switch (param.getParameterId()) {
            case PID_PROTOCOL_VERSION:
            case PID_VENDORID:
                // These parameters get sent by OSPL 5.5. We can ignore these
                break;
            case PID_TOPIC_NAME:
                super.topicName = ((TopicName) param).getName();
                break;
            case PID_PARTICIPANT_GUID:
                participantGuid = ((ParticipantGuid) param).getParticipantGuid();
                break;
            case PID_TYPE_NAME:
                super.typeName = ((TypeName) param).getTypeName();
                break;
            case PID_BUILTIN_TOPIC_KEY:
            case PID_KEY_HASH:
                super.key = new Guid(param.getBytes()); // TODO: We should store
                                                        // either GUID, or
                                                        // KeyHash only
                break;
            case PID_EXPECTS_INLINE_QOS:
                expectsInlineQos = ((ExpectsInlineQos) param).expectsInlineQos();
                break;
            case PID_CONTENT_FILTER_PROPERTY:
            	contentFilter = (ContentFilterProperty) param;
            	break;
            case PID_SENTINEL:
                break;
            case PID_PAD:
                // Ignore
                break;

            default:
                if (param instanceof QosPolicy) {
                    addQosPolicy((QosPolicy<?>) param);
                }
            }
        }

        if (super.typeName == null) { // Other vendors may use different typeName
            super.typeName = SubscriptionData.class.getName();
        }

        log.debug("Parameters of discovered subscription: {}", getParameters());

        resolveInconsistencies();
    }

    public SubscriptionData(String topicName, String typeName, Guid key, QualityOfService qos) {
    	this(topicName, typeName, key, null, qos);
    }
    
    public SubscriptionData(String topicName, String typeName, Guid key, ContentFilterProperty cfp, 
    		QualityOfService qos) {
        super(typeName, topicName, key, qos);
        contentFilter = cfp;
    }

    public Guid getParticipantGuid() {
        return participantGuid;
    }

    public boolean expectsInlineQos() {
        return expectsInlineQos;
    }

    /**
     * Gets the ContentFilterProperty associated with this SubscriptionData.
     * @return ContentFilterProperty, or null if there is no ContentFilterProperty available.
     */
    public ContentFilterProperty getContentFilter() {
        return contentFilter;
    }
}
