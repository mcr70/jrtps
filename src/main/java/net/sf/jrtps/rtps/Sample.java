package net.sf.jrtps.rtps;

import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.Time;

/**
 * Represents a sample of type T.
 * 
 * @author mcr70
 * 
 * @param <T>
 */
public class Sample<T> {
	private Guid writerGuid;
    private T obj;
    private Time timestamp;
    private StatusInfo sInfo;

    Sample(Guid writerGuid, T obj, Time timestamp, StatusInfo sInfo) {
        this.writerGuid = writerGuid;
		this.obj = obj;
        this.timestamp = timestamp;
        this.sInfo = sInfo;
    }

    /**
     * Gets the data associated with this Sample.
     * 
     * @return data
     */
    public T getData() {
        return obj;
    }

    /**
     * Gets the timestamp associated with this Sample.
     * 
     * @return timestamp in millis.
     */
    public long getTimestamp() {
        return timestamp.timeMillis();
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
    
    
    public String toString() {
        return obj.getClass().getSimpleName() + ", " + sInfo.getChangeKinds();
    }
}
