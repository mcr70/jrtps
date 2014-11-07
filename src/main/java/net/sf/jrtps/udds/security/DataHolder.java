package net.sf.jrtps.udds.security;


/**
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
    private BinaryProperty binary_property1;
    private BinaryProperty binary_property2;
    private long longlongs_value;
}
