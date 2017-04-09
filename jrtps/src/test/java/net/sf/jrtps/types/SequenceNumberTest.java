package net.sf.jrtps.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SequenceNumberTest {
    @Test
    public void testLongConversion() {
        SequenceNumber sn = new SequenceNumber(0, -1);        
        assertEquals(0x00000000ffffffff, sn.getAsLong());
        
        sn = new SequenceNumber(0x00000001ffffffffL);
        assertEquals(1, sn.getHighBytes());
        assertEquals(-1, sn.getLowBytes());
        
        sn = new SequenceNumber(0xffffffff00000001L);
        assertEquals(-1, sn.getHighBytes());
        assertEquals(1, sn.getLowBytes());

        sn = new SequenceNumber(-1, 0);
        assertEquals(0xffffffff00000000L, sn.getAsLong());
        assertEquals(-1, sn.getHighBytes());
        assertEquals(0, sn.getLowBytes());
    }
}
