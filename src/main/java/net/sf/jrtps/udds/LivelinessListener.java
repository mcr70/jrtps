package net.sf.jrtps.udds;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.rtps.WriterLivelinessListener;

/**
 * DataReader adds an instance of LivelinessListener to RTPSReader.
 * There is only one liveliness listener per DataReader, but multiple WriterListeners
 * per DataREader. This class acts as a bridge between.
 *  
 * @author mcr70
 *
 * @param <T>
 */
class LivelinessListener<T> implements WriterLivelinessListener {

    private DataReader<T> dr;

    public LivelinessListener(DataReader<T> dr) {
        this.dr = dr;
    }

    @Override
    public void livelinessLost(PublicationData pd) {
        dr.livelinessLost(pd);
    }
}
