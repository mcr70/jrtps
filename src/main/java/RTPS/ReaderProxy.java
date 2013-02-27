package RTPS;


/**
* RTPS/ReaderProxy.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

public final class ReaderProxy implements org.omg.CORBA.portable.IDLEntity
{
  public RTPS.GUID_t remoteReaderGuid = null;
  public boolean expectsInlineQos = false;
  public RTPS.Locator_t unicastLocatorList[] = null;
  public RTPS.Locator_t multicastLocatorList[] = null;

  public ReaderProxy ()
  {
  } // ctor

  public ReaderProxy (RTPS.GUID_t _remoteReaderGuid, boolean _expectsInlineQos, RTPS.Locator_t[] _unicastLocatorList, RTPS.Locator_t[] _multicastLocatorList)
  {
    remoteReaderGuid = _remoteReaderGuid;
    expectsInlineQos = _expectsInlineQos;
    unicastLocatorList = _unicastLocatorList;
    multicastLocatorList = _multicastLocatorList;
  } // ctor

} // class ReaderProxy
