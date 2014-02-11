package net.sf.jrtps.rtps;

import java.util.List;

/**
 * A listener for samples.
 * 
 * @author mcr70
 * 
 * @param <T>
 *            Type of the samples
 */
public interface SampleListener<T> {
    /**
     * This method is called when RTPSReader receives some samples.
     * 
     * @param samples
     *            a List of Samples of type T
     */
    public void onSamples(List<Sample<T>> samples);
}
