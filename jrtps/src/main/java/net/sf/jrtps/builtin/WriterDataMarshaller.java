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
 * Marshaller for builtin data for topic DCPSPublication.
 * With jRTPS, instances of this topic is of type WriterData.
 * 
 * @author mcr70
 *
 */
public class WriterDataMarshaller extends Marshaller<WriterData> {
	private static final Logger log = LoggerFactory.getLogger(WriterDataMarshaller.class);

	@Override
	public boolean hasKey(Class<?> data) {
		return true; // Always true. Key is PID_KEY_HASH.
	}

	@Override
	public byte[] extractKey(WriterData data) {
		return data.getKey().getBytes();
	}

	@Override
	public WriterData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		WriterData wd = null;
		try {
			wd = new WriterData(plEnc.getParameterList());
		} catch (InconsistentPolicy e) {
			log.error("Could not resolve inconsistent policies for WriterData", e);
		}
		
		return wd;
	}

	@Override
	public DataEncapsulation marshall(WriterData wd) {
		ParameterList payloadParams = new ParameterList();

		payloadParams.add(new TopicName(wd.getTopicName()));
		payloadParams.add(new TypeName(wd.getTypeName()));
		payloadParams.add(new KeyHash(wd.getKey().getBytes()));
		
		addQoS(wd, payloadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}

	private void addQoS(WriterData wd, ParameterList payloadParams) {
		Set<QosPolicy<?>> inlineableQosPolicies = wd.getInlineableQosPolicies();
		for (QosPolicy<?> qp : inlineableQosPolicies) {
			payloadParams.add((Parameter) qp);
		}
	}
}
