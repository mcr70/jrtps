package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * PropertyList attribute holds a sequence of name-value properties.
 * @author mcr70
 */
public class PropertyList extends Parameter implements InlineQoS {
    private Property[] properties;
    
    PropertyList() {
        super(ParameterEnum.PID_PROPERTY_LIST);
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

    /**
     * Property class represents name-value pair in PropertyList 
     * @author mcr70
     */
    public static class Property {
        private String name;
        private String value;
        
        Property(RTPSByteBuffer bb) {
            this.name = bb.readString();
            this.value = bb.readString();
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
    }
}