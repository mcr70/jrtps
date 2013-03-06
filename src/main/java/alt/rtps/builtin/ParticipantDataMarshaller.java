package alt.rtps.builtin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import alt.rtps.message.Data;
import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.message.parameter.DefaultMulticastLocator;
import alt.rtps.message.parameter.DefaultUnicastLocator;
import alt.rtps.message.parameter.MetatrafficMulticastLocator;
import alt.rtps.message.parameter.MetatrafficUnicastLocator;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParticipantGuid;
import alt.rtps.message.parameter.ParticipantLeaseDuration;
import alt.rtps.message.parameter.ProtocolVersion;
import alt.rtps.message.parameter.Sentinel;
import alt.rtps.message.parameter.VendorId;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.Locator_t;

/**
 * ParticipantDataMarshaller is able to marshall / unmarshall builtin ParticipantData
 * instances to / from RTPSByteBuffer.
 * 
 * @author mcr70
 */
public class ParticipantDataMarshaller extends Marshaller<ParticipantData> {

	@Override
	public ParticipantData unmarshall(RTPSByteBuffer bb) {
		ParticipantData pd = new ParticipantData(bb);
		
		return pd;
	}

	
	@Override
	public Data marshall(ParticipantData pd) {
		List<Parameter> inlineQosParams = new LinkedList<Parameter>();
		inlineQosParams.add(new Sentinel());

		List<Parameter> payloadParams = new LinkedList<Parameter>(); 
		// ---  Start of ParameterList

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
		// ---  End of ParameterList

		Data data = new Data(EntityId_t.UNKNOWN_ENTITY, EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER, 
				1, pd.getGuid(), 0x03cf, inlineQosParams, payloadParams);
		
		return data;
	}
}
