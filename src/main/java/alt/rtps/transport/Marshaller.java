package alt.rtps.transport;

import alt.rtps.message.Data;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Time_t;

public abstract class Marshaller<T> {
	public Message toMessage(GuidPrefix_t prefix, T data) {
		Message m = new Message(prefix);

		InfoTimestamp iTime = new InfoTimestamp(new Time_t((int)System.currentTimeMillis(), (int)System.nanoTime()));
		m.addSubMessage(iTime);
		m.addSubMessage(marshall(data));

		return m;
	}

	public abstract T unmarshall(RTPSByteBuffer bb);
	public abstract Data marshall(T data);
}
