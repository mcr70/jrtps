package net.sf.jrtps;

import net.sf.jrtps.types.Time_t;

public interface DataListener<T> {
	public void onData(T data, Time_t timestamp);
}
