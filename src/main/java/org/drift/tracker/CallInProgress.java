package org.drift.tracker;

/**
 * @author Dima Frid
 */
public class CallInProgress {
    MonitoredCall call;
    long timestamp;

    CallInProgress(MonitoredCall call) {
        this.call = call;
        timestamp = System.currentTimeMillis();
    }
}
