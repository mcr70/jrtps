package RTPS;


/**
* RTPS/FilterSignature_tHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

public final class FilterSignature_tHolder implements org.omg.CORBA.portable.Streamable
{
  public int value[] = null;

  public FilterSignature_tHolder ()
  {
  }

  public FilterSignature_tHolder (int[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RTPS.FilterSignature_tHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RTPS.FilterSignature_tHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RTPS.FilterSignature_tHelper.type ();
  }

}
