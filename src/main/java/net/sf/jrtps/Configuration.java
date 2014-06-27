package net.sf.jrtps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sf.jrtps.transport.PortNumberParameters;
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

    /**
     * Creates a Configuration from resource named <i>/jrtps.properties</i>
     */
    public Configuration() {
        this("/jrtps.properties");
    }

    /**
     * Creates a new Configuration with given resourceName. If given resourceName
     * is not found, a default Configuration is constructed.
     * 
     * @param resourceName name of the resource given to {@link Class#getResourceAsStream(String)} 
     */
    public Configuration(String resourceName) {
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

    /**
     * Gets PortNumberParamers from the configuation file. If a port number parameter
     * is missing, its default value is set into returned PortNumberParameters
     * @return PortNumberParameters
     */
    public PortNumberParameters getPortNumberParameters() {
        String[] params = getStringArrayProperty("rtps.traffic.port-config", 
                new String[] {"PB=7400", "DG=250", "PG=2", "d0=0", "d1=10", "d2=1", "d3=11"});
        
        int pb = 7400; int dg = 250; int pg = 2;
        int d0 = 0; int d1 = 10; int d2 = 1; int d3 = 11;

        for (String s : params) {
            String[] kv = s.split("=");
            if (kv.length == 2) {
                String k = kv[0].trim();
                String v = kv[1].trim();
                
                if ("PB".equalsIgnoreCase(k)) {
                    pb = convert(v, 7400);
                }
                else if ("DG".equalsIgnoreCase(k)) {
                    dg = convert(v, 250);
                } 
                else if ("PG".equalsIgnoreCase(k)) {
                    pg = convert(v, 2);
                } 
                else if ("d0".equalsIgnoreCase(k)) {
                    d0 = convert(v, 0);
                } 
                else if ("d1".equalsIgnoreCase(k)) {
                    d1 = convert(v, 10);
                } 
                else if ("d2".equalsIgnoreCase(k)) {
                    d2 = convert(v, 1);
                } 
                else if ("d3".equalsIgnoreCase(k)) {
                    d3 = convert(v, 11);
                }
                else {
                    log.warn("Variable '{}' in rtps.traffic.port-config is not one of PB, DG, PG, d0, d1, d2 or d3", k);
                }
            }
            else {
                log.warn("Variable '{}' in rtps.traffic.port-config is not in format <var>=<int>, ignoring it.", s);
            }
        }
        
        return new PortNumberParameters(pb, dg, pg, d0, d1, d2, d3);
    }
    
    /**
     * Gets the listener URIs
     * @return Listener URIs
     */
    public List<URI> getListenerURIs() {
        return __getListenerURIs("jrtps.listener-uris");
    }


    /**
     * Gets the listener URIs for discovery
     * @return Listener URIs
     */
    public List<URI> getDiscoveryListenerURIs() {
        return __getListenerURIs("jrtps.discovery.listener-uris");
    }
    
    /**
     * Gets whether or not udds DataWriter writes collections coherently or not.
     * 
     * @return true, if writer writes collections coherently 
     */
    public boolean getWriteCollectionsCoherently() {
        return getBooleanProperty("udds.collections.coherent", true);
    }
    
    
    private String[] getStringArrayProperty(String key, String[] deflt) {
        String property = props.getProperty(key);
        
        if (property == null) {
            return deflt;
        }
        
        return property.split(",");
    }

    private int convert(String v, int dflt) {
        try {
            return Integer.parseInt(v);
        }
        catch(Exception e) {
            log.warn("Failed to convert {} to int, using default value of {}", v, dflt);
        }
        
        return dflt;
    }

    private List<URI> __getListenerURIs(String prop) {
        String[] uriStrings = getStringArrayProperty(prop, new String[] {"udp://239.255.0.1", "udp://localhost"});

        List<URI> uriList = new LinkedList<>();
        for (String s : uriStrings) {
            try {
                uriList.add(new URI(s.trim()));
            } catch (URISyntaxException e) {
                log.error("Invalid URI", e);
            }
        }
        
        return uriList;
    }
}
