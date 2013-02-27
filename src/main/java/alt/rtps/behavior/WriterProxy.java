package alt.rtps.behavior;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.types.GUID_t;
import alt.rtps.types.Locator_t;

public class WriterProxy {
	/**
	 * Identifies the matched Writer.
	 */
	GUID_t remoteWriterGuid;

	/**
	 * List of unicast (address, port) combinations that can be used to send
	 * messages to the matched Writer or Writers. The list may be empty.
	 */
	List<Locator_t>unicastLocatorList = new LinkedList<Locator_t>();
	
	/**
	 * List of multicast (address, port) combinations that can be used to send
	 * messages to the matched Writer or Writers. The list may be empty.
	 */
	List<Locator_t>multicastLocatorList = new LinkedList<Locator_t>();
	
	/**
	 * List of CacheChange changes received or expected from the matched RTPS Writer.
	 */
	List<ChangeFromWriter>changes_from_writer;

	/**
	 * This operation returns the maximum SequenceNumber_t among the changes_from_writer changes in the RTPS
	 * WriterProxy that are available for access by the DDS DataReader.<p>
	 * 
	 * The condition to make any CacheChange ‘a_change’ available for ‘access’ by the DDS DataReader 
	 * is that there are no changes from the RTPS Writer with SequenceNumber_t smaller than or equal to
	 * a_change.sequenceNumber that have status MISSING or UNKNOWN. <p>
	 * 
	 * In other words, the available_changes_max and all previous changes are either RECEIVED or LOST.
	 * 
	 * @see 8.4.10.4.2 available_changes_max
	 */
	public long available_changes_max() {
		long seq_num = 0;
		
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.status == ChangeFromWriterStatusKind.LOST ||
				change.status == ChangeFromWriterStatusKind.RECEIVED) {

				if (change.cache_change.getSequenceNumber() > seq_num) {
					seq_num = change.cache_change.getSequenceNumber();
				}
			}
		}
		
		return seq_num;
	}

	/**
	 * This operation modifies the status of a ChangeFromWriter to indicate that the CacheChange with the
	 * SequenceNumber_t ‘a_seq_num’ is irrelevant to the RTPS Reader.
	 * 
	 * @see 8.4.10.4.3 irrelevant_change_set
	 */
	public void irrelevant_change_set(long a_seq_num) {
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.cache_change.getSequenceNumber() == a_seq_num) {
				change.status = ChangeFromWriterStatusKind.RECEIVED;
				change.isRelevant = false;
			}
		}
	}

	/**
	 * This operation modifies the status stored in ChangeFromWriter for any changes in the WriterProxy 
	 * whose status is MISSING or UNKNOWN and have sequence numbers lower than ‘first_available_seq_num.’ 
	 * The status of those changes is modified to LOST indicating that the changes are no longer available 
	 * in the WriterHistoryCache of the RTPS Writer represented by the RTPS WriterProxy.
	 * 
	 * @see 8.4.10.4.4 lost_changes_update
	 */
	public void lost_changes_update(long first_available_seq_num) {
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.status == ChangeFromWriterStatusKind.MISSING || 
				change.status == ChangeFromWriterStatusKind.UNKNOWN) {
				if (change.cache_change.getSequenceNumber() < first_available_seq_num) {
					change.status = ChangeFromWriterStatusKind.LOST;
				}
			}
		}		
	}

	/**
	 * 8.4.10.4.5 missing_changes
	 * This operation returns the subset of changes for the WriterProxy that have status ‘MISSING.’ 
	 * The changes with status ‘MISSING’ represent the set of changes available in the HistoryCache 
	 * of the RTPS Writer represented by the RTPS WriterProxy that have not been received by the RTPS Reader.
	 */
	public List<ChangeFromWriter> missing_changes() {
		List<ChangeFromWriter> missing_changes = new LinkedList<ChangeFromWriter>();
		
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.status == ChangeFromWriterStatusKind.MISSING) {
				missing_changes.add(change);
			}
		}
		
		return missing_changes;
	}

	/**
	 * 8.4.10.4.6 missing_changes_update
	 * This operation modifies the status stored in ChangeFromWriter for any changes in the WriterProxy 
	 * whose status is UNKNOWN and have sequence numbers smaller or equal to ‘last_available_seq_num.’ 
	 * The status of those changes is modified from UNKNOWN to MISSING indicating that the changes are 
	 * available at the WriterHistoryCache of the RTPS Writer represented by the RTPS WriterProxy but 
	 * have not been received by the RTPS Reader.
	 */
	public void missing_changes_update(long last_available_seq_num) {
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.status == ChangeFromWriterStatusKind.UNKNOWN &&
				change.cache_change.getSequenceNumber() <= last_available_seq_num) {
					
				change.status = ChangeFromWriterStatusKind.MISSING;
			}
		}				
	}
	
	/**
	 * 8.4.10.4.7 received_change_set
	 * This operation modifies the status of the ChangeFromWriter that refers to the CacheChange with the
	 * SequenceNumber_t ‘a_seq_num.’ The status of the change is set to ‘RECEIVED,’ indicating it has been received.
	 */
	public void received_change_set(long a_seq_num) {
		for (ChangeFromWriter change : changes_from_writer) {
			if (change.cache_change.getSequenceNumber() == a_seq_num) {
				
				change.status = ChangeFromWriterStatusKind.RECEIVED;
			}
		}				
		
	}
}
