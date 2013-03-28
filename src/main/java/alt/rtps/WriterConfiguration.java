package alt.rtps;

import alt.rtps.types.Duration_t;
/**
 * 
 * @author mcr70
 * @see 8.4.7.1
 */
public class WriterConfiguration {

	private boolean pushMode = true;	
	private Duration_t heartbeatPeriod = new Duration_t(5, 0); // 5 sec, tunable
	private Duration_t nackResponseDelay = new Duration_t(0, 200000000); // 200 ms
	private Duration_t nackSuppressionDuration = new Duration_t(0, 0); // 0, tunable
	

	public WriterConfiguration() {
	}
		
	/**
	 * Configures the mode in which the Writer operates. If pushMode==true, then the
	 * Writer will push changes to the reader. If pushMode==false, changes will only be 
	 * announced via heartbeats and only be sent as response to the request of a reader.
	 */	
	public boolean pushMode() {
		return pushMode;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce the
	 * availability of data by sending a Heartbeat Message.
	 * @return
	 */
	public Duration_t heartbeatPeriod() {
		return heartbeatPeriod;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to delay
	 * the response to a request for data from a negative acknowledgment.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return
	 */
	public Duration_t nackResponseDelay() {
		return nackResponseDelay;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to ignore requests for data from
	 * negative acknowledgments that arrive ‘too soon’ after the corresponding change is sent.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return 
	 * 
	 */
	public Duration_t nackSupressionDuration() {
		return nackSuppressionDuration;
	}
}
