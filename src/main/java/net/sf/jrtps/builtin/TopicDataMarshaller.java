package net.sf.jrtps.builtin;

import java.util.Set;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marshaller for builtin data for topic DCPSTopic. With jRTPS, instances of
 * this topic is of type TopicData.
 * 
 * @author mcr70
 * 
 */
public class TopicDataMarshaller implements Marshaller<TopicData> {
    private static final Logger log = LoggerFactory.getLogger(TopicDataMarshaller.class);

    @Override
    public boolean hasKey() {
        return true; // Always true. Key is PID_KEY_HASH.
    }

    @Override
    public byte[] extractKey(TopicData data) {
        return data.getKey().getBytes();
    }

    @Override
    public TopicData unmarshall(DataEncapsulation data) {
        ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
        TopicData wd = null;
        try {
            wd = new TopicData(plEnc.getParameterList());
        } catch (InconsistentPolicy e) {
            log.error("Could not resolve inconsistent policies for TopicData", e);
        }

        return wd;
    }

    @Override
    public DataEncapsulation marshall(TopicData td) {
        ParameterList payloadParams = new ParameterList();

        payloadParams.add(new TopicName(td.getTopicName()));
        payloadParams.add(new TypeName(td.getTypeName()));
        payloadParams.add(new KeyHash(td.getKey().getBytes()));

        addQoS(td, payloadParams);
        payloadParams.add(new Sentinel());

        return new ParameterListEncapsulation(payloadParams);
    }

    private void addQoS(TopicData td, ParameterList payloadParams) {
        Set<QosPolicy<?>> inlineableQosPolicies = td.getInlineableQosPolicies();
        for (QosPolicy<?> qp : inlineableQosPolicies) {
            payloadParams.add((Parameter) qp);
        }
    }
}
