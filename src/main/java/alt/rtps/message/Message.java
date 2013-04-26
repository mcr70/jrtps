package alt.rtps.message;


import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GuidPrefix_t;

/**
 * This class represents a RTPS message. It also represents 'the Reveiver' as
 * discussed in 8.3.4, to store the state of incoming message.
 * 
 * @author mcr70
 *
 */
public class Message {
	private static final Logger log = LoggerFactory.getLogger(Message.class);
	
	private Header header;
	private List<SubMessage> submessages = new LinkedList<SubMessage>();
	
	
	public Message(GuidPrefix_t prefix) {
		header = new Header(prefix);
	}
	
	/**
	 * Constructs a Message from given RTPSByteBuffer
	 * 
	 * @param bb
	 * @throws IOException
	 */
	public Message(RTPSByteBuffer bb) {
		header = new Header(bb); 
		log.trace("Reading message, header: {}", header);
		
		while(bb.getBuffer().hasRemaining()) {
			try {
				bb.align(4);
				int smhPosition = bb.position();
			
				SubMessageHeader smh = new SubMessageHeader(bb);
				int smStart = bb.position();
				
				log.trace("SubMessageHeader, starts at {}: {}", smhPosition, smh);

				SubMessage sm = null;

				switch(smh.kind) { // @see 9.4.5.1.1
				case Pad.KIND: sm = new Pad(smh, bb); break;
				case AckNack.KIND: sm = new AckNack(smh, bb); break;
				case Heartbeat.KIND: sm = new Heartbeat(smh, bb); break;
				case Gap.KIND: sm = new Gap(smh, bb); break;
				case InfoTimestamp.KIND: sm = new InfoTimestamp(smh, bb); break;
				case InfoSource.KIND: sm = new InfoSource(smh, bb); break;
				case InfoReplyIp4.KIND: sm = new InfoReplyIp4(smh, bb); break;
				case InfoDestination.KIND: sm = new InfoDestination(smh, bb); break;
				case InfoReply.KIND: sm = new InfoReply(smh, bb); break;
				case NackFrag.KIND: sm = new NackFrag(smh, bb); break;
				case HeartbeatFrag.KIND: sm = new HeartbeatFrag(smh, bb); break;
				case Data.KIND: sm = new Data(smh, bb); break;
				case DataFrag.KIND: sm = new DataFrag(smh, bb); break;

				default:
					sm = new UnknownSubMessage(smh, bb);
				}

				int smEnd = bb.position();
				if (smEnd - smStart != smh.submessageLength && smh.submessageLength != 0) {
					log.warn("SubMessage length differs for {} != {} for {}", smEnd-smStart, smh.submessageLength, sm.getKind());
				}
				else {
					log.debug("SubMessage, position {}, length {} for {}", smEnd, smh.submessageLength, sm.getKind());
				}
				log.trace("{}", sm);
				submessages.add(sm);
			}
			catch(BufferUnderflowException bue) {
				log.warn("Buffer underflow");
				break;
			}
		}
	}

	public SubMessage getSubMessage(SubMessage.Kind kind) {
		for (SubMessage sm: submessages) {
			if (sm.getKind() == kind) {
				return sm;
			}
		}
		
		return null;
	}

	public Header getHeader() {
		return header;
	}
	
	public List<SubMessage> getSubMessages() {
		return submessages;
	}

	
	
	public void writeTo(RTPSByteBuffer buffer) {
		header.writeTo(buffer);

		int position = 0;
		for (SubMessage msg : submessages) {
			int subMsgStartPosition = buffer.position();

			try {
				SubMessageHeader hdr = msg.getHeader();
				buffer.align(4);
				hdr.writeTo(buffer);
				
				position = buffer.position();  
				msg.writeTo(buffer);
				int subMessageLength = buffer.position() - position;

				// Position to 'submessageLength' -2 is for short (2 bytes)
				buffer.getBuffer().putShort(position-2, (short) subMessageLength);
			}
			catch(BufferOverflowException boe) {
				log.warn("Buffer overflow occured, dropping rest of the sub messages");
				buffer.getBuffer().position(subMsgStartPosition);
				break;
			}			
		}
		
		// Length of last submessage is 0, @see 8.3.3.2.3 submessageLength
		buffer.getBuffer().putShort(position-2, (short)0);
	}

	
	/**
	 * Adds a new SubMessage to this Message. SubMessage must well formed.
	 */
	public void addSubMessage(SubMessage sm) {
		submessages.add(sm);
	}

	
	public String toString() { 
		return getHeader() + ", " + getSubMessages();
	}
}
