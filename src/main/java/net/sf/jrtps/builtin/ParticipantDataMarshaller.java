package net.sf.jrtps.builtin;

import java.util.List;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.ParameterListEncapsulation;
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

        payloadParams.add(pd.getProtocolVersion());
        payloadParams.add(new ParticipantGuid(pd.getGuid()));
        payloadParams.add(new VendorId(pd.getVendorId()));

        payloadParams.add(new ParticipantBuiltinEndpoints(pd.getBuiltinEndpoints()));
        payloadParams.add(new BuiltinEndpointSet(pd.getBuiltinEndpoints()));

        
        List<Locator> discoveryLocators = pd.getDiscoveryLocators();
        if (discoveryLocators != null) {
            for (Locator loc : discoveryLocators) {
                if (loc.isMulticastLocator()) {
                    payloadParams.add(new MetatrafficMulticastLocator(loc));
                }
                else {
                    payloadParams.add(new MetatrafficUnicastLocator(loc));                                        
                }
            }
        }
        
        List<Locator> userdataLocators = pd.getUserdataLocators();
        if (userdataLocators != null) {
            for (Locator loc : userdataLocators) {
                if (loc.isMulticastLocator()) {
                    payloadParams.add(new DefaultMulticastLocator(loc));
                }
                else {
                    payloadParams.add(new DefaultUnicastLocator(loc));
                }
            }
        }

        payloadParams.add(pd.getUserData());
        
        payloadParams.add(new ParticipantLeaseDuration(pd.getLeaseDuration()));
        payloadParams.add(new Sentinel());

        return new ParameterListEncapsulation(payloadParams);
    }
}
