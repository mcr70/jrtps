package net.sf.jrtps;

import java.io.IOException;

import net.sf.jrtps.message.data.DataEncapsulation;


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
	private Marshaller marshaller;
	private DataEncapsulation marshalledData;
	
	/**
	 * Enumeration for different changes made to an instance.
	 * @author mcr70
	 */
	public enum Kind {
		WRITE, DISPOSE, UNREGISTER;
	}
	
	/**
	 * Constructor for CacheChange
	 * @param m Marhsaller
	 * @param kind Kind
	 * @param seqNum sequence Number
	 * @param data data
	 */
	public CacheChange(Marshaller m, Kind kind, long seqNum, Object data) {
		this(m, kind, seqNum, data, System.currentTimeMillis());
	}

	/**
	 * Constructor for CacheChange
	 * @param m Marhsaller
	 * @param kind Kind
	 * @param seqNum sequence Number
	 * @param data data
	 * @param timeStamp timestamp to use
	 */
	public CacheChange(Marshaller m, Kind kind, long seqNum, Object data, long timeStamp) {
		this.kind = kind;
		this.sequenceNumber = seqNum;
		this.data = data;
		this.timeStamp = timeStamp;
		this.hashCode = new Long(sequenceNumber).hashCode();
		this.marshaller = m;
	}

	Object getData() {
		return data;
	}

	DataEncapsulation getDataEncapsulation() throws IOException {
		if (marshalledData == null) {
			marshalledData = marshaller.marshall(data);
		}
		
		return marshalledData;
	}
	
	/**
	 * Gets the sequence number of this CacheChange.
	 * @return sequence number
	 */
	public long getSequenceNumber() {
		return sequenceNumber;
	}

	/**
	 * Gets the timestamp of this CacheChange.
	 * @return timestamp
	 */
	long getTimeStamp() {
		return timeStamp;
	}


	boolean hasKey() {
		return marshaller.hasKey();
	}

	byte[] extractKey() {
		return marshaller.extractKey(data);
	}

	Kind getKind() {
		return kind;
	}

	// TODO: should we implement equals() and hashCode(). If so, it would be for sequenceNumber
	@Override
	public boolean equals(Object other) {
		if (other instanceof CacheChange) {
			return sequenceNumber == ((CacheChange)other).sequenceNumber;
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
