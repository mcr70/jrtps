package net.sf.jrtps.udds;

import java.util.List;

import net.sf.jrtps.rtps.RTPSListener;
import net.sf.jrtps.rtps.Sample;

/**
 * This class is an adapter between RTPSListener and SampleListener.
 * Purpose of this class is to provide a place where samples can be 
 * dropped before passed on to applications. 
 * 
 * For example, implementing a QosOwnership can be implemented here.
 * 
 * @author mcr70
 *
 * @param <T>
 */
class RTPSListenerAdapter<T> implements RTPSListener<T> {

    private SampleListener<T> listener;

    public RTPSListenerAdapter(SampleListener<T> listener) {
        this.listener = listener;
    }
    
    @Override
    public void onSamples(List<Sample<T>> samples) {
        // TODO: currently just pass samples to applications as-is
        listener.onSamples(samples);
    }
}
