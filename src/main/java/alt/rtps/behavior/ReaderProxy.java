package alt.rtps.behavior;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.types.GUID_t;
import alt.rtps.types.Locator_t;

public class ReaderProxy {
	/**
	 * Identifies the remote matched RTPS Reader that is represented by the ReaderProxy.
	 */
	GUID_t remoteReaderGuid;
	
	/**
	 * List of unicast locators (transport, address, port combinations) that can be
	 * used to send messages to the matched RTPS Reader. The list may be empty.
	 */
	List<Locator_t>unicastLocatorList = new LinkedList<Locator_t>();

	/**
	 * List of multicast locators (transport, address, port combinations) that can be
	 * used to send messages to the matched RTPS Reader. The list may be empty.
	 */
	List<Locator_t >multicastLocatorList = new LinkedList<Locator_t>();
	
	/**
	 * List of CacheChange changes as they relate to the matched RTPS Reader.
	 */
	List<ChangeForReader>changes_for_reader; 
	
	/**
	 * Specifies whether the remote matched RTPS Reader expects in-line QoS to be
	 * sent along with any data.
	 */
	boolean expectsInlineQos = false;
	
	/**
	 * Specifies whether the remote Reader is responsive to the Writer.
	 */
	boolean isActive = true;
	
	
	/**
	 * This operation changes the ChangeForReader status of a set of changes for the reader 
	 * represented by ReaderProxy ‘the_reader_proxy.’ The set of changes with sequence number 
	 * smaller than or equal to the value ‘committed_seq_num’ have their status changed to ACKNOWLEDGED.
	 * @see 8.4.7.5.2 acked_changes_set
	 */
	public void acked_changes_set(long committed_seq_num) {
		for (ChangeForReader change : changes_for_reader) {
			// TODO: What is the relation of ChangeForReader to CacheChange? Inheritance?
			if (change.cache_change.getSequenceNumber() <= committed_seq_num) {
				change.status = ChangeForReaderStatusKind.ACKNOWLEDGED;
			}
		}
	}

	/**
	 * This operation returns the ChangeForReader for the ReaderProxy that has the lowest 
	 * sequence number among the changes with status ‘REQUESTED.’ This represents the next repair packet 
	 * that should be sent to the RTPS Reader represented by the ReaderProxy in response to 
	 * a previous AckNack message (see Section 8.3.7.1) from the Reader. 
	 * 
	 * @see 8.4.7.5.3 next_requested_change
	 */
	public ChangeForReader next_requested_change() {
		return next_change(ChangeForReaderStatusKind.REQUESTED);
	}

	
	/**
	 * This operation returns the CacheChange for the ReaderProxy that has the lowest sequence number
	 * among the changes with status ‘UNSENT.’ This represents the next change that should be sent to 
	 * the RTPS Reader represented by the ReaderProxy.
	 * 
	 * @return
	 * @see 8.4.7.5.4 next_unsent_change
	 */
	public ChangeForReader next_unsent_change() {
		return next_change(ChangeForReaderStatusKind.UNSENT);		
	}
	
	/**
	 * This operation returns the subset of changes for the ReaderProxy that have status ‘REQUESTED.’ 
	 * This represents the set of changes that were requested by the RTPS Reader represented by 
	 * the ReaderProxy using an ACKNACK Message.
	 * 
	 * @see 8.4.7.5.5 requested_changes
	 */
	public List<ChangeForReader> requested_changes() {
		return changes(ChangeForReaderStatusKind.REQUESTED);
	}
	
	/**
	 * 8.4.7.5.6 requested_changes_set
	 * This operation modifies the ChangeForReader status of a set of changes for the RTPS Reader 
	 * represented by ReaderProxy ‘this.’ The set of changes with sequence numbers ‘req_seq_num_set’ 
	 * have their status changed to REQUESTED.
	 */
	public void requested_changes_set(long[] req_seq_num_set) {
		for (int i = 0; i < req_seq_num_set.length; i++) {
			ChangeForReader change = findChange(req_seq_num_set[i]);
			change.status = ChangeForReaderStatusKind.REQUESTED;
		}
	}



	/**
	 * This operation returns the subset of changes for the ReaderProxy the have status ‘UNSENT.’ 
	 * This represents the set of changes that have not been sent to the RTPS Reader represented 
	 * by the ReaderProxy.
	 * @return 
	 *
	 * @see 8.4.7.5.7 unsent_changes
	 */
	public List<ChangeForReader> unsent_changes() {
		return changes(ChangeForReaderStatusKind.UNSENT);
	}
	
	/**
	 * This operation returns the subset of changes for the ReaderProxy that have status ‘UNACKNOWLEDGED.’ 
	 * This represents the set of changes that have not been acknowledged yet by the RTPS Reader represented 
	 * by the ReaderProxy.
	 * @return 
	 * 
	 * @see 8.4.7.5.8 unacked_changes
	 */
	public List<ChangeForReader> unacked_changes() {
		return changes(ChangeForReaderStatusKind.UNACKNOWLEDGED);
	}


	/**
	 * This operation returns the ChangeForReader for the ReaderProxy that has the lowest 
	 * sequence number among the changes with given status.   
	 */
	private ChangeForReader next_change(ChangeForReaderStatusKind statusKind) {
		ChangeForReader next_change = null;
		
		for (ChangeForReader change : changes_for_reader) {
			if (change.status == statusKind) {
				if (next_change == null) {
					next_change = change;
				}
				else if (change.cache_change.getSequenceNumber() < next_change.cache_change.getSequenceNumber()) {
					next_change = change;
				}
			}
		}		
		
		return next_change;
		
	}

	/**
	 * This operation returns the subset of changes for the ReaderProxy that have given status. 
	 */
	private List<ChangeForReader> changes(ChangeForReaderStatusKind statusKind) {
		List<ChangeForReader> changes = new LinkedList<ChangeForReader>();
		
		for (ChangeForReader change : changes_for_reader) {
			if (change.status == statusKind) {
				changes.add(change);
			}
		}
		
		return changes;
	}

	private ChangeForReader findChange(long seq_num) {
		for (ChangeForReader change : changes_for_reader) {
			if (change.cache_change.getSequenceNumber() == seq_num) {
				return change;
			}
		}
		
		return null;
	}
}
