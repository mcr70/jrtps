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
	
	public T getData() {
		return obj;
	}
	
	public long getTimestamp() {
		return timestamp.timeMillis();
	}
	
	public boolean isDisposed() {
		return sInfo.isDisposed();
	}
	
	public boolean isUnregistered() {
		return sInfo.isUnregistered();
	}
	
	public String toString() {
		return obj.getClass().getSimpleName() + ", " + sInfo.getChangeKinds();
	}
}
