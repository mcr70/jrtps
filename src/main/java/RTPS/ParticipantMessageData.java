package RTPS;


/**
* RTPS/ParticipantMessageData.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

public final class ParticipantMessageData implements org.omg.CORBA.portable.IDLEntity
{
  public byte participantGuidPrefix[] = null;
  public byte kind[] = null;
  public byte data[] = null;

  public ParticipantMessageData ()
  {
  } // ctor

  public ParticipantMessageData (byte[] _participantGuidPrefix, byte[] _kind, byte[] _data)
  {
    participantGuidPrefix = _participantGuidPrefix;
    kind = _kind;
    data = _data;
  } // ctor

} // class ParticipantMessageData
