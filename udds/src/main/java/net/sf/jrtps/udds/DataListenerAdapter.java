package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Time_t;


/**
 * This class is an adapter between DDS DataListener and RTPS DataListener.
 * More specifically, between net.sf.udds.DataListener and net.sf.jrtps.DataListener.
 * 
 * @author mcr70
 *
 */
class DataListenerAdapter<T> implements net.sf.jrtps.DataListener<T> {
	DataListener<T> udds_listener;
	
	public DataListenerAdapter(DataListener<T> dds_listener) {
		this.udds_listener = dds_listener;
	}
	
	@Override
	public void onData(T data, Time_t timestamp, StatusInfo sInfo) {
		List<T> list = new LinkedList<>();
		list.add(data);
		if (sInfo.isDisposed()) {
			udds_listener.onDataDisposed(list);
		}
		else {
			udds_listener.onDataAvailable(list);
		}
	}
}
