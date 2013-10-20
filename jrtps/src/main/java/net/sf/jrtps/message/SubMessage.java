package net.sf.jrtps.message;

import java.io.IOException;

import net.sf.jrtps.transport.RTPSByteBuffer;

public abstract class SubMessage {
	
	protected final SubMessageHeader header;
	public enum Kind {
		PAD, ACKNACK, HEARTBEAT, GAP, INFOTIMESTAMP, INFOSOURCE, INFOREPLYIP4,
		INFODESTINATION, INFOREPLY, NACKFRAG, HEARTBEATFRAG, DATA, DATAFRAG, UNKNOWN
	}
	
	// TODO: implement this constructor??? try to use enum instead of int kind
	//protected SubMessage(Kind kind) {		
	//}
	
	protected SubMessage(SubMessageHeader header) {
		this.header = header;
	}
	

	public SubMessageHeader getHeader() {
		return header;
	}

	public Kind getKind() {
		switch(header.kind) {
		case 0x01: return Kind.PAD;
		case 0x06: return Kind.ACKNACK;
		case 0x07: return Kind.HEARTBEAT;
		case 0x08: return Kind.GAP;
		case 0x09: return Kind.INFOTIMESTAMP; 
		case 0x0c: return Kind.INFOSOURCE; 
		case 0x0d: return Kind.INFOREPLYIP4; 
		case 0x0e: return Kind.INFODESTINATION; 
		case 0x0f: return Kind.INFOREPLY; 
		case 0x12: return Kind.NACKFRAG;
		case 0x13: return Kind.HEARTBEATFRAG;
		case 0x15: return Kind.DATA;
		case 0x16: return Kind.DATAFRAG;
		default:
			return Kind.UNKNOWN;
		}
	}
	
	/**
	 * Writes this SubMessage into given ByteBuffer.
	 * 
	 * @param os
	 * @throws IOException 
	 */
	public abstract void writeTo(RTPSByteBuffer buffer);
	

	public String toString() {
		return getClass().getSimpleName() + ":" + header.toString();
	}
}
