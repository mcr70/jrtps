package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.HashMap;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a sample of type T.
 * 
 * @author mcr70
 * 
 * @param <T> Type of Sample
 */
public class Sample<T> implements Cloneable {
    private static final Logger log = LoggerFactory.getLogger(Sample.class);

    private HashMap<String, Object> properties = new HashMap<>();
    private final Guid writerGuid;
    private final Marshaller<T> marshaller;
    private final long seqNum;
    private final long timestamp;
    private final long sourceTimeStamp;
    private final StatusInfo sInfo;

    private T obj;      // Sample contains either T or Data, lazily convert to other when needed.
    private Data data;
    private KeyHash key;

    private DataEncapsulation marshalledData;
    private CoherentSet coherentSet;

    private Sample(Guid writerGuid, Marshaller<T> marshaller, long seqNum, 
            long timestamp, long sourceTimeStamp, StatusInfo sInfo) {
        this.writerGuid = writerGuid;
        this.marshaller = marshaller;
        this.seqNum = seqNum;
        this.timestamp = timestamp;        
        this.sourceTimeStamp = sourceTimeStamp;
        this.sInfo = sInfo;
    }

    /**
     * This constructor is used to create a Sample, that has no content. It is used to pass
     * only inline QoS parameters to remote reader. For example, indicating an end of coherent set.
     * @param seqNum Sequence number
     */
    public Sample(long seqNum) {
        this(null, null, seqNum, System.currentTimeMillis(), System.currentTimeMillis(), (StatusInfo)null);
    }

    /**
     * This constructor is used when adding Sample to UDDSWriterCache.
     * @param writerGuid Guid of the writer
     * @param m Marshaller used 
     * @param seqNum Sequence number
     * @param timestamp Timestamp of this sample
     * @param kind ChangeKind
     * @param obj Object of type T
     */
    public Sample(Guid writerGuid, Marshaller<T> m, long seqNum, long timestamp, ChangeKind kind, T obj) {
        this(writerGuid, m, seqNum, System.currentTimeMillis(), timestamp, new StatusInfo(kind));        
        this.obj = obj;
    }

    /**
     * This constructor is used when adding Sample to UDDSReaderCache.
     * @param writerGuid Guid of the writer
     * @param m Marshaller used
     * @param seqNum Sequence number
     * @param timestamp Timestamp of sample
     * @param sourceTimestamp source timestamp of sample
     * @param data Data, whose payload is decoded into Object of type T
     */
    public Sample(Guid writerGuid, Marshaller<T> m, long seqNum, 
            long timestamp, long sourceTimestamp, Data data) {
        this(writerGuid, m, seqNum, timestamp, sourceTimestamp, data.getStatusInfo());
        this.data = data;
        
        if (data.inlineQosFlag()) {
            ParameterList inlineQos = data.getInlineQos();
            if (inlineQos != null) {
                coherentSet = (CoherentSet) inlineQos.getParameter(ParameterId.PID_COHERENT_SET);
            }
        }
    }


    /**
     * Gets the data associated with this Sample.
     * 
     * @return data
     */
    public T getData() {
        if (obj != null) {
            return obj;
        }

        if (data != null) {
            try {
                obj = marshaller.unmarshall(data.getDataEncapsulation());
            } catch (IOException e) {
                log.warn("Failed to convert Data submessage to java object", e);
            }
            finally {
                data = null; // Try to convert only once
            }
        }

        return obj;
    }

    /**
     * Gets the timestamp associated with this Sample.
     * Time stamp can be either local timestamp, or remote writers timestamp,
     * based on DESTINATION_ORDER QoS policy.
     * 
     * @return timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the sourceTimestamp associated with this Sample. Returns the timestamp
     * set by remote writer. If remote writer did not provide timestamp, it has been
     * set to reception time.
     * 
     * @return source timestamp in milliseconds
     */
    public long getSourceTimeStamp() {
        return sourceTimeStamp;
    }
    
    /**
     * Gets the value of disposeFlag of StatusInfo parameter. StatusInfo
     * parameter is part of Data submessage.
     * 
     * @see StatusInfo
     * @return true, if disposeFlag is set
     */
    public boolean isDisposed() {
        return sInfo.isDisposed();
    }

    /**
     * Gets the value of unregisterFlag of StatusInfo parameter. StatusInfo
     * parameter is part of Data submessage.
     * 
     * @see StatusInfo
     * @return true, if unregisterFlag is set
     */
    public boolean isUnregistered() {
        return sInfo.isUnregistered();
    }

    /**
     * Gets the Guid of the writer that wrote this Sample originally.
     * @return Guid of the writer
     */
    public Guid getWriterGuid() {
        return writerGuid;
    }



    /**
     * Gets the sequence number of this Sample.
     * 
     * @return sequence number
     */
    public long getSequenceNumber() {
        return seqNum;
    }

    /**
     * Gets the key of this Sample. Key of the Sample is used to distinguish between
     * instances, when transmitting Samples over the wire.
     * 
     * @return Key, or null if this Sample does not have a key.
     */
    public KeyHash getKey() {
        if (key == null && marshaller != null && marshaller.hasKey()) {
            T aData = getData();
            key = new KeyHash(marshaller.extractKey(aData));
        }

        return key;
    }

    /**
     * Get the ChangeKind of this Sample.
     * @return ChangeKind May be null, if this Sample does not represent a change to an instance.
     */
    public ChangeKind getKind() {
        if (sInfo != null) {
            return sInfo.getKind();
        }
        
        return null;
    }


    /**
     * Gets the DataEncapsulation.
     * @return DataEncapsulation
     * @throws IOException
     */
    DataEncapsulation getDataEncapsulation() throws IOException {
        if (marshalledData == null && marshaller != null) {
            marshalledData = marshaller.marshall(getData());
        }

        return marshalledData;
    }

    /**
     * Checks whether or not this Sample is associated with a Key.
     * @return true or false
     */
    boolean hasKey() {
        if (marshaller != null) {
            return marshaller.hasKey();
        }
        
        return false;
    }

    /**
     * Return CoherentSet attribute of this Sample, if it exists.
     * @return CoherentSet, or null if one has not been set
     */
    public CoherentSet getCoherentSet() {
        return coherentSet;
    }

    /**
     * Sets a CoherentSet attribute for this Sample.
     * @param cs CoherentSet to set
     */
    public void setCoherentSet(CoherentSet cs) {
        coherentSet = cs;
    }
    
    public String toString() {
        return "Sample[" + seqNum + "]:" + sInfo;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Sample<?> s = (Sample<?>) super.clone();
        s.properties = new HashMap<>(this.properties);
        
        return s;
    }
}
