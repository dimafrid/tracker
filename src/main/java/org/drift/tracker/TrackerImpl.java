package org.drift.tracker;

import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dima Frid
 */
public class TrackerImpl implements Tracker {
    private Map<Thread, Stack<CallInProgress>> calls = new ConcurrentHashMap<Thread, Stack<CallInProgress>>();
    private Map<String, CallStats> callStats = new ConcurrentHashMap<String, CallStats>();


    @Override
    public void registerCall(MonitoredCall call) {
        Thread thread = Thread.currentThread();
        Stack<CallInProgress> stack = calls.get(thread);
        if (stack == null) {
            stack = new Stack<CallInProgress>();
            calls.put(thread, stack);
        }
        stack.push(new CallInProgress(call));

        CallStats stats = callStats.get(call.getCall());
        if (stats == null) {
            stats = new CallStats(call.getCall());
            callStats.put(call.getCall(), stats);
        }
        stats.incrementInvocationCount();
    }

    @Override
    public void deregisterCall() {
        Thread thread = Thread.currentThread();
        Stack<CallInProgress> stack = calls.get(thread);
        if (stack != null) {
            CallInProgress callInProgress = stack.pop();
            if (stack.empty()) {
                calls.remove(thread);
            }

            CallStats stats = callStats.get(callInProgress.call.getCall());
            if (stats != null) {
                stats.addElapsedTime(System.currentTimeMillis() - callInProgress.timestamp);
            }
        }
    }

    @Override
    public Map<Thread, Stack<CallInProgress>> getCalls() {
        return Collections.unmodifiableMap(calls);
    }

    @Override
    public Map<String, CallStats> getCallStats() {
        return callStats;
    }

    @Override
    public void clearCallStats() {
        callStats.clear();
    }
}
