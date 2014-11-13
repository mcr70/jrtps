package net.sf.jrtps.udds.security;


/**
 * See 7.2.3.1 IDL representation for DataHolder
 * 
 * @author mcr70
 */
public abstract class DataHolder {
    
    /**
     * Gets the class_id of this DataHolder
     * @return class_id
     */
    public abstract String getClassId();
    
    private Property[] string_properties;
    private BinaryProperty[] binary_properties;
    private String[] string_values;
    private byte[] binary_value1;
    private byte[] binary_value2;
    private long longlongs_value;
}
