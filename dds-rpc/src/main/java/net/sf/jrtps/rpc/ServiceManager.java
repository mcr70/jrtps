package net.sf.jrtps.rpc;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.udds.CommunicationListener;
import net.sf.jrtps.udds.DataReader;
import net.sf.jrtps.udds.DataWriter;
import net.sf.jrtps.udds.EntityListener;
import net.sf.jrtps.udds.Participant;

/**
 * ServiceManager is a starting point for RPC related work.
 * It can be used to register services and create clients.
 * 
 * @author mcr70
 */
public class ServiceManager {
    private static final Logger logger = LoggerFactory.getLogger(ServiceManager.class);

    private final Map<Class<?>, Serializer> serializers = new HashMap<>();   
    private final Set<Service> services = new HashSet<>();

    // For services:
    private final Map<Class<?>, DataReader<Request>> requestReaders = new HashMap<>();
    private final Map<Class<?>, DataWriter<Reply>> replyWriters = new HashMap<>();

    // For clients:
    private final Map<Class<?>, DataWriter<Request>> requestWriters = new HashMap<>();
    private final Map<Class<?>, DataReader<Reply>> replyReaders = new HashMap<>();

    private final Participant participant;
    private final QualityOfService serviceQos = new QualityOfService();
    private final Set<String> discoveredRemoteTopics = new HashSet<>();

    /**
     * Creates a ServiceManager with default participant.
     */
    public ServiceManager() {
	this(new Participant());
    }

