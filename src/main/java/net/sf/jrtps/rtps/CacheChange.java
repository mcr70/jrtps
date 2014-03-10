package net.sf.jrtps.rtps;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;

/**
 * This class represents a sample in history cache.
 * 
 * @author mcr70
 */
public class CacheChange implements Comparable<CacheChange> {
    private final long sequenceNumber;
    private final Object data;
    private final Kind kind;
    private final long timeStamp;
    private final int hashCode;
    private final KeyHash keyHash;

    private Marshaller marshaller;
    private DataEncapsulation marshalledData;

    private static MessageDigest md5 = null;
    private static NoSuchAlgorithmException noSuchAlgorithm = null;
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
    public CacheChange(Marshaller m, Kind kind, long seqNum, Object data) {
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
    public CacheChange(Marshaller m, Kind kind, long seqNum, Object data, long timeStamp) {
        this.kind = kind;
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

    public Object getData() {
        return data;
    }

    public DataEncapsulation getDataEncapsulation() throws IOException {
        if (marshalledData == null) {
            marshalledData = marshaller.marshall(data);
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
        return marshaller.hasKey();
    }

    public KeyHash getKey() {
        return keyHash;
    }

    private KeyHash extractKey() {
        byte[] key = marshaller.extractKey(data);
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
        return kind;
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
    public int compareTo(CacheChange o) {
        return (int) (sequenceNumber - o.sequenceNumber);
    }

    public String toString() {
        return "change: " + sequenceNumber;
    }

}
