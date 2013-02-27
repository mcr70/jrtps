package alt.rtps.structure;

import alt.rtps.types.Time_t;

public interface DataListener {
	public void onData(Object data, Time_t timestamp);
}
