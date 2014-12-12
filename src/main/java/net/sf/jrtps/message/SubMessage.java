package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * An abstract Base class for known sub-messages.
 * 
 * @author mcr70
 * 
 */
public abstract class SubMessage {
    protected final SubMessageHeader header;

    /**
     * Different kinds of SubMessages
     */
    public enum Kind {
        PAD, ACKNACK, HEARTBEAT, GAP, 
        INFOTIMESTAMP, INFOSOURCE, INFOREPLYIP4, INFODESTINATION, INFOREPLY, 
        NACKFRAG, HEARTBEATFRAG, DATA, DATAFRAG, 
        SECURESUBMSG, UNKNOWN
    }

    /**
     * Constructor.
     * 
     * @param header
     */
    protected SubMessage(SubMessageHeader header) {
        this.header = header;
    }

    /**
     * Gets the SubMessageHeader.
     * 
     * @return SubMessageHeader
     */
    public SubMessageHeader getHeader() {
        return header;
    }

    /**
     * Gets the Kind of this SubMessage
     * 
     * @return Kind
     */
    public Kind getKind() {
        switch (header.kind) {
        case 0x01:
            return Kind.PAD;
        case 0x06:
            return Kind.ACKNACK;
        case 0x07:
            return Kind.HEARTBEAT;
        case 0x08:
            return Kind.GAP;
        case 0x09:
            return Kind.INFOTIMESTAMP;
        case 0x0c:
            return Kind.INFOSOURCE;
        case 0x0d:
            return Kind.INFOREPLYIP4;
        case 0x0e:
            return Kind.INFODESTINATION;
        case 0x0f:
            return Kind.INFOREPLY;
        case 0x12:
            return Kind.NACKFRAG;
        case 0x13:
            return Kind.HEARTBEATFRAG;
        case 0x15:
            return Kind.DATA;
        case 0x16:
            return Kind.DATAFRAG;
        case 0x30:
            return Kind.SECURESUBMSG;
        default:
            return Kind.UNKNOWN;
        }
    }

    /**
     * Writes this SubMessage into given RTPSByteBuffer.
     * 
     * @param bb
     */
    public abstract void writeTo(RTPSByteBuffer bb);

    public String toString() {
        return getClass().getSimpleName() + ":" + header.toString();
    }
}
