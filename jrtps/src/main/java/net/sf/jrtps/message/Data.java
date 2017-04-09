package net.sf.jrtps.message;

import java.nio.ByteBuffer;

import net.sf.jrtps.message.parameter.ContentFilterInfo;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Submessage notifies the RTPS Reader of a change to a data-object
 * belonging to the RTPS Writer. The possible changes include both changes in
 * value as well as changes to the lifecycle of the data-object.
 * 
 * see 8.3.7.2
 * 
 * @author mcr70
 * 
 */
public class Data extends SubMessage {
    public static final int KIND = 0x15;

    private static final Logger log = LoggerFactory.getLogger(Data.class);

    private short extraFlags = 0;
    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumber writerSN;
    private ParameterList inlineQosParams;
    private DataEncapsulation dataEncapsulation;

    /**
     * Constructor for creating a Data message.
     * 
     * @param readerId EntityId of the reader
     * @param writerId EntityId of the writer
     * @param seqNum Sequence number of the Data submessage
     * @param inlineQosParams Inline QoS parameters. May be null.
     * @param dEnc If null, neither dataFlag or keyFlag is set
     */
    public Data(EntityId readerId, EntityId writerId, long seqNum, ParameterList inlineQosParams, DataEncapsulation dEnc) {
        super(new SubMessageHeader(KIND));

        this.readerId = readerId;
        this.writerId = writerId;
        this.writerSN = new SequenceNumber(seqNum);

        if (inlineQosParams != null && inlineQosParams.size() > 0) {
            header.flags |= 0x2;
            this.inlineQosParams = inlineQosParams;
        }

        if (dEnc != null) {
            if (dEnc.containsData()) {
                header.flags |= 0x4; // dataFlag
            } else {
                header.flags |= 0x8; // keyFlag
            }
        }
        
        this.dataEncapsulation = dEnc;
    }

    /**
     * Constructor to read Data sub-message from RTPSByteBuffer.
     * 
     * @param smh
     * @param bb
     */
    Data(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        if (dataFlag() && keyFlag()) {
            // Should we just ignore this message instead
            throw new IllegalStateException(
                    "This version of protocol does not allow Data submessage to contain both serialized data and serialized key (9.4.5.3.1)");
        }

        int start_count = bb.position(); // start of bytes read so far from the
        // beginning

        this.extraFlags = (short) bb.read_short();
        int octetsToInlineQos = bb.read_short() & 0xffff;

        int currentCount = bb.position(); // count bytes to inline qos

        this.readerId = EntityId.readEntityId(bb);
        this.writerId = EntityId.readEntityId(bb);
        this.writerSN = new SequenceNumber(bb);

        int bytesRead = bb.position() - currentCount;
        int unknownOctets = octetsToInlineQos - bytesRead;

        for (int i = 0; i < unknownOctets; i++) {
            // TODO: Instead of looping, we should do just
            // newPos = bb.getBuffer.position() + unknownOctets or something
            // like that
            bb.read_octet(); // Skip unknown octets, @see 9.4.5.3.3
            // octetsToInlineQos
        }

        if (inlineQosFlag()) {
            log.trace("Reading inline QoS");
            this.inlineQosParams = new ParameterList(bb);
        }

        if (dataFlag() || keyFlag()) {
            bb.align(4); // Each submessage is aligned on 32-bit boundary, @see
            // 9.4.1 Overall Structure
            int end_count = bb.position(); // end of bytes read so far from the
            // beginning

            byte[] serializedPayload = null;
            if (header.submessageLength != 0) {
                serializedPayload = new byte[header.submessageLength - (end_count - start_count)];
            } else { // SubMessage is the last one. Rest of the bytes are read.
                // @see 8.3.3.2.3
                ByteBuffer buffer = bb.getBuffer();
                serializedPayload = new byte[buffer.capacity() - buffer.position()];
            }

            log.trace("Serialized payload starts at {}, {} bytes", end_count, serializedPayload.length);
            bb.read(serializedPayload);
            dataEncapsulation = DataEncapsulation.createInstance(serializedPayload);
        }
    }

