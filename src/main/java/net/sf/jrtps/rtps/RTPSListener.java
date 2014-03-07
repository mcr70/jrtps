package net.sf.jrtps.rtps;

import java.util.List;

/**
 * RTPSListener is used to listen for samples from RTPS layer.
 * Samples are not filtered in any way. 
 * 
 * @author mcr70
 * 
 * @param <T> Type of the samples
 */
public interface RTPSListener<T> {
    /**
     * This method is called when RTPSReader receives some samples.
     * 
     * @param samples a List of Samples of type T
     */
    public void onSamples(List<Sample<T>> samples);
}
