package net.sf.jrtps.types;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.4.2.6 SequenceNumberSet
 * 
 * @author mcr70
 * 
 */
public class SequenceNumberSet {
	private SequenceNumber_t bitmapBase;
	private int[] bitmaps;

	public SequenceNumberSet(long base, int[] bitmaps) {
		this.bitmapBase = new SequenceNumber_t(base);
		this.bitmaps = bitmaps;
	}
	
	public SequenceNumberSet(RTPSByteBuffer bb) {
		bitmapBase = new SequenceNumber_t(bb);
		
		long count = bb.read_long();
		assert count <= 8 && count >= 0;
		
		bitmaps = new int[(int) count];
		for (int i = 0; i < count; i++) {
			bitmaps[i] = bb.read_long();
		}
	}

	public long getBitmapBase() {
		return bitmapBase.getAsLong();
	}
	
	public List<Long> getSequenceNumbers() {
		List<Long> seqNums = new LinkedList<Long>();
		
		long seqNum = bitmapBase.getAsLong();
		
		for (int i = 0; i < bitmaps.length; i++) {
			int bitmap = bitmaps[i];

			for (int j = 0; j < 32; j++) {
				if ((bitmap & 0x8000000) == 0x8000000) { // id the MSB matches, add a new seqnum
					seqNums.add(seqNum);
				}
				
				seqNum++;
				bitmap = bitmap << 1;
			}
		}
		
		return seqNums;
	}

	public List<Long> getMissingSequenceNumbers() {
		List<Long> seqNums = new LinkedList<Long>();
		
		long seqNum = bitmapBase.getAsLong();
		
		for (int i = 0; i < bitmaps.length; i++) {
			int bitmap = bitmaps[i];

			for (int j = 0; j < 32; j++) {
				if ((bitmap & 0x8000000) == 0x0) { // id the MSB does not matches, add a new seqnum
					seqNums.add(seqNum);
				}
				
				seqNum++;
				bitmap = bitmap << 1;
			}
		}
		
		return seqNums;
	}

	public void writeTo(RTPSByteBuffer buffer) {
		bitmapBase.writeTo(buffer);
		
		buffer.write_long(bitmaps.length);
		for (int i = 0; i < bitmaps.length; i++) {
			buffer.write_long(bitmaps[i]);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(bitmapBase.toString());
		sb.append(":[");
		for (int i = 0; i < bitmaps.length; i++) {
			sb.append("0x");
			sb.append(String.format("%04x", bitmaps[i]));

			if (i < bitmaps.length - 1) sb.append(' ');
		}
		sb.append(']');
		
		return sb.toString();
	}
}
