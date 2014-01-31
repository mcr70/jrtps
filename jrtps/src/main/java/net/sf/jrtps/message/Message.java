package net.sf.jrtps.message;


import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.Experimental;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a RTPS message.
 * 
 * @author mcr70
 *
 */
public class Message {
	private static final Logger log = LoggerFactory.getLogger(Message.class);
	
	private Header header;
	private List<SubMessage> submessages = new LinkedList<SubMessage>();
	
	/**
	 * Constructor.
	 * @param prefix
	 */
	public Message(GuidPrefix prefix) {
		header = new Header(prefix);
	}
	
	/**
	 * Constructs a Message from given RTPSByteBuffer.
	 * 
	 * @param bb
	 * @throws IOException
	 */
	public Message(RTPSByteBuffer bb) {
		header = new Header(bb); 
		
		while(bb.getBuffer().hasRemaining()) {
			try {
				bb.align(4);
				int smhPosition = bb.position();
			
				SubMessageHeader smh = new SubMessageHeader(bb);
				int smStart = bb.position();
				
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
				
				log.trace("SubMsg in:  {}", sm);
				submessages.add(sm);
			}
			catch(BufferUnderflowException bue) {
				log.warn("Buffer underflow");
				break;
			}
		}
	}

	/**
	 * Gets all the SubMessages with given SubMessage.Kind.
	 * 
	 * @param kind
	 * @return a List of SubMessages of given Kind, or an empty List if none was found.
	 */
	public List<SubMessage> getSubMessage(SubMessage.Kind kind) {
		List<SubMessage> list = new LinkedList<>();
		for (SubMessage sm: submessages) {
			if (sm.getKind() == kind) {
				list.add(sm);
			}
		}
		
		return list;
	}

	/**
	 * Gets the Header of this Message.
	 * @return Header
	 */
	public Header getHeader() {
		return header;
	}
	
	/**
	 * Gets all the SubMessages of this Message.
	 * 
	 * @return List<SubMessages>. Returned List is never null.
	 */
	public List<SubMessage> getSubMessages() {
		return submessages;
	}

	
	/**
	 * Writes this Message to given RTPSByteBuffer.
	 * During writing of each SubMessage, its length is calculated and SubMessageHeader.submessageLength
	 * is updated. If an overflow occurs during writing, buffer position is set 
	 * to the start of submessage that caused overflow.
	 * 
	 * @param buffer
	 * @return true, if an overflow occured during write.
	 */
	public boolean writeTo(RTPSByteBuffer buffer) {
		header.writeTo(buffer);
		boolean overFlowed = false;
		
		int position = 0;
		int subMessageCount = 0;
		for (SubMessage msg : submessages) {
			int subMsgStartPosition = buffer.position();

			try {
				SubMessageHeader hdr = msg.getHeader();
				buffer.align(4);
				buffer.setEndianess(hdr.endiannessFlag()); // Set the endianess
				hdr.writeTo(buffer);
				
				subMessageCount++;
				
				position = buffer.position();  
				msg.writeTo(buffer);
				int subMessageLength = buffer.position() - position;

				// Position to 'submessageLength' -2 is for short (2 bytes)
				// buffers current position is not changed
				buffer.getBuffer().putShort(position-2, (short) subMessageLength);

				log.trace("SubMsg out: {}", msg);
			}
			catch(BufferOverflowException boe) {
				log.warn("Buffer overflow occured after {} succesful sub-message writes, dropping rest of the sub messages",
						subMessageCount);
				buffer.getBuffer().position(subMsgStartPosition);
				overFlowed = true; 
				break;
			}			
		}
		
		// Length of last submessage is 0, @see 8.3.3.2.3 submessageLength
		if (subMessageCount > 0) {
			buffer.getBuffer().putShort(position-2, (short)0);
		}
		
		return overFlowed;
	}

	
	/**
	 * Writes this message to given RTPSByteBuffer. While writing message to buffer, successfully
	 * written SubMessages are removed from this message. This makes it possible to write this
	 * same message again to another buffer without duplicating submessages to recipient.
	 * 
	 * @param buffer
	 * @return true, if all the SubMessages were drained to given buffer.
	 */
	@Experimental("drainTo() is experimental, use writeTo() instead")
	public boolean drainTo(RTPSByteBuffer buffer) {
		header.writeTo(buffer);
		
		// TODO: handle situation, where no submessages was not written. This will cause endless loop.
		//       Maybe mark the whole message illegal, or just remove first submessage and log a warning.
		
		// TODO: Handle receiver state affecting SubMessages, like InfoDestination 
		
		int position = 0;
		int subMessageCount = 0;
		Iterator<SubMessage> i = submessages.iterator();
		while(i.hasNext()) {
			SubMessage msg = i.next();
			int subMsgStartPosition = buffer.position();

			SubMessageHeader hdr = msg.getHeader();
			buffer.align(4);
			buffer.setEndianess(hdr.endiannessFlag()); // Set the endianess

			try {
				hdr.writeTo(buffer);
				i.remove();

				subMessageCount++;
				
				position = buffer.position();  
				msg.writeTo(buffer);
				int subMessageLength = buffer.position() - position;

				// Position to 'submessageLength' -2 is for short (2 bytes)
				// buffers current position is not changed
				if (subMessageLength > Short.MAX_VALUE) {
					// Length of submessage is 0, if its size exceeds Short.MAX_VALUE. @see 8.3.3.2.3 submessageLength
					buffer.getBuffer().putShort(position-2, (short) 0);
					break;  // Also, it has to be the last SubMessage
				}
				else {
					buffer.getBuffer().putShort(position-2, (short) subMessageLength);
				}
			}
			catch(BufferOverflowException boe) {
				log.warn("Buffer overflow occured after {} succesful sub-message writes, dropping rest of the sub messages",
						subMessageCount);
				buffer.getBuffer().position(subMsgStartPosition); 
				break;
			}			
		}

		return submessages.size() == 0;
	}
	

	/**
	 * Joins two messages together. SubMessages of the other Message are appended to sub messages of this Message.
	 * Before append, an InfoDestination is inserted if the default GuidPrefix from Messages headers differ.  
	 * 
	 * @param other
	 */
	public void join(Message other) {
		GuidPrefix otherPrefix = other.getHeader().getGuidPrefix();
		if (!header.getGuidPrefix().equals(otherPrefix)) {
			submessages.add(new InfoDestination(otherPrefix));
		}
		
		submessages.addAll(other.getSubMessages());
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
