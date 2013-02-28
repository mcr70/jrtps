package alt.rtps.message;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterEnum;
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

	private List<Parameter> inlineQosParams = new LinkedList<Parameter>();
	private byte[] serializedPayload;

	/**
	 * Constructor for creating a Data message used by SPDPbuiltinParticipantData.
	 * 
	 * @param readerId
	 * @param writerId
	 * @param seqNum
	 * @param participantGuid
	 * @param endpointset
	 * @param payloadParams2 
	 * @param defaultUnicastLocator
	 * @param metatrafficUnicastLocator
	 * @param defaultMulticastLocator
	 * @param metatrafficMulticastLocator
	 * @param leaseDuration
	 */
	public Data(EntityId_t readerId, EntityId_t writerId, long seqNum,
			GUID_t participantGuid, int endpointset, List<Parameter> inlineQosParams, List<Parameter> payloadParams) {
		
		super(new SubMessageHeader(0x15));
		
		this.readerId = readerId;
		this.writerId = writerId;
		this.writerSN = new SequenceNumber_t(seqNum);
		
		if (inlineQosParams != null && inlineQosParams.size() > 0) {
			header.flags |= 0x2;
			this.inlineQosParams = inlineQosParams;
		}
		
		header.flags |= 0x4; // dataFlag
				
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		buffer.order(ByteOrder.LITTLE_ENDIAN);

		RTPSByteBuffer payload = new RTPSByteBuffer(buffer);
		
		payload.write_octet((byte) 0); // encapsulation header
		payload.write_octet((byte) 3); // 0x0002 = PL_CDR_LE (little endian parameterlist)
		payload.write_short(0); // u_short options, not recognized
		
		writeParameterList(payloadParams, payload);
			
		payload.align(4);
		
		serializedPayload = new byte[payload.position()];
		System.arraycopy(payload.getBuffer().array(), 0, serializedPayload, 0, serializedPayload.length);
		//printPayload();
		
		RTPSByteBuffer bb = new RTPSByteBuffer(serializedPayload);
		bb.read_octet(); bb.read_octet();		
	}
	
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
			//System.out.println("Data: skip unknown octets");
			bb.read_octet(); // Skip unknown octets, @see 9.4.5.3.3 octetsToInlineQos
		}
		
		if (inlineQosFlag()) {
			readParameterList(bb);
		}
		
		if (dataFlag() || keyFlag()) { 
			bb.align(4); // Each submessage is aligned on 32-bit boundary, @see 9.4.1 Overall Structure
			int end_count = bb.position(); // end of bytes read so far from the beginning

			if (header.submessageLength != 0) {
				this.serializedPayload = new byte[header.submessageLength - (end_count-start_count)];
			}
			else { // SubMessage is the last one. Rest of the bytes are read. @see 8.3.3.2.3
				ByteBuffer buffer = bb.getBuffer();
				//log.debug(buffer.limit() + ", " + buffer.capacity() + ", " + buffer.position());
				this.serializedPayload = new byte[buffer.capacity() - buffer.position()];
			}
			
			bb.read(serializedPayload);
		}
	}
	


	/**
	 * 
	 * @param bb
	 * @throws IOException 
	 * @see 9.4.2.11 ParameterList
	 */
	private void readParameterList(RTPSByteBuffer bb) {
		
		while (true) {
			bb.align(4);
			Parameter param = Parameter.readParameter(bb);
			//System.out.println("Read param " + param + ", endianess " + header.endianessFlag());
			inlineQosParams.add(param);
			if (param.getParameterId() == ParameterEnum.PID_SENTINEL) {
				break; // TODO: Add some control token to CDRInputStream that counts bytes read and 
				       //       fails if expected_read_count+1 is reached 
			}
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

	public List<Parameter> getParameters() {
		return inlineQosParams;
	}
	
	/**
	 * Get the KeyHash parameter if it is present.
	 * @return return KeyHash or null, if it not found from inline params
	 */
	public KeyHash getKeyHashParameter() {
		for (Parameter param: getParameters()) {
			if (param.getParameterId() == ParameterEnum.PID_KEY_HASH) {
				return (KeyHash) param; 
			}
		}
		
		return null;
	}
	
	/**
	 * Return the serialized payload attached to this Data message.
	 * first two bytes of the serialized payload contains Encapsulation identifier of the data.
	 * 
	 * @return
	 */
	public byte[] getSerializedPayload() {
		return serializedPayload;
	}
	
	public RTPSByteBuffer getSerializedPayloadInputStream() {
		RTPSByteBuffer bb = null;
		
		if (serializedPayload == null) {
			return new RTPSByteBuffer(new byte[0]);
		}
		
		// Check encapsulation header. @see table 10.1
		if (serializedPayload[0] == 0 && serializedPayload[1] <= 3) { // known encapsulation header
			boolean littleEndian = (serializedPayload[1] & 0x01) == 0x01;
			
			bb = new RTPSByteBuffer(serializedPayload);
			if (littleEndian) {
				bb.getBuffer().order(ByteOrder.LITTLE_ENDIAN);
			}
			else {
				bb.getBuffer().order(ByteOrder.BIG_ENDIAN);
			}
		}
		else {
			bb = new RTPSByteBuffer(serializedPayload);
		}
		
		bb.read_octet(); bb.read_octet(); // u_short options, not recognized
		
		return bb;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer(super.toString());
		sb.append(", readerId: " + getReaderId());
		sb.append(", writerId: " + getWriterId());
		sb.append(", writerSN: " + writerSN);
		
		return sb.toString();
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
			writeParameterList(inlineQosParams, buffer);
		}		
		if (dataFlag() || keyFlag()) { 
			buffer.align(4);
			buffer.write(serializedPayload); // TODO: check this
		}
	}

	private void writeParameterList(List<Parameter> params, RTPSByteBuffer buffer) {
		for (Parameter param: params) {
			buffer.align(4); // @see 9.4.2.11
			//System.out.println("Data: writeParamList: " + param.getParameterId());
			buffer.write_short(param.getParameterId().kind());
			
			//if (param.getParameterId() != ParameterEnum.PID_SENTINEL) {
			if (true) { // TODO: Sentinel handling
				buffer.write_short(0); // length will be calculated
			
				int pos = buffer.position();
				param.writeTo(buffer); 
				int paramLength = buffer.position() - pos;
				paramLength += (paramLength % 4); // Make sure length is multiple of 4
				
				buffer.getBuffer().putShort(pos - 2, (short) paramLength);
			}
		}
		
		// TODO: last Parameter must be PID_SENTINEL
	}


	private void printPayload() {
		System.out.print("      ");
		for (int i = 0; i < serializedPayload.length; i++) {
			
			System.out.print("0x" + String.format("%02x", serializedPayload[i]) + " ");
			if(i % 16 == 15) {
				System.out.println();
				System.out.print("      ");
			}
		}
		System.out.println();
	}

}