    /**
     * Indicates to the Reader the presence of a ParameterList containing QoS
     * parameters that should be used to interpret the message.
     * 
     * @return true, if inlineQos flag is set
     */
    public boolean inlineQosFlag() {
        return (header.flags & 0x2) != 0;
    }

    /**
     * Gets the inlineQos parameters if present. Inline QoS parameters are
     * present, if inlineQosFlag() returns true.
     * 
     * @see #inlineQosFlag()
     * @return InlineQos parameters, or null if not present
     */
    public ParameterList getInlineQos() {
        return inlineQosParams;
    }


    /**
     * Indicates to the Reader that the dataPayload submessage element contains
     * the serialized value of the data-object.
     * 
     * @return true, data flag is set
     */
    public boolean dataFlag() {
        return (header.flags & 0x4) != 0;
    }

    /**
     * Indicates to the Reader that the dataPayload submessage element contains
     * the serialized value of the key of the data-object.
     * 
     * @return true, if key flag is set
     */
    public boolean keyFlag() {
        return (header.flags & 0x8) != 0;
    }

    /**
     * Identifies the RTPS Reader entity that is being informed of the change to
     * the data-object.
     * 
     * @return EntityId_t of the reader
     */
    public EntityId getReaderId() {
        return readerId;
    }

    /**
     * Identifies the RTPS Writer entity that made the change to the
     * data-object.
     * 
     * @return EntityId_t of the writer
     */
    public EntityId getWriterId() {
        return writerId;
    }

    /**
     * Uniquely identifies the change and the relative order for all changes
     * made by the RTPS Writer identified by the writerGuid. Each change gets a
     * consecutive sequence number. Each RTPS Writer maintains is own sequence
     * number.
     * 
     * @return sequence number
     */
    public long getWriterSequenceNumber() {
        return writerSN.getAsLong();
    }

    public short getExtraFlags() {
        return extraFlags;
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_short(extraFlags);

        short octets_to_inline_qos = 4 + 4 + 8;// EntityId.LENGTH + EntityId.LENGTH + SequenceNumber.LENGTH;
        bb.write_short(octets_to_inline_qos);

        readerId.writeTo(bb);
        writerId.writeTo(bb);
        writerSN.writeTo(bb);

        if (inlineQosFlag()) {
            inlineQosParams.writeTo(bb);
        }

        if (dataFlag() || keyFlag()) {
            bb.align(4);
            bb.write(dataEncapsulation.getSerializedPayload());
        }
    }

    /**
     * Gets the DataEncapsulation.
     * @return DataEncapsulation
     */
    public DataEncapsulation getDataEncapsulation() {
        return dataEncapsulation;
    }

    /**
     * Get the StatusInfo (PID 0x0071) inline QoS parameter if it is present. If inline Qos
     * is not present, an empty(default) StatusInfo is returned
     * 
     * @return StatusInfo
     */
    public StatusInfo getStatusInfo() {
        StatusInfo sInfo = null;
        if (inlineQosFlag()) {
            sInfo = (StatusInfo) inlineQosParams.getParameter(ParameterId.PID_STATUS_INFO);
        }

        if (sInfo == null) {
            sInfo = new StatusInfo(); // return empty StatusInfo (WRITE)
        }

        return sInfo;
    }

    /**
     * Gets the ContentFilterInfo (PID 0x0055) inline qos parameter if present.
     * @return ContentFilterInfo, or null if one was not present
     */
    public ContentFilterInfo getContentFilterInfo() {
    	ContentFilterInfo cfi = null;
    	if (inlineQosFlag()) {
    		cfi = (ContentFilterInfo) inlineQosParams.getParameter(ParameterId.PID_CONTENT_FILTER_INFO);
    	}
    	
    	return cfi;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append(", readerId: ").append(getReaderId());
        sb.append(", writerId: ").append(getWriterId());
        sb.append(", writerSN: ").append(writerSN);

        if (inlineQosFlag()) {
            sb.append(", inline QoS: ").append(inlineQosParams);
        }

        return sb.toString();
    }
}
