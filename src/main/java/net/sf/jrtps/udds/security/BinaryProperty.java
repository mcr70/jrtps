package net.sf.jrtps.udds.security;

class BinaryProperty {
    private String name;
    private byte[] value;

    public BinaryProperty(String name, byte[] value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return name;
    }
    
    public byte[] getValue() {
        return value;
    }
}
