package net.sf.jrtps.udds;

import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(Participant.class);
	
	private final RTPSParticipant rtps_participant;
	private final JavaSerializableMarshaller marshaller;

	/**
	 * Create a Participant with domainId 0 and participantId 0.
	 * @throws SocketException
	 */
	public Participant() throws SocketException {
		this(0, 0);
	} 
	
	/**
	 * Create a Participant with given domainId and participantId.
	 * Participants with same domainId are able to communicate with each other.
	 * participantId is used to distinguish participants within this domain(and JVM).
	 * More specifically, domainId and participantId are used to select networking ports used
	 * by participant.
	 * 
	 * @param domainId domainId of this participant.
	 * @param participantId participantId of this participant. 
	 * @throws SocketException
	 */
	public Participant(int domainId, int participantId) throws SocketException {
		marshaller = new JavaSerializableMarshaller();
		logger.debug("Creating Participant for domain {}, participantId {}", domainId, participantId);
		
		rtps_participant = new RTPSParticipant(domainId, participantId);
		rtps_participant.start();
	}
	
	
	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataReader is set to fully qualified class name.
	 * @param c 
	 * @return
	 */
	public <T> DataReader<T> createDataReader(Class<T> c) {
		if (!java.io.Serializable.class.isAssignableFrom(c)) {
			throw new IllegalArgumentException(c.getName() + " must implement java.io.Serializable" );
		}
		
		return createDataReader(c.getSimpleName(), c.getName());
	} 

	/**
	 * Create DataReader with given topicName and typeName.
	 * 
	 * @param topicName name of the topic
	 * @param typeName name of the type
	 * @return
	 */
	public <T> DataReader<T> createDataReader(String topicName, String typeName) {
		RTPSReader rtps_reader = rtps_participant.createReader(topicName, typeName, marshaller);
		logger.debug("Creating DataReader for topic {}, type {}", topicName, typeName);
		
		return new DataReader<T>(topicName, rtps_reader);
	} 

	
	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataWriter is set to fully qualified class name.
	 * 
	 * @param c A class, that is used with created DataWriter.
	 * @return
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> c) {
		return createDataWriter(c.getSimpleName(), c.getName());
	} 

	/**
	 * Create DataWriter with given topicName and typeName.
	 * 
	 * @param topicName name of the topic
	 * @param typeName name of the type
	 * @return
	 */
	public <T> DataWriter<T> createDataWriter(String topicName, String typeName) {
		RTPSWriter rtps_writer = rtps_participant.createWriter(topicName, typeName, marshaller);
		logger.debug("Creating DataWriter for topic {}, type {}", topicName, typeName);
		
		return new DataWriter<T>(topicName, rtps_writer);
	}

	/**
	 * Close this participant.
	 */
	public void close() {
		rtps_participant.close();
	} 
}
