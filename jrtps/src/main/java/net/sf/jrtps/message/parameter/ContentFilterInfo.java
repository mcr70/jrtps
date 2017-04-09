package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * ContentFilterInfo is transmitted with Data submessage as an inline-qos parameter.
 * Writer informs reader which filters was applied to Data and whether  
 * corresponding filter passed given filter or not.
 * Each bit in bitmaps (filterResult) represents one FilterSignature. First bit is 
 * reserved for FilterSignature[0] etc.
 * 
 * @author mcr70
 */
public class ContentFilterInfo extends Parameter implements InlineQoS {
	private int[] bitmaps;
	private FilterSignature[] signatures;
	
	ContentFilterInfo() {
        super(ParameterId.PID_CONTENT_FILTER_INFO);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
    	this.bitmaps = new int[bb.read_long()];
    	for (int i = 0; i < bitmaps.length; i++) {
    		bitmaps[i] = bb.read_long();
    	}
    	
    	this.signatures = new FilterSignature[bb.read_long()];
    	for (int i = 0; i < signatures.length; i++) {
    		signatures[i] = new FilterSignature(bb);
    	}
    }

    public boolean containsSignature(byte[] signature) {
    	for (int i = 0; i < signatures.length; i++) {
    		if (Arrays.equals(signature, signatures[i].getSignature())) {
    			int bitMapIdx = i / 32;
    			int bitIdx = i % 32;
    			int bitMap = bitmaps[bitMapIdx] << bitIdx; // MSB contains the bit
    			
    			return (bitMap & 0x80000000) == 0x80000000;
    		}
    	}
    	
    	return false;
    }
    
    @Override
    public void writeTo(RTPSByteBuffer bb) {
    	bb.write_long(bitmaps.length);
    	for (int i = 0; i < bitmaps.length; i++) {
    		bb.write_long(bitmaps[i]);
    	}
    	
    	bb.write_long(signatures.length);
    	for (int i = 0; i < signatures.length; i++) {
    		bb.write(signatures[i].signature);
    	}
    }
    
    public class FilterSignature {
    	private byte[] signature;
    	
    	FilterSignature(RTPSByteBuffer bb) {
    		signature = new byte[16]; // length of MD5 digest 
    	}
    	
    	/**
    	 * Gets the signature bytes. Signature bytes is an MD5 checksum
    	 * of strings in ContentFilterProperty. 
    	 * See 9.6.3.1 Content filter info (PID_CONTENT_FILTER_INFO) for
    	 * more information.
    	 *  
    	 * @return signature bytes
    	 */
    	public byte[] getSignature() {
			return signature;
		}
    }
}