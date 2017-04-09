package net.sf.jrtps;

/**
 * OutOfResources gets thrown if one of RESOURCE_LIMITS QoS policys
 * limit is exceeded.
 * 
 * @author mcr70
 */
public class OutOfResources extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private Kind kind;
    private int limit;
    
    public enum Kind {
        MAX_SAMPLES_EXCEEDED,
        MAX_INSTANCES_EXCEEDED,
        MAX_SAMPLES_PER_INSTANCE_EXCEEDED
    };
    
    public OutOfResources(Kind kind, int limit) {
        super(kind + ": limit is " + limit);
        this.kind = kind;
        this.limit = limit;
    }
   
    /**
     * Get the reason that caused this exception to be thrown.
     * @return Kind
     */
    public Kind getKind() {
        return kind;
    }
    
    /**
     * Get the limit that was exceeded
     * @return limit
     */
    public int getLimit() {
        return limit;
    }
}
