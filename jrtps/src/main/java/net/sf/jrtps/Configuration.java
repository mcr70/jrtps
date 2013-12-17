package net.sf.jrtps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.jrtps.types.Duration_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for jRTPS.
 * All the protocol tuning parameters mentioned in specification can be obtained from 
 * this class, as well as jRTPS specific configuration parameters.<br>
 * 
 * see 8.4.7.1
 *  
 * @author mcr70
 * 
 */
public class Configuration {
	private static final Logger log = LoggerFactory.getLogger(Configuration.class);

	// --- Writer configurations -------------
	private boolean pushMode = true;	
	private long nackSuppressionDuration = 0;      // 0


	// --- Reader configurations -------------
	private long heartbeatSuppressionDuration = 0; // 0 ms

	private final Properties props;

	public Configuration() {
		this("/jrtps.properties");
	}

	Configuration(String resourceName) {
		this.props = new Properties();

		InputStream is = getClass().getResourceAsStream(resourceName);

		if (is != null) {
			try {
				props.load(is);
				log.debug("Configuration loaded: {}", props);
			} 
			catch (IOException e) {
				log.warn("Failed to read configuration {}. Using defaults.", resourceName, e);
			}
		}
		else {
			log.warn("Failed to read configuration {}. Using defaults.", resourceName);
		}
	}

	/**
	 * Configures the mode in which the Writer operates. If pushMode==true, then the
	 * Writer will push changes to the reader. If pushMode==false, changes will only be 
	 * announced via heartbeats and only be sent as response to the request of a reader.
	 * 
	 * @return pushMode
	 */	
	boolean pushMode() {
		return pushMode;
	}

	/**
	 * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce the
	 * availability of data by sending a Heartbeat Message.
	 * @return heartbeat period
	 */
	int getHeartbeatPeriod() {
		return getIntProperty("rtps.writer.nack-response-delay", 0);
	}

	/**
	 * Protocol tuning parameter that allows the RTPS Writer to delay
	 * the response to a request for data from a negative acknowledgment.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return Nack response delay
	 */
	int getNackResponseDelay() {
		return getIntProperty("rtps.writer.nack-response-delay", 200);
	}

	/**
	 * Protocol tuning parameter that allows the RTPS Writer to ignore requests for data from
	 * negative acknowledgments that arrive 'too soon' after the corresponding change is sent.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return Nack supression duration
	 * 
	 */
	long nackSupressionDuration() {
		return nackSuppressionDuration;
	}

	/**
	 * Protocol tuning parameter that allows the RTPS Reader to ignore HEARTBEATs that
	 * arrive 'too soon' after a previous HEARTBEAT was received.
	 */
	long getHeartbeatSuppressionDuration() {
		return heartbeatSuppressionDuration;
	}

	/**
	 * Protocol tuning parameter that allows the RTPS Reader to delay the sending of a
	 * positive or negative acknowledgment (seeSection 8.4.12.2)
	 * @return heartbeat response delay
	 */
	int getHeartbeatResponseDelay() {
		return getIntProperty("rtps.reader.heartbeat-response-delay", 500);
	}

	public int getBufferSize() {
		return getIntProperty("jrtps.buffer-size", 16384);
	}

	// package access to ease unit tests
	public int getIntProperty(String key, int defltValue) {
		String value = props.getProperty(key);
		int i = defltValue;
		if (value != null) {
			try {
				i = Integer.parseInt(value);
			}
			catch(NumberFormatException nfe) {
				log.warn("Failed to convert value of {}: {} to integer", key, value);
			}
		}
		
		return i;
	}

	/**
	 * Get the default SPDP announcement rate. 
	 * @return resend period.
	 */
	public Duration_t getSPDPResendPeriod() {
		int millis = getIntProperty("rtps.spdp.resend-data-period", 30000); // see 9.6.1.4.2 Default announcement rate
		
		return new Duration_t(millis);
	}

	public int getMessageQueueSize() {
		return getIntProperty("jrtps.message-queue.size", 10);
	}
}
