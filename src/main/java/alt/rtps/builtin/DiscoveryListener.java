package alt.rtps.builtin;


public interface DiscoveryListener {
	public void onSpdpData(ParticipantData participantData);

//	public void onSedpData(TopicData topicData);
//	public void onSedpData(ReaderData readerData);
//	public void onSedpData(WriterData writerData);
}
