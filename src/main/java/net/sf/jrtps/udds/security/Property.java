package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

class Property {
    private String name;
    private String value;

    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    Property(RTPSByteBuffer bb) {
    	this.name = bb.read_string();
    	this.value = bb.read_string();
    }

    void writeTo(RTPSByteBuffer bb) {
    	bb.write_string(name);
    	bb.write_string(value);
    }
    
	public String getName() {
        return name;
    }
    
    public String getValue() {
        return value;
    }
}
