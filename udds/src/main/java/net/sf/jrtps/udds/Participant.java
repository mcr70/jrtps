package net.sf.jrtps.udds;

import java.net.SocketException;
import java.util.HashMap;

import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.RTPSWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Participant acts as a communication endpoint of different data.
 * 
 * @author mcr70
 *
 */
public class Participant {
	private static final Logger logger = LoggerFactory.getLogger(Participant.class);
	
	private Marshaller<?> defaultMarshaller;
	private final HashMap<String, Marshaller<?>> marshallers = new HashMap<>();
	private final RTPSParticipant rtps_participant;
	

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
		defaultMarshaller = new JavaSerializableMarshaller();
		logger.debug("Creating Participant for domain {}, participantId {}", domainId, participantId);
		
		rtps_participant = new RTPSParticipant(domainId, participantId);
		rtps_participant.start();
	}
	
	
	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataReader is set to fully qualified class name.
	 * @param c 
	 * @return a DataReader<T>
	 */
	public <T> DataReader<T> createDataReader(Class<T> c) {
		return createDataReader(c, new QualityOfService());
	} 

	public <T> DataReader<T> createDataReader(Class<T> c, QualityOfService qos) {
		return createDataReader(c.getSimpleName(), c, c.getName(), qos);
	} 

	/**
	 * Create DataReader with given topicName and typeName.
	 * 
	 * @param topicName name of the topic
	 * @param type type of the DataReader
	 * @param typeName name of the type
	 * @param qos QualityOfService
	 * @return a DataReader<T>
	 */
	public <T> DataReader<T> createDataReader(String topicName, Class<T> type, String typeName, QualityOfService qos) {
		Marshaller<?> m = getMarshaller(typeName);
		RTPSReader<T> rtps_reader = rtps_participant.createReader(topicName, type, typeName, m, qos);
		logger.debug("Creating DataReader for topic {}, type {}", topicName, typeName);
		
		return new DataReader<T>(rtps_reader);
	} 

	
	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataWriter is set to fully qualified class name.
	 * 
	 * @param c A class, that is used with created DataWriter.
	 * @return a DataWriter<T>
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> c) {
		return createDataWriter(c, new QualityOfService());
	} 

	public <T> DataWriter<T> createDataWriter(Class<T> c, QualityOfService qos) {
		return createDataWriter(c.getSimpleName(), c, c.getName(), qos);
	} 
	
	/**
	 * Create DataWriter with given topicName and typeName.
	 * 
	 * @param topicName name of the topic
	 * @param type type of the DataWriter
	 * @param typeName name of the type
	 * @param qos QualityOfService
	 * @return a DataWriter<T>
	 */	
	public <T> DataWriter<T> createDataWriter(String topicName, Class<T> type, String typeName, QualityOfService qos) {
		Marshaller<?> m = getMarshaller(typeName);
		RTPSWriter<T> rtps_writer = rtps_participant.createWriter(topicName, type, typeName, m, qos);
		logger.debug("Creating DataWriter for topic {}, type {}", topicName, typeName);
		
		return new DataWriter<T>(rtps_writer);
	}

	
	/**
	 * Sets the default Marshaller. Default marshaller is used if no other Marshaller 
	 * could not be used.
	 * d
	 * @param m
	 */
	public void setDefaultMarshaller(Marshaller<?> m) {
		defaultMarshaller = m;
	}
	
	/**
	 * Sets a type specific Marshaller. When creating entities, a type specific Marshaller is
	 * preferred over default Marshaller.
	 * 
	 * @param typeName
	 * @param m
	 */
	public void setMarshaller(String typeName, Marshaller<?> m) {
		marshallers.put(typeName, m);
	}
	
	
	/**
	 * Close this participant.
	 */
	public void close() {
		rtps_participant.close();
	} 

	/**
	 * Get a Marshaller for given type. If no explicit Marshaller is found for type,
	 * a default Marshaller is returned 
	 * 
	 * @param typeName
	 * @return Marshaller
	 */
	private Marshaller<?> getMarshaller(String typeName) {
		Marshaller<?> m = marshallers.get(typeName);
		if (m == null) {
			m = defaultMarshaller;
		}
		
		return m;
	}
}
