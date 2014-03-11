package net.sf.jrtps.udds;

/**
 * A Filter interface is used for reader side filtering.
 * @author mcr70
 *
 * @param <T>
 */
interface Filter<T> {
    boolean acceptSample(T sample);
}
