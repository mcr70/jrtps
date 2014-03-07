package net.sf.jrtps.udds;

import java.util.List;

import net.sf.jrtps.rtps.Sample;

/**
 * A listener for samples.
 * 
 * @author mcr70
 * 
 * @param <T> Type of the samples
 */
public interface SampleListener<T> {
    /**
     * This method is called when DataReader receives some samples.
     * 
     * @param samples a List of Samples of type T
     */
    public void onSamples(List<Sample<T>> samples);
}
