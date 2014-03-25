package net.sf.jrtps.rtps;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Guid;

/**
 * This class represents a sample in history cache.
 * 
 * @author mcr70
 */
public class CacheChange<T> implements Comparable<CacheChange<T>> {
    private final long sequenceNumber;
    private final T data;
    //private final Kind kind;
    private final long timeStamp;
    private final int hashCode;
    private final StatusInfo sInfo;
    private final KeyHash keyHash;
    private final Marshaller<T> marshaller;
    
    private DataEncapsulation marshalledData;
    

    private static MessageDigest md5 = null;
    private static NoSuchAlgorithmException noSuchAlgorithm = null;
    private Guid writerGuid;
    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // Actual usage might not even need it.
            noSuchAlgorithm = e;
        }
    }

    /**
     * Enumeration for different changes made to an instance.
     * 
     * @author mcr70
     */
    public enum Kind {
        /**
         * Writer updates an instance.
         */
        WRITE,
        /**
         * Writer disposes an instance.
         */
        DISPOSE,
        /**
         * Writer unregisters an instance.
         */
        UNREGISTER;
    }

    /**
     * Constructor for CacheChange
     * 
     * @param m Marhsaller
     * @param kind Kind
     * @param seqNum sequence Number
     * @param data data
     */
    public CacheChange(Marshaller<T> m, Kind kind, long seqNum, T data) {
        this(m, kind, seqNum, data, System.currentTimeMillis());
    }

    /**
     * Constructor for CacheChange.
     * 
     * @param m Marhsaller
     * @param kind Kind
     * @param seqNum sequence Number
     * @param data data
     * @param timeStamp timestamp to use
     * @throws RuntimeException
     *             if this cache change has a key, and it needs to be coverted
     *             to MD5 hash, but there is no MD5 algorithm available for this
     *             platform.
     */
    public CacheChange(Marshaller<T> m, Kind kind, long seqNum, T data, long timeStamp) {
        //this.kind = kind;
        this.sInfo = new StatusInfo(kind);
        this.sequenceNumber = seqNum;
        this.data = data;
        this.timeStamp = timeStamp;
        this.hashCode = Long.valueOf(sequenceNumber).hashCode();
        this.marshaller = m;

        if (m.hasKey()) {
            this.keyHash = extractKey();
        } else {
            this.keyHash = null;
        }
    }

    
    public CacheChange(Guid writerGuid, Marshaller<T> m, long seqNum, Data data, long timeStamp) throws IOException {
        this.writerGuid = writerGuid;
        this.marshaller = m;
        this.sequenceNumber = seqNum;
        this.timeStamp = timeStamp;
        this.hashCode = Long.valueOf(sequenceNumber).hashCode();
        this.sInfo = data.getStatusInfo();
        this.data = m.unmarshall(data.getDataEncapsulation());
        
        if (m.hasKey()) {
            this.keyHash = extractKey();
        }
        else {
            this.keyHash = null;
        }
    }
    
    public T getData() {
        return data;
    }

    public DataEncapsulation getDataEncapsulation() throws IOException {
        if (marshalledData == null) {
            marshalledData = this.marshaller.marshall(data);
        }

        return marshalledData;
    }

    /**
     * Gets the sequence number of this CacheChange.
     * 
     * @return sequence number
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Gets the timestamp of this CacheChange.
     * 
     * @return timestamp
     */
    public long getTimeStamp() {
        return timeStamp;
    }

    public boolean hasKey() {
        return this.marshaller.hasKey();
    }

    public KeyHash getKey() {
        return keyHash;
    }

    public StatusInfo getStatusInfo() {
        return sInfo;
    }
    
    public Guid getWriterGuid() {
        return writerGuid;
    }
    
    private KeyHash extractKey() {
        byte[] key = this.marshaller.extractKey(data);
        if (key == null) {
            key = new byte[0];
        }

        byte[] bytes = null;
        if (key.length < 16) {
            bytes = new byte[16];
            System.arraycopy(key, 0, bytes, 0, key.length);
        } else {
            if (md5 == null) {
                throw new RuntimeException(noSuchAlgorithm);
            }

            // TODO: multithreading
            bytes = md5.digest(key);
            md5.reset();
        }

        return new KeyHash(bytes);
    }

    public Kind getKind() {
        // TODO: this method should be removed
        if (sInfo.isDisposed()) {
            return Kind.DISPOSE;
        }
        else if (sInfo.isUnregistered()) {
            return Kind.UNREGISTER;
        }
        
        return Kind.WRITE;
    }

    // TODO: should we implement equals() and hashCode(). If so, it would be for
    // sequenceNumber
    @Override
    public boolean equals(Object other) {
        if (other instanceof CacheChange) {
            return sequenceNumber == ((CacheChange) other).sequenceNumber;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public int compareTo(CacheChange<T> o) {
        return (int) (sequenceNumber - o.sequenceNumber);
    }

    public String toString() {
        return "change: " + sequenceNumber + ", " + getKind();
    }
}
