package net.sf.jrtps.rtps;

import java.io.IOException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a sample of type T.
 * 
 * @author mcr70
 * 
 * @param <T>
 */
public class Sample<T> {
    private static final Logger log = LoggerFactory.getLogger(Sample.class);

    private final Guid writerGuid;
    private final long seqNum;
    private final StatusInfo sInfo;
    private final long timestamp;
    private final Marshaller<T> marshaller;

    private T obj;      // Sample contains either T or Data, lazily convert to other when needed.
    private Data data;
    private KeyHash keyHash;

    private DataEncapsulation marshalledData;

    private CoherentSet coherentSet;

    private Sample(Guid writerGuid, Marshaller<T> marshaller, long seqNum, long timestamp, StatusInfo sInfo) {
        this.writerGuid = writerGuid;
        this.marshaller = marshaller;
        this.seqNum = seqNum;
        this.sInfo = sInfo;
        this.timestamp = timestamp;        
    }

    public Sample(Guid writerGuid, Marshaller<T> m, long seqNum, long timestamp, ChangeKind kind, T obj) {
        this(writerGuid, m, seqNum, timestamp, new StatusInfo(kind));        
        this.obj = obj;
    }

    public Sample(Guid writerGuid, Marshaller<T> m, long seqNum, long timestamp, Data data) {
        this(writerGuid, m, seqNum, timestamp, data.getStatusInfo());
        this.data = data;
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
     * 
     * @return timestamp in millis.
     */
    public long getTimestamp() {
        return timestamp;
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
     * @return KeyHash, or null if this Sample does not have a key.
     */
    public KeyHash getKey() {
        if (keyHash == null && marshaller.hasKey()) {
            T aData = getData();
            if (aData instanceof DiscoveredData) {
                DiscoveredData dd = (DiscoveredData) aData;
                byte[] builtinTopicKey = dd.getKey().getBytes();
                keyHash = new KeyHash(builtinTopicKey, true);
            }
            else {
                keyHash = new KeyHash(marshaller.extractKey(aData), false);
            }
        }

        return keyHash;
    }

    public ChangeKind getKind() {
        return sInfo.getKind();
    }


    /**
     * Gets the DataEncapsulation.
     * @return DataEncapsulation
     * @throws IOException
     */
    DataEncapsulation getDataEncapsulation() throws IOException {
        if (marshalledData == null) {
            marshalledData = this.marshaller.marshall(getData());
        }

        return marshalledData;
    }

    /**
     * Checks wheter or not this Sample is associated with a Key.
     * @return true or false
     */
    boolean hasKey() {
        return this.marshaller.hasKey();
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
     * @param cs
     */
    public void setCoherentSet(CoherentSet cs) {
        coherentSet = cs;
    }
    
    public String toString() {
        return "Sample[" + seqNum + "]";
    }
}
