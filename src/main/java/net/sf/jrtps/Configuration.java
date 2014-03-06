package net.sf.jrtps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.jrtps.types.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for jRTPS. All the protocol tuning parameters mentioned in
 * specification can be obtained from this class, as well as jRTPS specific
 * configuration parameters.<br>
 * 
 * see 8.4.7.1
 * 
 * @author mcr70
 * 
 */
public class Configuration {
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);

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
            } catch (IOException e) {
                log.warn("Failed to read configuration {}. Using defaults.", resourceName, e);
            }
            finally {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn("Failed to read configuration {}. Using defaults.", resourceName, e);
                }
            }
        } else {
            log.warn("Failed to read configuration {}. Using defaults.", resourceName);
        }
    }

    /**
     * Configures the mode in which the Writer operates. If pushMode==true, then
     * the Writer will push changes to the reader. If pushMode==false, changes
     * will only be announced via heartbeats and only be sent as response to the
     * request of a reader. Note, that pushMode is applicable only to reliable writers.
     * Best effort writers are always in push mode.<p>
     * 
     * Default value for pushMode is true
     * 
     * @return pushMode
     */
    public boolean getPushMode() {
        return getBooleanProperty("rtps.writer.push-mode", true);
    }


    /**
     * Protocol tuning parameter that allows the RTPS Writer to repeatedly
     * announce the availability of data by sending a Heartbeat Message.
     * 
     * @return heartbeat period in milliseconds
     */
    public int getHeartbeatPeriod() {
        return getIntProperty("rtps.writer.heartbeat-period", 10000);
    }

    /**
     * Protocol tuning parameter that allows the RTPS Writer to delay the
     * response to a request for data from a negative acknowledgment.
     * <p>
     * See chapter 8.4.7.1.1 for default values
     * 
     * @return Nack response delay
     */
    public int getNackResponseDelay() {
        return getIntProperty("rtps.writer.nack-response-delay", 200);
    }

    /**
     * Protocol tuning parameter that allows the RTPS Writer to ignore requests
     * for data from negative acknowledgments that arrive 'too soon' after the
     * corresponding change is sent.
     * <p>
     * See chapter 8.4.7.1.1 for default values
     * 
     * @return Nack supression duration
     * 
     */
    public int getNackSuppressionDuration() {
        return getIntProperty("rtps.writer.nack-suppression-duration", 0);
    }

    /**
     * Protocol tuning parameter that allows the RTPS Reader to ignore
     * HEARTBEATs that arrive 'too soon' after a previous HEARTBEAT was
     * received.
     * 
     * @return heartbeat suppression duration in milliseconds
     */
    public int getHeartbeatSuppressionDuration() {
        return getIntProperty("rtps.reader.heartbeat-suppression-duration", 0);
    }

    /**
     * Protocol tuning parameter that allows the RTPS Reader to delay the
     * sending of a positive or negative acknowledgment (seeSection 8.4.12.2)
     * 
     * @return heartbeat response delay
     */
    public int getHeartbeatResponseDelay() {
        return getIntProperty("rtps.reader.heartbeat-response-delay", 500);
    }


    public int getBufferSize() {
        return getIntProperty("jrtps.buffer-size", 16384);
    }

    /**
     * Gets a named integer property from configuration.
     * 
     * @param key Key of the property
     * @param defltValue default value used, if property is not set, or it could not be
     *        converted to int
     * @return integer value
     */
    public int getIntProperty(String key, int defltValue) {
        String value = props.getProperty(key);
        int i = defltValue;
        if (value != null) {
            try {
                i = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                log.warn("Failed to convert value of {}: {} to integer", key, value);
            }
        }

        return i;
    }

    /**
     * Gets a named boolean property from configuration.
     * 
     * @param key Key of the property
     * @param defltValue default value used, if property is not set, or it could not be converted to boolean
     * @return boolean value
     */
    public boolean getBooleanProperty(String key, boolean defltValue) {
        String value = props.getProperty(key);
        boolean b = defltValue;
        if (value != null) {
            try {
                b = Boolean.parseBoolean(value);
            } catch (NumberFormatException nfe) {
                log.warn("Failed to convert value of {}: {} to boolean", key, value);
            }
        }

        return b;
    }

    /**
     * Get the default SPDP announcement rate.
     * see 9.6.1.4.2 Default announcement rate
     * @return resend period.
     */
    public Duration getSPDPResendPeriod() {
        int millis = getIntProperty("rtps.spdp.resend-data-period", 30000); 

        return new Duration(millis);
    }

    /**
     * Gets the size of message queue. 
     * @return queue size
     */
    public int getMessageQueueSize() {
        return getIntProperty("jrtps.message-queue.size", 10);
    }

    /**
     * get whether or not multicast is preferred.
     * @return true or false
     */
    public boolean preferMulticast() {
        return getBooleanProperty("jrtps.prefer-multicast", true);
    }
    
    /**
     * Boolean configuration option. If true, builtin data for builtin entities is published.
     * @return true or false
     */
    public boolean getPublishBuiltinEntities() {
        return getBooleanProperty("jrtps.publish-builtin-data", false);
    }
}
