package alt.rtps;

import alt.rtps.types.Duration_t;

public class Reader {
	/**
	 * Specifies whether the RTPS Reader expects in-line QoS to be sent along with any data.
	 */
	boolean expectsInlineQos = false;
	/**
	 * Protocol tuning parameter that allows the RTPS Reader to ignore HEARTBEATs that
	 * arrive ‘too soon’ after a previous HEARTBEAT was received.
	 */
	Duration_t heartbeatSuppressionDuration = new Duration_t(0, 0);
	
	Duration_t heartbeatResponseDelay = new Duration_t(0, 500000000); // 500 ms

}

