package net.sf.jrtps.builtin;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Marshaller for builtin data for topic DCPSSubscription.
 * With jRTPS, instances of this topic is of type ReaderData.
 * 
 * @author mcr70
 *
 */
public class ReaderDataMarshaller implements Marshaller<ReaderData> {
	private static final Logger log = LoggerFactory.getLogger(ReaderDataMarshaller.class);

	@Override
	public boolean hasKey() {
		return true; // Always true. Key is PID_KEY_HASH.
	}

	@Override
	public byte[] extractKey(ReaderData data) {
		return data.getKey().getBytes();
	}

	@Override
	public ReaderData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		ReaderData rd = null;
		try {
			rd = new ReaderData(plEnc.getParameterList());
		} catch (InconsistentPolicy e) {
			log.error("Could not resolve inconsistent policies for ReaderData", e);
		}
		
		return rd;
	}

	@Override
	public DataEncapsulation marshall(ReaderData rd) {
		ParameterList payloadParams = new ParameterList();
		 
		payloadParams.add(new TopicName(rd.getTopicName()));
		payloadParams.add(new TypeName(rd.getTypeName()));
		payloadParams.add(new KeyHash(rd.getKey().getBytes()));
		
		addQoS(rd, payloadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}
	
	private void addQoS(ReaderData rd, ParameterList payloadParams) {
		Set<QosPolicy<?>> inlineableQosPolicies = rd.getInlineableQosPolicies();
		for (QosPolicy<?> qp : inlineableQosPolicies) {
			payloadParams.add((Parameter) qp);
		}
	}
}
