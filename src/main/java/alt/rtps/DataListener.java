package alt.rtps;

import alt.rtps.types.Time_t;

public interface DataListener<T> {
	public void onData(T data, Time_t timestamp);
}
