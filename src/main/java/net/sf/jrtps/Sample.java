package net.sf.jrtps;

import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Time;

/**
 * Represents a sample of type T.
 * 
 * @author mcr70
 * 
 * @param <T>
 */
public class Sample<T> {
    private T obj;
    private Time timestamp;
    private StatusInfo sInfo;

    Sample(T obj, Time timestamp, StatusInfo sInfo) {
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

    public String toString() {
        return obj.getClass().getSimpleName() + ", " + sInfo.getChangeKinds();
    }
}
