package alt.rtps.builtin;

import java.util.Set;

import alt.rtps.message.DataEncapsulation;
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

		Set<Locator_t> defaultUnicastLocatorList = pd.getDefaultUnicastLocatorList();
		for (Locator_t loc : defaultUnicastLocatorList) {
			payloadParams.add(new DefaultUnicastLocator(loc));			
		}

		Set<Locator_t> defaultMulticastLocatorList = pd.getDefaultMulticastLocatorList();
		for (Locator_t loc : defaultMulticastLocatorList) {
			payloadParams.add(new DefaultMulticastLocator(loc));
		}

		Set<Locator_t> metatrafficUnicastLocatorList = pd.getMetatrafficUnicastLocatorList();
		for (Locator_t loc : metatrafficUnicastLocatorList) {
			payloadParams.add(new MetatrafficUnicastLocator(loc));
		}

		Set<Locator_t> metatrafficMulticastLocatorList = pd.getMetatrafficMulticastLocatorList();
		for (Locator_t loc : metatrafficMulticastLocatorList) {
			payloadParams.add(new MetatrafficMulticastLocator(loc));
		}

		payloadParams.add(new ParticipantLeaseDuration(pd.getLeaseDuration()));
		payloadParams.add(new ParticipantGuid(pd.getGuid()));
		payloadParams.add(new BuiltinEndpointSet(pd.getBuiltinEndpoints()));
		payloadParams.add(new Sentinel());
		
		return new ParameterListEncapsulation(payloadParams);
	}
}
