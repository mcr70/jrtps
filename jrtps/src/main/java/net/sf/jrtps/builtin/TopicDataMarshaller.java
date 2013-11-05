package net.sf.jrtps.builtin;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;

/**
 * Marshaller for builtin data for topic DCPSTopic.
 * With jRTPS, instances of this topic is of type TopicData.
 * 
 * @author mcr70
 *
 */
public class TopicDataMarshaller extends Marshaller<TopicData> {

	@Override
	public boolean hasKey(Class<?> data) {
		return true; // Always true. Key is PID_KEY_HASH.
	}

	@Override
	public byte[] extractKey(TopicData data) {
		return data.getKey().getBytes();
	}

	@Override
	public TopicData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		TopicData wd = new TopicData(plEnc.getParameterList());
		
		return wd;
	}

	@Override
	public DataEncapsulation marshall(TopicData td) {
		ParameterList payloadParams = new ParameterList();
		 
		payloadParams.add(new TopicName(td.getTopicName()));
		payloadParams.add(new TypeName(td.getTypeName()));
		payloadParams.add(new KeyHash(td.getKey().getBytes()));
		
		// addQos(wd, payLoadParams);
		payloadParams.add(new Sentinel());

		return new ParameterListEncapsulation(payloadParams);
	}
}
