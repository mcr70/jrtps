package net.sf.jrtps.message.parameter;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContentFilterProperty parameter is sent by reader to writer, so that writer side
 * filtering is possible.
 * 
 * @author mcr70
 */
public class ContentFilterProperty extends Parameter {
	private static final Logger logger = LoggerFactory.getLogger(ContentFilterProperty.class);
	
	/**
	 * Filter class name defined by DDS specification.
	 */
	public static final String DDSSQL = "DDSSQL";
	
    private String contentFilteredTopicName; // length 256
    private String relatedTopicName; // length 256
    private String filterClassName; // length 256
    private String filterExpression;
    private String[] expressionParameters;

	private byte[] signature; // filter signature
    
	public ContentFilterProperty(String cfTopicName, String relatedTopicName,
			String filterClassName, String filterExpression) {
		this(cfTopicName, relatedTopicName, filterClassName, filterExpression, new String[0]);
	}
	
	public ContentFilterProperty(String cfTopicName, String relatedTopicName,
			String filterClassName, String filterExpression, String[] expressionParameters) {
		super(ParameterId.PID_CONTENT_FILTER_PROPERTY);
		
		this.contentFilteredTopicName = cfTopicName;
		this.relatedTopicName = relatedTopicName;
		this.filterClassName = filterClassName;
		this.filterExpression = filterExpression;
		this.expressionParameters = expressionParameters;
		
		if (contentFilteredTopicName == null || relatedTopicName == null || 
				filterClassName == null || filterExpression == null || expressionParameters == null) {
			throw new IllegalArgumentException("Arguments cannot be null");
		}
	}
    
    ContentFilterProperty() {
        super(ParameterId.PID_CONTENT_FILTER_PROPERTY);
    }
	
    /**
     * Gets name of the content filtered topic.
     * @return name of the content filtered topic
     */
	public String getContentFilteredTopicName() {
		return contentFilteredTopicName;
	}
	
	/**
	 * Gets the name of the related topic.
	 * @return name of the related topic
	 */
	public String getRelatedTopicName() {
		return relatedTopicName;
	}
	
	/**
	 * Name of filter class
	 * @return name of the filter class
	 */
	public String getFilterClassName() {
		return filterClassName;
	}
	
	/**
	 * Gets the filter expression. 
	 * @return filter expression
	 */
	public String getFilterExpression() {
		return filterExpression;
	}
	
	/**
	 * Expression parameters used by filter expression
	 * @return expression parameters
	 */
	public String[] getExpressionParameters() {
		return expressionParameters;
	}
	
	/**
	 * Gets the filter signature. Filter signature is calculated as MD5 checksum of the
	 * fields of this ContentFilterProperty. If MD5 algorithm is not available on platform
	 * where application is being run, a null will be returned. In that case writer side
	 * content filtering is not possible.
	 * 
	 * @return filter signature, or null if filter signature could not be calculated
	 */
	public byte[] getFilterSignature() {
		if (signature == null) {
	        try {
				MessageDigest md5 = MessageDigest.getInstance("MD5");
				md5.update(contentFilteredTopicName.getBytes("UTF-8"));
				md5.update(relatedTopicName.getBytes("UTF-8"));
				md5.update(filterClassName.getBytes("UTF-8"));
				md5.update(filterExpression.getBytes("UTF-8"));
				for (int i = 0; i < expressionParameters.length; i++) {
					md5.update(expressionParameters[i].getBytes("UTF-8"));
				}
				
				signature = md5.digest();
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				logger.warn("Writer side content filtering is not possible, as signature cannot be calculated due {}: {}", 
						e.getClass(), e.getMessage());
			}
		}
		
		return signature;
	}
	
	
    @Override
    public void read(RTPSByteBuffer bb, int length) {
    	this.contentFilteredTopicName = bb.read_string();
    	this.relatedTopicName = bb.read_string();
    	this.filterClassName = bb.read_string();
    	this.filterExpression = bb.read_string();
    	this.expressionParameters = new String[bb.read_long()];
    	for (int i = 0; i < expressionParameters.length; i++) {
    		expressionParameters[i] = bb.read_string();
    	}
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
    	bb.write_string(contentFilteredTopicName);
    	bb.write_string(relatedTopicName);
    	bb.write_string(filterClassName);
    	bb.write_string(filterExpression);
    	bb.write_long(expressionParameters.length);
    	for (int i = 0; i < expressionParameters.length; i++) {
    		bb.write_string(expressionParameters[i]);
    	}
    }

    public String toString() {
    	return "ContentFilterProperty: cfTopic " + contentFilteredTopicName + ", related topic " + 
    			relatedTopicName + ", filter class " + filterClassName + ", filter expression " +
    			filterExpression;
    }
}