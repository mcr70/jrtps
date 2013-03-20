package alt.rtps.message;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.ParameterList;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.SequenceNumber_t;


/**
 * This Submessage notifies the RTPS Reader of a change to a data-object belonging to the RTPS Writer. 
 * The possible changes include both changes in value as well as changes to the lifecycle of the data-object.
 * 
 * @author mcr70
 * @see 8.3.7.2
 */
public class Data extends SubMessage {
	public static final int KIND = 0x15;
	
	private static final Logger log = LoggerFactory.getLogger(Data.class);
	
	private short extraFlags = 0;
	/**
	 * Identifies the RTPS Reader entity that is being informed of the change to the data-object.
	 */
	private EntityId_t readerId;
	/**
	 * Identifies the RTPS Writer entity that made the change to the data-object.
	 */
	private EntityId_t writerId;
	/**
	 * Uniquely identifies the change and the relative order for all changes made by the RTPS Writer 
	 * identified by the writerGuid. Each change gets a consecutive sequence number. Each RTPS
	 * Writer maintains is own sequence number.
	 */
	private SequenceNumber_t writerSN;

	//private List<Parameter> inlineQosParams = new LinkedList<Parameter>();
	private ParameterList inlineQosParams;
	private DataEncapsulation dataEncapsulation;

	
	
	
	/**
	 * Constructor for creating a Data message used by SPDPbuiltinParticipantData.
	 * 
	 * @param readerId
	 * @param writerId
	 * @param seqNum
	 * @param participantGuid
	 * @param inlineQosParams Inline QoS parameters. May be null.
	 * @param payloadParams
	 */
	public Data(EntityId_t readerId, EntityId_t writerId, long seqNum,
			GUID_t participantGuid, ParameterList inlineQosParams, DataEncapsulation dEnc) {
		
		super(new SubMessageHeader(0x15));
		
		this.readerId = readerId;
		this.writerId = writerId;
		this.writerSN = new SequenceNumber_t(seqNum);
		
		if (inlineQosParams != null && inlineQosParams.size() > 0) {
			header.flags |= 0x2;
			this.inlineQosParams = inlineQosParams;
		}

		if (dEnc.containsData()) {
			header.flags |= 0x4; // dataFlag	
		}
		else {
			header.flags |= 0x8; // keyFlag
		}

		this.dataEncapsulation = dEnc;
	}
	
	/**
	 * Constructor to read Data sub-message from RTPSByteBuffer.
	 * 
	 * @param smh
	 * @param bb
	 */
	public Data(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		if (dataFlag() && keyFlag()) {
			// Should we just ignore this message instead
			throw new IllegalStateException("This version of protocol does not allow Data submessage to contain both serialized data and serialized key (9.4.5.3.1)");
		}
		
		readMessage(bb);
	}
	
	public boolean inlineQosFlag() {
		return (header.flags & 0x2) != 0;
	}

	public boolean dataFlag() {
		return (header.flags & 0x4) != 0;
	}

	public boolean keyFlag() {
		return (header.flags & 0x8) != 0;
	}
	
	
	/**
	 * @see 9.4.5.3
	 */
	private void readMessage(RTPSByteBuffer bb) {
		int start_count = bb.position(); // start of bytes read so far from the beginning
		
		this.extraFlags = (short) bb.read_short();
		int octetsToInlineQos = bb.read_short() & 0xffff;
		
		int currentCount = bb.position(); // count bytes to inline qos

		this.readerId = EntityId_t.readEntityId(bb);
		this.writerId = EntityId_t.readEntityId(bb);
		this.writerSN = new SequenceNumber_t(bb);
				
		int bytesRead = bb.position() - currentCount;
		int unknownOctets = octetsToInlineQos - bytesRead;
		
		for (int i = 0; i < unknownOctets; i++) {
			bb.read_octet(); // Skip unknown octets, @see 9.4.5.3.3 octetsToInlineQos
		}
		
		if (inlineQosFlag()) {
			this.inlineQosParams = new ParameterList(bb);
		}
		
		if (dataFlag() || keyFlag()) { 
			bb.align(4); // Each submessage is aligned on 32-bit boundary, @see 9.4.1 Overall Structure
			int end_count = bb.position(); // end of bytes read so far from the beginning
			
			byte[] serializedPayload = null;
			if (header.submessageLength != 0) {
				serializedPayload = new byte[header.submessageLength - (end_count-start_count)];
			}
			else { // SubMessage is the last one. Rest of the bytes are read. @see 8.3.3.2.3
				ByteBuffer buffer = bb.getBuffer();
				serializedPayload = new byte[buffer.capacity() - buffer.position()];
			}
			
			bb.read(serializedPayload);
			dataEncapsulation = DataEncapsulation.createInstance(serializedPayload);
		}
	}
	
	public EntityId_t getReaderId() {
		return readerId;
	}

	public EntityId_t getWriterId() {
		return writerId;
	}
	
	public SequenceNumber_t getWriterSequenceNumber() {
		return writerSN;
	}

	

	
	public  short getExtraFlags() {
		return extraFlags;
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_short(extraFlags);
		
		short octets_to_inline_qos = EntityId_t.LENGTH + EntityId_t.LENGTH + SequenceNumber_t.LENGTH;  
		buffer.write_short(octets_to_inline_qos);

		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		writerSN.writeTo(buffer);
		
		if (inlineQosFlag()) {
			inlineQosParams.writeTo(buffer);
		}		

		if (dataFlag() || keyFlag()) { 
			buffer.align(4);
			buffer.write(dataEncapsulation.getSerializedPayload()); // TODO: check this
		}
	}


	public DataEncapsulation getDataEncapsulation() {
		return dataEncapsulation;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(", readerId: " + getReaderId());
		sb.append(", writerId: " + getWriterId());
		sb.append(", writerSN: " + writerSN);
		
		return sb.toString();
	}
}
