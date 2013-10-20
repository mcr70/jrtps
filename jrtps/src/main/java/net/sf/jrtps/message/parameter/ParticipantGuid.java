package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GUID_t;

public class ParticipantGuid extends Parameter {
	private GUID_t guid;

	public ParticipantGuid(GUID_t guid) {
		this();
		this.guid = guid;
	}
	
	ParticipantGuid() {
		super(ParameterEnum.PID_PARTICIPANT_GUID);
	}

	
	public GUID_t getParticipantGuid() {
		return guid;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.guid = new GUID_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		guid.writeTo(buffer);
	}
	
	public String toString() {
		return super.toString() + ": " + getParticipantGuid();
	}
}