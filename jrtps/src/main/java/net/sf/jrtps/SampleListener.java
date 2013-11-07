package net.sf.jrtps;

import java.util.List;

public interface SampleListener<T> {
	public void onSamples(List<Sample<T>> samples);
}
