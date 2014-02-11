package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * UnknownSubMessage. If an unknown SubMessage is received, it is wrapped in
 * this class. Implementation does not known what to do with it, but rest of the
 * SubMessages are processed.
 * 
 * @author mcr70
 * 
 */
public class UnknownSubMessage extends SubMessage {
    private byte[] bytes;

    public UnknownSubMessage(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    private void readMessage(RTPSByteBuffer bb) {
        bytes = new byte[header.submessageLength];
        bb.read(bytes);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write(bytes);
    }
}
