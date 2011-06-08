package org.drift.tracker;

import java.util.Map;
import java.util.Stack;

/**
 * @author Dima Frid
 */
public interface Tracker {
    void registerCall(MonitoredCall call);

    void deregisterCall();

    Map<Thread, Stack<CallInProgress>> getCalls();

    Map<String, CallStats> getCallStats();

    void clearCallStats();
}
