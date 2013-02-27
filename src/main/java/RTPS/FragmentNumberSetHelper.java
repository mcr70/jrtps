package RTPS;


/**
* RTPS/FragmentNumberSetHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

abstract public class FragmentNumberSetHelper
{
  private static String  _id = "IDL:RTPS/FragmentNumberSet:1.0";

  public static void insert (org.omg.CORBA.Any a, RTPS.FragmentNumberSet that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static RTPS.FragmentNumberSet extract (org.omg.CORBA.Any a)
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
          org.omg.CORBA.StructMember[] _members0 = new org.omg.CORBA.StructMember [2];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          _tcOf_members0 = RTPS.FragmentNumber_tHelper.type ();
          _members0[0] = new org.omg.CORBA.StructMember (
            "bitmapBase",
            _tcOf_members0,
            null);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_sequence_tc (8, _tcOf_members0);
          _members0[1] = new org.omg.CORBA.StructMember (
            "bitmap",
            _tcOf_members0,
            null);
          __typeCode = org.omg.CORBA.ORB.init ().create_struct_tc (RTPS.FragmentNumberSetHelper.id (), "FragmentNumberSet", _members0);
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

  public static RTPS.FragmentNumberSet read (org.omg.CORBA.portable.InputStream istream)
  {
    RTPS.FragmentNumberSet value = new RTPS.FragmentNumberSet ();
    value.bitmapBase = RTPS.FragmentNumber_tHelper.read (istream);
    int _len0 = istream.read_long ();
    if (_len0 > (8))
      throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    value.bitmap = new int[_len0];
    istream.read_long_array (value.bitmap, 0, _len0);
    return value;
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, RTPS.FragmentNumberSet value)
  {
    RTPS.FragmentNumber_tHelper.write (ostream, value.bitmapBase);
    if (value.bitmap.length > (8))
      throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    ostream.write_long (value.bitmap.length);
    ostream.write_long_array (value.bitmap, 0, value.bitmap.length);
  }

}
