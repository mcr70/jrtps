package net.sf.jrtps.udds.security;


/**
 * See 7.2.3.1 IDL representation for DataHolder
 * 
 * @author mcr70
 */
class DataHolder {
    String class_id;
    Property[] string_properties;
    BinaryProperty[] binary_properties;
    String[] string_values;
    byte[] binary_value1;
    byte[] binary_value2;
    long longlongs_value;
}
