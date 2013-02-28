package alt.rtps.behavior;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import alt.rtps.message.AckNack;
import alt.rtps.structure.CacheChange;
import alt.rtps.structure.RTPSWriter;
import alt.rtps.structure.Writer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;

/**
 * 
 * @author mcr70
 * @see 8.4.7.4 RTPS StatefulWriter
 */
public class StatefulWriter extends Writer {
	private static final Logger log = LoggerFactory.getLogger(StatefulWriter.class);
	private List<ReaderProxy> matched_readers;
	
	public StatefulWriter(GuidPrefix_t prefix, EntityId_t entityId, String topicName) {
		super(prefix, entityId, topicName);
	}

	/**
	 * This operation takes a CacheChange a_change as a parameter and determines whether 
	 * all the ReaderProxy have acknowledged the CacheChange. The operation will return true 
	 * if all ReaderProxy have acknowledged the corresponding CacheChange and false otherwise.
	 * 
	 * @return
	 * @see 8.4.7.4.2 is_acked_by_all
	 */
	public boolean is_acked_by_all(CacheChange a_change) {
		for (ReaderProxy proxy : matched_readers) {
			for (ChangeForReader change : proxy.changes_for_reader) {
				if (a_change == change.cache_change) { // TODO: equals
					if (! (change.is_relevant == true && change.status == ChangeForReaderStatusKind.ACKNOWLEDGED)) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	/**
	 * This operation adds the ReaderProxy a_reader_proxy to the set StatefulWriter::matched_readers.
	 * 
	 * @param proxy
	 * @see 8.4.7.4.3 matched_reader_add
	 */
	public void matched_reader_add(ReaderProxy proxy) {
		matched_readers.add(proxy);
	}

	/**
	 * This operation removes the ReaderProxy a_reader_proxy from the set StatefulWriter::matched_readers.
	 * 
	 * @param proxy
	 * @see 8.4.7.4.4 matched_reader_remove
	 */
	public void matched_reader_remove(ReaderProxy proxy) {
		matched_readers.remove(proxy);
	}
	
	/**
	 * This operation finds the ReaderProxy with GUID_t a_reader_guid from the set StatefulWriter::matched_readers.
	 * @param guid
	 * @return
	 * @see 8.4.7.4.5 matched_reader_lookup
	 */
	public ReaderProxy matched_reader_lookup(GUID_t guid) {
		for (ReaderProxy proxy : matched_readers) {
			if (proxy.remoteReaderGuid == guid) { // TODO: equals()
				return proxy;
			}
		}
		
		return null;
	}


	
	public void onAckNack(AckNack ackNack) {
		log.debug("onAckNack(): " + ackNack);
		
		log.warn("AckNack NOT handled");
	}

	
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
