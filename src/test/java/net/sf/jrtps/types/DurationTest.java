package net.sf.jrtps.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DurationTest {
    @Test
    public void testMillisConstructor() {
        Duration d1 = new Duration(1000);
        assertEquals(1, d1.getSeconds());
        assertEquals(0, d1.getNanoSeconds());
        
        Duration d2 = new Duration(1);
        assertEquals(0, d2.getSeconds());
        assertEquals(1000000, d2.getNanoSeconds());
    }


    @Test
    public void testAsMillis() {
        Duration d1 = new Duration(1000);
        assertEquals(1000, d1.asMillis());

        Duration d2 = new Duration(1, 1000000);
        assertEquals(1001, d2.asMillis());
    }
    
    @Test
    public void testInfinite() {
        Duration d = Duration.INFINITE;
        
        assertTrue(d.isInfinite());
    }
}
