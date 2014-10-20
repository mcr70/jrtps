package net.sf.jrtps.message.parameter;


public class QosReaderDataLifecycle implements DataReaderPolicy<QosReaderDataLifecycle> {

    private final long autopurge_nowriter_samples_delay;
    private final long autopurge_disposed_samples_delay;

    public QosReaderDataLifecycle(long autopurge_nowriter_samples_delay, long autopurge_disposed_samples_delay) {
        this.autopurge_nowriter_samples_delay = autopurge_nowriter_samples_delay;
        this.autopurge_disposed_samples_delay = autopurge_disposed_samples_delay;
    }

    public long getAutopurgeDisposedSamplesDelay() {
        return autopurge_disposed_samples_delay;
    }
    
    public long getAutopurgeNoWriterSamplesDelay() {
        return autopurge_nowriter_samples_delay;
    }
    
    @Override
    public boolean isCompatible(QosReaderDataLifecycle requested) {
        return true; // Applies only to reader
    }
}
