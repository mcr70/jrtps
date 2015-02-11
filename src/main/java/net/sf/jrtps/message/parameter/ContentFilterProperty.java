package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * ContentFilterProperty parameter
 * @author mcr70
 */
public class ContentFilterProperty extends Parameter {
    private String contentFilteredTopicName; // length 256
    private String relatedTopicName; // length 256
    private String filterClassName; // length 256
    private String filterExpression;
    private String[] expressionParameters;
    
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
	
	public String getContentFilteredTopicName() {
		return contentFilteredTopicName;
	}
	
	public String getRelatedTopicName() {
		return relatedTopicName;
	}
	
	public String getFilterClassName() {
		return filterClassName;
	}
	
	public String getFilterExpression() {
		return filterExpression;
	}
	
	public String[] getExpressionParameters() {
		return expressionParameters;
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
}