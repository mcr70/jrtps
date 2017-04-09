package net.sf.jrtps.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TimeTest {
    @Test
    public void testTimeSeconds() {
        long timeMillis = 1000; // 1 sec
        Time t = new Time(timeMillis);
        long timeConverted = t.timeMillis();
        
        assertEquals(timeMillis, timeConverted);
    }

    @Test
    public void testTimeFraction() {
        long timeMillis = 1; // 1 msec
        Time t = new Time(timeMillis);
        long timeConverted = t.timeMillis();
        
        assertEquals(timeMillis, timeConverted);
    }
    
    @Test
    public void testTimeSecondsFraction() {
        long timeMillis = 1001; // 1 sec, 1 msec
        Time t = new Time(timeMillis);
        long timeConverted = t.timeMillis();
        
        assertEquals(timeMillis, timeConverted);
    }

    @Test
    public void testTimeCurrentTimeMillis() {
        long currentTimeMillis = System.currentTimeMillis();
        Time t = new Time(currentTimeMillis);
        long timeMillis = t.timeMillis();
        
        assertEquals(currentTimeMillis, timeMillis);
    }
}
