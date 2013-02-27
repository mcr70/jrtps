package alt.rtps.message;

import java.util.List;

import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
import alt.rtps.types.ProtocolVersion_t;
import alt.rtps.types.Time_t;
import alt.rtps.types.VendorId_t;

public class ReceiverState {
	// 'Receiver' state. TODO: consider this. Do we need to maintain this state.
	private ProtocolVersion_t sourceVersion;
	private VendorId_t sourceVendorId;
	private GuidPrefix_t sourceGuidPrefix;
	private GuidPrefix_t destGuidPrefix; // If GUIDPREFIX_UNKNOWN, -> GuidPrefix of _this_ participant
	private List<Locator_t> unicastReplyLocatorList;
	private List<Locator_t> multicastReplyLocatorList;
	private Time_t timestamp;
	private boolean hasInfoReplyIp4 = false; // TODO: LocatorIPV4_t is not compatible with Locator_t
	private InfoReplyIp4 infoReplyIp4;

	
	public void setTimestamp(Time_t timeStamp) {
		timestamp = timeStamp;
	}


	public void setSourceGuidPrefix(GuidPrefix_t guidPrefix) {
		sourceGuidPrefix = guidPrefix;
	}


	public void setSourceVersion(ProtocolVersion_t protocolVersion) {
		sourceVersion = protocolVersion;
	}


	public void setSourceVendorId(VendorId_t vendorId) {
		sourceVendorId = vendorId;
	}


	public void setInfoReplyIp4(InfoReplyIp4 infoReplyIp4) {
		this.infoReplyIp4 = infoReplyIp4;
	}


	public void setDestinationGuidPrefix(GuidPrefix_t guidPrefix) {
		destGuidPrefix = guidPrefix;
	}


	public void setUnicastReplyLocatorList(List<Locator_t> ucList) {
		unicastReplyLocatorList = ucList;
	}


	public void setMulticastReplyLocatorList(List<Locator_t> mcList) {
		multicastReplyLocatorList = mcList;
	}
}
