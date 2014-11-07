package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * DataTag, as specified in DDS Security Sepcification
 * @author mcr70
 */
public class DataTag {

    private String name;
    private String value;

    public DataTag(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    DataTag(RTPSByteBuffer bb) {
        this.name = bb.read_string();
        this.value = bb.read_string();
    }

    /**
     * Gets the name of this DataTag
     * @return name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the value of this DataTag
     * @return value
     */
    public String getValue() {
        return value;
    }

    void writeTo(RTPSByteBuffer bb) {
        bb.write_string(name);
        bb.write_string(value);
    }
}