    /**
     * Create a ServiceManager with given Participant.
     * @param participant a Participant
     */
    public ServiceManager(Participant participant) {
	this.participant = participant;
	Configuration cfg = participant.getConfiguration();
	this.participant.setMarshaller(Request.class, new RequestMarshaller(cfg.getBufferSize()));
	this.participant.setMarshaller(Reply.class, new ReplyMarshaller(cfg.getBufferSize()));

	participant.addEntityListener(new EntityListener() {
	    @Override
	    public void writerDetected(PublicationData wd) {
		discoveredRemoteTopics.add(wd.getTopicName());
	    }
	    @Override
	    public void readerDetected(SubscriptionData rd) {
		discoveredRemoteTopics.add(rd.getTopicName());
	    }
	    @Override
	    public void participantLost(ParticipantData pd) {
	    }
	    @Override
	    public void participantDetected(ParticipantData pd) {
	    }
	});

	// TODO: Check serviceQos
	serviceQos.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, Duration.INFINITE));
	serviceQos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_ALL, 1));
	serviceQos.setPolicy(new QosDurability(QosDurability.Kind.VOLATILE));

	initializeSerializers();
    }

    /**
     * Initialize serializers for the primitive Java types.
     */
    private void initializeSerializers() {      
	Class<?>[] primitiveClasses = new Class[] {
		int.class, Integer.class, int[].class, Integer[].class,
		short.class, Short.class, short[].class, Short[].class,
		long.class, Long.class, long[].class, Long[].class,
		float.class, Float.class, float[].class, Float[].class,
		double.class, Double.class, double[].class, Double[].class,
		char.class, Character.class, char[].class, Character[].class,
		byte.class, Byte.class, byte[].class, Byte[].class,
		boolean.class, Boolean.class, boolean[].class, Boolean[].class, 
		String.class, String[].class
	};

	JavaPrimitiveSerializer js = new JavaPrimitiveSerializer();
	for (Class<?> c: primitiveClasses) {
	    serializers.put(c, js);
	}
    }

    /**
     * Creates a new client proxy for the given service.
     * @param service An interface extending net.sf.jrtps.rpc.Service
     * @param <T> Generic type for Service
     * @return A client proxy for service
     * @throws TimeoutException If there is a timeout connecting with service
     */
    public <T extends Service> T createClient(Class<T> service) throws TimeoutException {
	final String reqTopic = service.getSimpleName() + "_Service_Request";
	final String repTopic = service.getSimpleName() + "_Service_Reply";

	final CountDownLatch cdl = new CountDownLatch(2);

	logger.debug("Creating writer({}) and reader({}) for client {}", 
		reqTopic, repTopic, service.getSimpleName());

	DataWriter<Request> dw = 
		participant.createDataWriter(reqTopic,
			Request.class, Request.class.getName(), serviceQos);
	requestWriters.put(service, dw);

	dw.addCommunicationListener(new CommunicationListener<SubscriptionData>() {
	    @Override
	    public void inconsistentQoS(SubscriptionData ed) {
		logger.warn("Got inconsistent QoS with {}", ed);
	    }
	    @Override
	    public void entityMatched(SubscriptionData ed) {
		cdl.countDown();
	    }
	    @Override
	    public void deadlineMissed(KeyHash instanceKey) {}
	});

	DataReader<Reply> dr = 
		participant.createDataReader(repTopic,
			Reply.class, Reply.class.getName(), serviceQos);
	replyReaders.put(service, dr);

	dr.addCommunicationListener(new CommunicationListener<PublicationData>() {
	    @Override
	    public void deadlineMissed(KeyHash instanceKey) {}
	    @Override
	    public void entityMatched(PublicationData ed) {
		cdl.countDown();
	    }
	    @Override
	    public void inconsistentQoS(PublicationData ed) {
		logger.warn("Inconsistent QoS with {}", ed);
	    }
	});

	EndpointConfiguration ec = new EndpointConfiguration(service);

	Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {service}, 
		new RPCInvocationHandler(ec, participant.getConfiguration(), dw, dr, serializers));

	if (discoveredRemoteTopics.contains(dr.getTopicName()) && 
		discoveredRemoteTopics.contains(dw.getTopicName())) {
	    return (T) proxy;
	}

	boolean await = false;
	try {
	    int timeout = participant.getConfiguration().getRPCConnectionTimeout();
	    await = cdl.await(timeout, TimeUnit.MILLISECONDS);
	} catch (InterruptedException e) {
	    // TimeoutException will be thrown
	}

	if (!await) {
	    throw new TimeoutException("Failed to connect with service");
	}

	return (T) proxy;
    }


    /**
     * Registers a new Service to this ServiceManager. A Succesfull registration of
     * a Service will create all the needed internal DDS Entities. Failure to 
     * create a service will clean up all the related DDS Entities.
     * 
     * @param service a Service to create 
     */
    public void registerService(Service service) {
	logger.debug("Registering service: {}", service);
	Class<?>[] interfaces = service.getClass().getInterfaces();

	for (Class<?> i: interfaces) {
	    if (Service.class.isAssignableFrom(i)) {
		//services.add();
		createEndpoints(i, service);
	    }
	}
    }

    /**
     * Registers a Serializer for given type. Serializers are used to serialize
     * Service call input parameters and return values to wire during remote
     * invocation process. By default, only Serializers for primitive types
     * (int.class, Integer.class, .... , String.class) and arrays of primitive 
     * types is defined.<p>
     * 
     * If a Serializer is registered twice, first registration is overriden.
     * 
     * @param type Type 
     * @param serializer Serializer for type
     */
    public void registerSerializer(Class<?> type, Serializer serializer) {
	serializers.put(type, serializer);
    }

    private void createEndpoints(Class<?> serviceClass, Service service) {      
	EndpointConfiguration ec = new EndpointConfiguration(serviceClass);

	String reqTopic = ec.getRequestTopic();//serviceClass.getSimpleName() + "_Service_Request";
	String repTopic = ec.getReplyTopic();//serviceClass.getSimpleName() + "_Service_Reply";

	logger.debug("Creating reader({}) and writer({}) for service {}", 
		reqTopic, repTopic, serviceClass.getSimpleName());

	DataReader<Request> dr = 
		participant.createDataReader(reqTopic,
			Request.class, Request.class.getName(), serviceQos);
	requestReaders.put(serviceClass, dr);
//dr.setContentFilter(cf); // ContentFilter for serviceName, instanceName

	DataWriter<Reply> dw = 
		participant.createDataWriter(repTopic,
			Reply.class, Reply.class.getName(), serviceQos);
	replyWriters.put(serviceClass, dw);
//dw.registerContentFilter(cf); // ContentFilter for serviceName, instanceName
	
	dr.addSampleListener(new ServiceInvoker(participant.getConfiguration(), 
		serializers, dr, dw, service));
    }
}
