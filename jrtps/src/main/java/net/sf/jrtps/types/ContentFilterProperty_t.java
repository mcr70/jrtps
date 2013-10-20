package net.sf.jrtps.types;

public class ContentFilterProperty_t {
	private String contentFilteredTopicName; // length 256
	private String relatedTopicName; // length 256
	private String filterClassName; // length 256
	private String filterExpression;
	private String[] expressionParameters;
}
