package net.sf.jrtps.udds;

import java.util.List;
import java.util.Set;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.rtps.WriterLivelinessListener;
import net.sf.jrtps.rtps.WriterProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents a strongly typed DataReader in spirit of DDS specification.
 * DataReader maintains a history cache where it keeps Samples received from writers on the network.
 * There are two ways to get these Samples from DataReader. One is by using any of the get methods,
 * and the other is by registering a SampleListener.
 * 
 * @author mcr70
 * 
 * @param <T> Type of the DataReader. Type may be obtained from an external tool
 *            like IDL compiler, or it may be more dynamically constructed
 *            Object that is used with uDDS.
 */
public class DataReader<T> extends Entity<T, PublicationData> {
	private static final Logger logger = LoggerFactory.getLogger(DataReader.class);
	
	private UDDSReaderCache<T> hCache;

    /**
     * RTPSReader associated with this DataReader
     */
    protected final RTPSReader<T> rtps_reader;

    private SubscriptionData subscriptionData;

	private ContentFilter<T> contentFilter;

    /**
     * Constructor with null typeName
     * @param p Participant
     * @param type Class of the type this DataReader handles
     * @param reader RTPSReader used 
     */
    protected DataReader(Participant p, Class<T> type, RTPSReader<T> reader) {
    	this(p, type, null, reader);
    }
    
    /**
     * Constructor for DataReader.
     * 
     * @param p Participant that created this DataReader
     * @param type Type of this DataReader
     * @param typeName Name of the type. If null, fully qualified class name is used
     * @param reader associated RTPSReader
     */
    protected DataReader(Participant p, Class<T> type, String typeName, RTPSReader<T> reader) {
        super(p, type, typeName, reader.getTopicName(), reader.getGuid());
        this.rtps_reader = reader;
    }

    /**
     * Package access.
     * @param hCache history cache
     */
    void setHistoryCache(UDDSReaderCache<T> hCache) {
        this.hCache = hCache;
    }

    /**
     * Adds a SampleListener to this DataReader
     * @param listener Listener to add
     */
    public void addSampleListener(SampleListener<T> listener) {
        hCache.addListener(listener);
    }
    
    /**
     * Adds a WriterListener to this DataReader.
     * @param wl WriterListener to add
     */
    public void addWriterListener(WriterLivelinessListener wl) {
        rtps_reader.addWriterLivelinessListener(wl);
    }


    /**
     * Gets a Set of instances this DataReader knows. 
     * 
     * @return a Set of instances
     */
    public Set<Instance<T>> getInstances() {
        return hCache.getInstances();
    }

    /**
     * Gets an Instance of given Sample. Due to asynchronous nature of DDS applications, 
     * instance might be disposed after the Sample has been passed to application. In that case,
     * this method will return null.
     * <pre>
     *   App                      DataReader                  RTPSReader
     *    |------- getSamples() -----&gt;|                           |
     *    |&lt;------ samples -----------|                           |
     *    |                           |&lt;-------- dispose ---------|
     *    |------- getInstance() ----&gt;|                           |
     *    |&lt;------ null --------------|                           |
     *    |                           |                           |
     * </pre>
     * @param sample a Sample, whose instance is retrieved 
     * @return Instance of given Sample, or null if there is no such Instance available.
     */
    public Instance<T> getInstance(Sample<T> sample) {
        return hCache.getInstance(sample.getKey());
    }
    
    
    /**
     * Gets all the samples this DataReader knows about. Samples are returned in the order
     * they have been received.
     * 
     * @return all the Samples
     */
    public List<Sample<T>> getSamples() {
        return hCache.getSamplesSince(0);
    }
    
    /**
     * Gets all the Samples that have been received after given Sample. 
     * 
     * @param s a Sample to compare.
     * @return all the samples that have been received after given Sample
     */
    public List<Sample<T>> getSamplesSince(Sample<T> s) {
        return hCache.getSamplesSince(s.getSequenceNumber());
    }

    /**
     * Clears Samples from the history cache. Once cleared, they cannot be retrieved from
     * the cache anymore. Note, that clearing samples from readers history cache is not the same thing as 
     * writer disposing. Reader clearing samples is merely a local operation while writer disposal
     * is a global operation affecting all the entities in the network. Clearing can be thought of as 
     * a manual resource control on reader side.
     * 
     * @param samples a List of Samples to clear.
     */
    public void clear(List<Sample<T>> samples) {
        hCache.clear(samples); 
    }

    
    /**
     * Removes a given SampleListener from this DataReader.
     * @param listener A listener to remove
     */
    public void removeSampleListener(SampleListener<T> listener) {
        hCache.removeListener(listener);
    }


    /**
     * Removes a WriterListener from this DataReader.
     * @param wl WriterListener to remove
     */
    public void removeWriterListener(WriterLivelinessListener wl) {        
        rtps_reader.removeWriterLivelinessListener(wl);
    }

    /**
     * Package access
     */
    RTPSReader<T> getRTPSReader() {
        return rtps_reader;
    }


    void addMatchedWriter(PublicationData pd) {
        rtps_reader.addMatchedWriter(pd);
        synchronized (communicationListeners) {
            for (CommunicationListener<PublicationData> cl : communicationListeners) {
                cl.entityMatched(pd);
            }
        }
    }

    void removeMatchedWriter(PublicationData pd) {
    	WriterProxy writer = rtps_reader.removeMatchedWriter(pd);
    	if (writer != null) {
    		logger.debug("marking writer {}({}) as not alive", writer.getGuid(), writer.hashCode());
    		writer.isAlive(false);
    	}
    }

    void inconsistentQoS(PublicationData pd) {
        synchronized (communicationListeners) {
            for (CommunicationListener<PublicationData> cl : communicationListeners) {
                cl.inconsistentQoS(pd);
            }
        }
    }
    
    
    void setSubscriptionData(SubscriptionData rd) {
        this.subscriptionData = rd;
    }
    
    SubscriptionData getSubscriptionData() {
        return subscriptionData;
    }

    /**
     * Sets a ContentFilter. ContentFilterProperty from ContentFilter will be sent to
     * writers. Writers that support writer side filtering will be able to apply
     * filtering if the parameters of ContentFilterProperty match.<p>
     * 
     * @param cf ContentFilter to set
     */
    public void setContentFilter(ContentFilter<T> cf) {
    	this.contentFilter = cf;
    	// Write ContentFilterProperty to remote writers
    	getParticipant().writeSubscriptionData(this);
    	hCache.setContentFilter(cf);

    	if (cf != null && cf.getContentFilterProperty() != null) {
			hCache.setContentFilterSignature(cf.getContentFilterProperty().getRawSignature());
		}
		else {
			hCache.setContentFilterSignature(null);
		}
    }


    /**
     * Gets the ContentFilterProperty associated with this reader.
     * @return ContentFilterProperty, or null if this reader has no ContentFilter
     */
    ContentFilterProperty getContentFilterProperty() {
    	if (contentFilter != null) {
    		return contentFilter.getContentFilterProperty();
    	}
    	
    	return null;
    }
}
