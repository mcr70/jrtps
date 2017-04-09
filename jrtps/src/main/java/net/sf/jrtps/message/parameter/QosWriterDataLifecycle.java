package net.sf.jrtps.message.parameter;


/**
 * WriterDataLifecycle QoS policy.
 * 
 * @author mcr70
 */
public class QosWriterDataLifecycle implements DataWriterPolicy<QosWriterDataLifecycle>, Changeable {

    private boolean autodisposeUnregisteredInstances;
    
    public QosWriterDataLifecycle(boolean autodisposeUnregisteredInstances) {
        this.autodisposeUnregisteredInstances = autodisposeUnregisteredInstances;
    }

    public boolean getAutodisposeUnregisteredInstances() {
        return autodisposeUnregisteredInstances;
    }
    
    @Override
    public boolean isCompatible(QosWriterDataLifecycle requested) {
        return true; // Applies only to writer
    }
}
