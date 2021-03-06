package net.sf.jrtps.builtin;

import java.util.Set;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.BuiltinTopicKey;
import net.sf.jrtps.message.parameter.DataWriterPolicy;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Marshaller for builtin data for topic <i>DCPSPublication</i>. With jRTPS,
 * instances of this topic is of type PublicationData.
 * 
 * @author mcr70
 */
public class PublicationDataMarshaller implements Marshaller<PublicationData> {
    private static final Logger log = LoggerFactory.getLogger(PublicationDataMarshaller.class);

    /**
     * PublicationData has always a key.
     * 
     * @return true
     */
    @Override
    public boolean hasKey() {
    	// Always false, KeyHash is optional and RTI Connext expects
    	// that KeyHash of builtin topics is not a MD5 sum
    	return true; // Always true. Key is PID_KEY_HASH.
    }

    /**
     * Extracts the key from PublicationData. Guid of the writer represented
     * by PublicationData is the key.
     * 
     * @return Guid as byte array
     */
    @Override
    public byte[] extractKey(PublicationData data) {
        return data.getBuiltinTopicKey().getBytes();
    }

    @Override
    public PublicationData unmarshall(DataEncapsulation data) {
        ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
        PublicationData wd = null;
        try {
            wd = new PublicationData(plEnc.getParameterList());
        } catch (InconsistentPolicy e) {
            log.error("Could not resolve inconsistent policies for WriterData", e);
        }

        return wd;
    }

    @Override
    public DataEncapsulation marshall(PublicationData wd) {
        ParameterList payloadParams = new ParameterList();

        payloadParams.add(new BuiltinTopicKey(wd.getBuiltinTopicKey()));
        payloadParams.add(new TopicName(wd.getTopicName()));
        payloadParams.add(new TypeName(wd.getTypeName()));

        addQoS(wd, payloadParams);
        payloadParams.add(new Sentinel());

        return new ParameterListEncapsulation(payloadParams);
    }

    private void addQoS(PublicationData wd, ParameterList payloadParams) {
        Set<DataWriterPolicy> policies = wd.getQualityOfService().getWriterPolicies();
        for (QosPolicy<?> qp : policies) {
            if (qp instanceof Parameter) {
                payloadParams.add((Parameter) qp); 
            }
        }
    }
}
