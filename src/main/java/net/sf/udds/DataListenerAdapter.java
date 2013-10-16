package net.sf.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.types.Time_t;


/**
 * This class is an adapter between DDS DataListener and RTPS DataListener.
 * More specifically, between alt.udds.DataListener and alt.rtps.DataListener.
 * 
 * @author mcr70
 *
 */
class DataListenerAdapter<T> implements net.sf.jrtps.DataListener<T> {
	private DataListener<T> dds_listener;
	
	public DataListenerAdapter(DataListener<T> dds_listener) {
		this.dds_listener = dds_listener;
	}
	
	@Override
	public void onData(T data, Time_t timestamp) {
		List<T> list = new LinkedList<>();
		list.add(data);
		dds_listener.onDataAvailable(list);
	}
}
