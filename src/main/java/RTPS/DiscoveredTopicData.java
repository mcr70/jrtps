package RTPS;


/**
* RTPS/DiscoveredTopicData.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from rtps.idl
* 8. helmikuuta 2012 9.16.40 EET
*/

public final class DiscoveredTopicData implements org.omg.CORBA.portable.IDLEntity
{
  public DDS.TopicBuiltinTopicData ddsTopicData = null;

  public DiscoveredTopicData ()
  {
  } // ctor

  public DiscoveredTopicData (DDS.TopicBuiltinTopicData _ddsTopicData)
  {
    ddsTopicData = _ddsTopicData;
  } // ctor

} // class DiscoveredTopicData
