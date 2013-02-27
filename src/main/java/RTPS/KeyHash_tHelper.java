package RTPS;


/**
* RTPS/KeyHash_tHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

abstract public class KeyHash_tHelper
{
  private static String  _id = "IDL:RTPS/KeyHash_t:1.0";

  public static void insert (org.omg.CORBA.Any a, RTPS.KeyHash_t that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RTPS.KeyHash_t extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
          }
          __active = true;
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [1];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_octet);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_array_tc (16, _tcOf_members0 );
          _members0[0] = new org.omg.CORBA.StructMember (
            "value",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (RTPS.KeyHash_tHelper.id (), "KeyHash_t", _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static RTPS.KeyHash_t read (org.omg.CORBA.portable.InputStream istream)
  {
    RTPS.KeyHash_t value = new RTPS.KeyHash_t ();
    value.value = new byte[16];
    for (int _o0 = 0;_o0 < (16); ++_o0)
    {
      value.value[_o0] = istream.read_octet ();
    }
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RTPS.KeyHash_t value)
  {
    if (value.value.length != (16))
      throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    for (int _i0 = 0;_i0 < (16); ++_i0)
    {
      ostream.write_octet (value.value[_i0]);
    }
  }

}
