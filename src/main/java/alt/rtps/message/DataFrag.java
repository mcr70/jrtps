package alt.rtps.message;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterEnum;
import alt.rtps.message.parameter.ParameterFactory;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.SequenceNumber_t;


/**
 * 
 * @author mcr70
 * @see 8.3.7.3 DataFrag
 */
public class DataFrag extends SubMessage {
	public static final int KIND = 0x16;
	
	private short extraFlags;
	private EntityId_t readerId;
	private EntityId_t writerId;
	private SequenceNumber_t writerSN;
	private int fragmentStartingNum;
	private int fragmentsInSubmessage;
	private int fragmentSize;
	private int sampleSize;

	private List<Parameter> parameterList = new LinkedList<Parameter>();
	private byte[] serializedPayload;

	
	public DataFrag(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}


	public boolean inlineQosFlag() {
		return (header.flags & 0x2) != 0;
	}

	public boolean keyFlag() {
		return (header.flags & 0x4) != 0;
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

	public int getFragmentStartingNumber() {
		return fragmentStartingNum;
	}
	
	public int getFragmentsInSubmessage() {
		return fragmentsInSubmessage;
	}
	
	public int getFragmentSize() {
		return fragmentSize;
	}
	
	public int getSampleSize() { // getDataSize()
		return sampleSize;
	}
	
	public List<Parameter> getParameterList() {
		return parameterList;
	}
	
	public byte[] getSerializedPayload() {
		return serializedPayload;
	}
	
	
	private void readMessage(RTPSByteBuffer bb) {
		int start_count = bb.position(); // start of bytes read so far from the beginning
		
		this.extraFlags = (short) bb.read_short();
		int octetsToInlineQos = bb.read_short() & 0xffff;
		
		int currentCount = bb.position(); // count bytes to inline qos

		this.readerId = EntityId_t.readEntityId(bb);
		this.writerId = EntityId_t.readEntityId(bb);		
		this.writerSN = new SequenceNumber_t(bb);
		
		this.fragmentStartingNum = bb.read_long(); // ulong
		this.fragmentsInSubmessage = bb.read_short(); // ushort
		this.fragmentSize = bb.read_short(); // ushort
		this.sampleSize = bb.read_long(); // ulong
		
		int bytesRead = bb.position() - currentCount;
		int unknownOctets = octetsToInlineQos - bytesRead;
		
		for (int i = 0; i < unknownOctets; i++) {
			System.out.println("SKIP");
			bb.read_octet(); // Skip unknown octets, @see 9.4.5.3.3 octetsToInlineQos
		}
		
		if (inlineQosFlag()) {
			readParameterList(bb);
		}
		
		// TODO: alignment
		int end_count = bb.position(); // end of bytes read so far from the beginning

		this.serializedPayload = new byte[header.submessageLength - (end_count-start_count)];
		bb.read(serializedPayload);
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
			Parameter param = ParameterFactory.readParameter(bb);
			parameterList.add(param);
			if (param.getParameterId() == ParameterEnum.PID_SENTINEL) {
				break; // TODO: Add some control token to CDRInputStream that counts bytes read and 
				       //       fails if expected_read_count+1 is reached 
			}
		}
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_short(extraFlags);
		
		short octets_to_inline_qos = EntityId_t.LENGTH + EntityId_t.LENGTH + SequenceNumber_t.LENGTH + 
				4 + 2 + 2 + 4;  
		buffer.write_short(octets_to_inline_qos);

		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		writerSN.writeTo(buffer);
		
		buffer.write_long(fragmentStartingNum);
		buffer.write_short((short) fragmentsInSubmessage);
		buffer.write_short((short) fragmentSize);
		buffer.write_long(sampleSize);
		
		if (inlineQosFlag()) {
			writeParameterList(buffer);
		}		

		buffer.write(serializedPayload); // TODO: check this
	}


	private void writeParameterList(RTPSByteBuffer buffer) {
		for (Parameter param: parameterList) {
			param.writeTo(buffer); 
		}
		
		// TODO: last Parameter must be PID_SENTINEL
	}
}
