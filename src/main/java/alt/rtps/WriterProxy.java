package alt.rtps;

import alt.rtps.types.GUID_t;
import alt.rtps.types.Locator_t;

/**
 * WriterProxy represents a remote writer
 * 
 * @author mcr70
 *
 */
class WriterProxy {
	GUID_t writerGuid;
	Locator_t locator;
	long writerSeqNum = 0;
	long ackedSeqNum = 0;
}
