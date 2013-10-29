package net.sf.jrtps.types;

/**
 * see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in Topic Data
 * 
 * @author mcr70
 * 
 */
public class StatusInfo_t {
	private byte[] value;
	
	public StatusInfo_t(byte[] bytes) {
		value = bytes;
		
		assert bytes != null && bytes.length == 4;
	}
}
