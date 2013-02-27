package RTPS;

/**
* RTPS/ParameterHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

public final class ParameterHolder implements org.omg.CORBA.portable.Streamable
{
  public RTPS.Parameter value = null;

  public ParameterHolder ()
  {
  }

  public ParameterHolder (RTPS.Parameter initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = RTPS.ParameterHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    RTPS.ParameterHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return RTPS.ParameterHelper.type ();
  }

}
