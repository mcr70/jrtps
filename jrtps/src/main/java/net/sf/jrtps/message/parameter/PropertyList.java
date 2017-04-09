package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * PropertyList attribute holds a sequence of name-value properties.
 * @author mcr70
 */
public class PropertyList extends Parameter implements InlineQoS {
    private Property[] properties;
    
    PropertyList() {
        super(ParameterId.PID_PROPERTY_LIST);
    }

    public PropertyList(Property[] properties) {
        super(ParameterId.PID_PROPERTY_LIST);
        this.properties = properties;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.properties = new Property[bb.read_long()];
        for (int i = 0; i < properties.length; i++) {
            properties[i] = new Property(bb);
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(properties.length);
        for (Property p : properties) {
            p.writeTo(bb);
        }
    }

    
    @Override
    public String toString() {
    	StringBuffer sb = new StringBuffer("PropertyList[ ");
    	for (Property p : properties) {
    		sb.append(p.toString());
    		sb.append(" ");
    	}
    	sb.append("]");
    	
    	return sb.toString();
    }
    
    /**
     * Property class represents name-value pair in PropertyList 
     * @author mcr70
     */
    public static class Property {
        private String name;
        private String value;
        
        Property(RTPSByteBuffer bb) {
            this.name = bb.read_string();
            this.value = bb.read_string();
        }
        
        public void writeTo(RTPSByteBuffer bb) {
            bb.writeString(name);
            bb.writeString(value);
        }

        public Property(String name, String value) {
            this.name = name;
            this.value = value;
        }
        
        /**
         * Gets the name of this property
         * @return name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Gets the value of this property
         * @return value
         */
        public String getValue() {
            return value;
        }
        
        @Override
        public String toString() {
        	return name + "=" + value;
        }
    }
}