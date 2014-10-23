package net.sf.jrtps.message.parameter;



public class QosWriterDataLifecycle implements DataWriterPolicy<QosWriterDataLifecycle> {

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
