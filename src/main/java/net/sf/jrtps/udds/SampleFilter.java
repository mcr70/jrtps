package net.sf.jrtps.udds;

import net.sf.jrtps.rtps.Sample;

/**
 * A Filter interface is used for reader side filtering.
 * @author mcr70
 *
 * @param <T>
 */
interface SampleFilter<T> {
    boolean acceptSample(Sample<T> sample);
}
