package net.sf.jrtps.builtin;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.data.ParameterListEncapsulation;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.DefaultMulticastLocator;
import net.sf.jrtps.message.parameter.DefaultUnicastLocator;
import net.sf.jrtps.message.parameter.MetatrafficMulticastLocator;
import net.sf.jrtps.message.parameter.MetatrafficUnicastLocator;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.ParticipantBuiltinEndpoints;
import net.sf.jrtps.message.parameter.ParticipantGuid;
import net.sf.jrtps.message.parameter.ParticipantLeaseDuration;
import net.sf.jrtps.message.parameter.ProtocolVersion;
import net.sf.jrtps.message.parameter.Sentinel;
import net.sf.jrtps.message.parameter.VendorId;
import net.sf.jrtps.types.Locator;

/**
 * Marshaller for builtin data for topic <i>DCPSParticipant</i>. With jRTPS,
 * instances of this topic is of type ParticipantData.
 * 
 * @author mcr70
 */
public class ParticipantDataMarshaller implements Marshaller<ParticipantData> {

    @Override
    public boolean hasKey() {
        return false; // hardcoded. Key is remote participants GUID
    }

    @Override
    public byte[] extractKey(ParticipantData data) {
        return new byte[0];
        // return data.getGuid().getBytes();
    }

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
        payloadParams.add(new ParticipantGuid(pd.getGuid()));
        payloadParams.add(new VendorId(pd.getVendorId()));

        payloadParams.add(new ParticipantBuiltinEndpoints(pd.getBuiltinEndpoints()));
        payloadParams.add(new BuiltinEndpointSet(pd.getBuiltinEndpoints()));
        
        Locator metaUnicastLocator = pd.getMetatrafficUnicastLocator();
        if (metaUnicastLocator != null) {
            payloadParams.add(new MetatrafficUnicastLocator(metaUnicastLocator));
        }

        Locator metaMulticastLocator = pd.getMetatrafficMulticastLocator();
        if (metaMulticastLocator != null) {
            payloadParams.add(new MetatrafficMulticastLocator(metaMulticastLocator));
        }

        Locator unicastLocator = pd.getUnicastLocator();
        if (unicastLocator != null) {
            payloadParams.add(new DefaultUnicastLocator(unicastLocator));
        }

        Locator multicastLocator = pd.getMulticastLocator();
        if (multicastLocator != null) {
            payloadParams.add(new DefaultMulticastLocator(multicastLocator));
        }

        payloadParams.add(new ParticipantLeaseDuration(pd.getLeaseDuration()));
        payloadParams.add(new Sentinel());

        return new ParameterListEncapsulation(payloadParams);
    }
}
