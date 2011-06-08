package org.drift.tracker;

import org.drift.tracker.utils.ThreadUtils;
import org.junit.Test;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Dima Frid
 */
public class TrackerTest {

    @Test
    public void testInSameThread() {
        DemoClass testClass = new DemoClass();

        testClass.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                Map<Thread, Stack<CallInProgress>> calls = TrackerFactory.getTracker().getCalls();
                assertNotNull(calls);
                assertEquals(1, calls.size());
                assertTrue(calls.get(Thread.currentThread()).peek().call.methodName.equals("topLevelCall"));
            }
        });

        testClass.topLevelCall(0, 0);
    }

    @Test
    public void testAsync() {
        final DemoClass testClass = new DemoClass();

        final long sleep = TimeUnit.SECONDS.toMillis(10);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                testClass.topLevelCall(sleep, sleep);
            }
        };
        Thread thr = new Thread(runnable, "test-thr");
        thr.start();

        ThreadUtils.sleep(sleep / 10);

        Map<Thread, Stack<CallInProgress>> calls = TrackerFactory.getTracker().getCalls();
        assertNotNull(calls);
        assertEquals(1, calls.size());
        assertTrue(calls.get(thr).peek().call.methodName.equals("topLevelCall"));
    }
}
