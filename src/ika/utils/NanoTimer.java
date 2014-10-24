package ika.utils;
/*
 * NanoTimer.java
 *
 * Created on March 23, 2005, 6:30 PM
 */

//CD/examples/ch05/time/NanoTimer/NanoTimer.java

import sun.misc.Perf;


/**
 * NanoTimer<br>
 * A utility class for measuring time differences in nano seconds.<br>
 * From "Java fuer Mac OS X" by Thomas Much.<br>
 * See the main() method for an example of how to use NanoTimer.
 */
public class NanoTimer {
    
    private sun.misc.Perf timer;
    private long freq;
    
    
    public NanoTimer() {
        
        timer = Perf.getPerf();
        freq  = timer.highResFrequency();
    }
      
    public long nanoTime() {
        
        return ( timer.highResCounter() * 1000000000L / freq );
    }
    
    /*
    public static void main(String[] args) {       
        NanoTimer time = new NanoTimer();
        long startTime = time.nanoTime();
        long nanoDif = time.nanoTime() - startTime;
        System.out.println( nanoDif );
    }
     */
}

