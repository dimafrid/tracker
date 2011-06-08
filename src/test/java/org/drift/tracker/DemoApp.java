package org.drift.tracker;

import org.drift.tracker.utils.JmxUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author Dima Frid
 */
public class DemoApp {
    public static void main(String[] args) throws InterruptedException {
        JmxUtils.register();

        long tlcSleep = TimeUnit.SECONDS.toMillis(Long.parseLong(System.getProperty("tlcSleep", "10")));
        long mlcSleep = TimeUnit.SECONDS.toMillis(Long.parseLong(System.getProperty("mlcSleep", "20")));

        run(tlcSleep, mlcSleep);
    }

    private static void run(final long tlcSleep, final long mlcSleep) throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DemoClass obj = new DemoClass();
                obj.topLevelCall(tlcSleep, mlcSleep);
            }
        };
        Thread thread = new Thread(runnable, "demo-thr");
        thread.start();

        thread.join();
    }
}
