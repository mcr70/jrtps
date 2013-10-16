package net.sf.udds;

import java.net.SocketException;

import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.RTPSWriter;


/**
 * Participant acts as a communication endpoint of different data.
 * 
 * @author mcr70
 *
 */
public class Participant {

	private final RTPSParticipant rtps_participant;
	private final JavaSerializableMarshaller marshaller;

	public Participant() throws SocketException {
		this(0, 0);
	} 
	
	public Participant(int domainId, int participantId) throws SocketException {
		marshaller = new JavaSerializableMarshaller();
		
		rtps_participant = new RTPSParticipant(domainId, participantId);
		rtps_participant.start();
	}
	
	
	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * 
	 * @param c
	 * @return
	 */
	public <T> DataReader<T> createDataReader(Class<T> c) {
		if (!java.io.Serializable.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException(c.getName() + " must implement java.io.Serializable" );
		}
		
		return createDataReader(c.getSimpleName(), c.getName());
	} 

	public <T> DataReader<T> createDataReader(String topicName, String typeName) {
		RTPSReader rtps_reader = rtps_participant.createReader(topicName, typeName, marshaller);
		
		return new DataReader<T>(topicName, rtps_reader);
	} 

	
	/**
	 * Creates a new DataWriter of given type.
	 * @param c A class, that is used with created DataWriter.
	 * @return
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> c) {
		return createDataWriter(c.getSimpleName(), c.getName());
	} 

	public <T> DataWriter<T> createDataWriter(String topicName, String typeName) {
		RTPSWriter rtps_writer = rtps_participant.createWriter(topicName, typeName, marshaller);
		
		return new DataWriter<T>(topicName, rtps_writer);
	}

	/**
	 * Close this participant. 
	 */
	public void close() {
		rtps_participant.close();
	} 
}
