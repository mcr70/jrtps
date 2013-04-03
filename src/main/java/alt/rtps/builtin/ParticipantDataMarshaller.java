package alt.rtps.builtin;

import java.util.Set;

import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.message.data.ParameterListEncapsulation;
import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.message.parameter.DefaultMulticastLocator;
import alt.rtps.message.parameter.DefaultUnicastLocator;
import alt.rtps.message.parameter.MetatrafficMulticastLocator;
import alt.rtps.message.parameter.MetatrafficUnicastLocator;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.ParticipantGuid;
import alt.rtps.message.parameter.ParticipantLeaseDuration;
import alt.rtps.message.parameter.ProtocolVersion;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.VendorId;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.Locator_t;

/**
 * ParticipantDataMarshaller is able to marshall / unmarshall builtin ParticipantData
 * instances to / from RTPSByteBuffer.
 * 
 * @author mcr70
 */
public class ParticipantDataMarshaller extends Marshaller<ParticipantData> {

	@Override
	public ParticipantData unmarshall(DataEncapsulation data) {
		ParameterListEncapsulation plEnc = (ParameterListEncapsulation) data;
		ParticipantData pd = new ParticipantData(plEnc.getParameterList());
		
		return pd;
	}

	
	@Override
	public DataEncapsulation marshall(ParticipantData pd) {
		ParameterList payloadParams = new ParameterList();

		payloadParams.add(new ProtocolVersion(pd.getProtocolVersion()));
		payloadParams.add(new VendorId(pd.getVendorId()));

		Locator_t unicastLocator = pd.getUnicastLocator();
		if (unicastLocator != null) {
			payloadParams.add(new DefaultUnicastLocator(unicastLocator));			
		}

		Locator_t multicastLocator = pd.getMulticastLocator();
		if (multicastLocator != null) {
			payloadParams.add(new DefaultMulticastLocator(multicastLocator));
		}

		Locator_t metaUnicastLocator = pd.getMetatrafficUnicastLocator();
		if (metaUnicastLocator != null) {
			payloadParams.add(new MetatrafficUnicastLocator(metaUnicastLocator));
		}

		Locator_t metaMulticastLocator = pd.getMetatrafficMulticastLocator();
		if (metaMulticastLocator != null) {
			payloadParams.add(new MetatrafficMulticastLocator(metaMulticastLocator));
		}

		payloadParams.add(new ParticipantLeaseDuration(pd.getLeaseDuration()));
		payloadParams.add(new ParticipantGuid(pd.getGuid()));
		payloadParams.add(new BuiltinEndpointSet(pd.getBuiltinEndpoints()));
		payloadParams.add(new Sentinel());
		
		return new ParameterListEncapsulation(payloadParams);
	}
}
