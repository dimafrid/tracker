package org.drift.tracker.jmx;

import org.drift.tracker.CallInProgress;
import org.drift.tracker.CallStats;
import org.drift.tracker.StatusSummaries;
import org.drift.tracker.Tracker;
import org.drift.tracker.TrackerFactory;
import org.drift.tracker.utils.Indentor;
import org.drift.tracker.utils.ThreadUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Dima Frid
 */
public class MonitoringJmx implements MonitoringJmxMBean {

    @Override
    public String getTopInvocations(String trackerName) {
        Comparator<CallStats> comparator = new Comparator<CallStats>() {
            @Override
            public int compare(CallStats o1, CallStats o2) {
                if (o1.invocationCount > o2.invocationCount) {
                    return -1;
                } else if (o1.invocationCount == o2.invocationCount) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
        return getTopStats(comparator, trackerName);
    }

    @Override
    public String getTopAvgElapsed(String trackerName) {
        Comparator<CallStats> comparator = new Comparator<CallStats>() {
            @Override
            public int compare(CallStats o1, CallStats o2) {
                double o1avg = (((double) o1.totalElapsedTime) / o1.invocationCount);
                double o2avg = (((double) o2.totalElapsedTime) / o2.invocationCount);
                if (o1avg > o2avg) {
                    return -1;
                } else if (o1avg == o2avg) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
        return getTopStats(comparator, trackerName);
    }

    @Override
    public String getTopElapsed(String trackerName) {
        Comparator<CallStats> comparator = new Comparator<CallStats>() {
            @Override
            public int compare(CallStats o1, CallStats o2) {
                if (o1.totalElapsedTime > o2.totalElapsedTime) {
                    return -1;
                } else if (o1.totalElapsedTime == o2.totalElapsedTime) {
                    return 0;
                } else {
                    return 1;
                }
            }
        };
        return getTopStats(comparator, trackerName);
    }

    private String getTopStats(Comparator<CallStats> comparator, String trackerName) {
        Collection<CallStats> stats = TrackerFactory.getTracker(trackerName).getCallStats().values();
        List<CallStats> sorted = new ArrayList<CallStats>(stats);
        Collections.sort(sorted, comparator);

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (i >= sorted.size()) {
                break;
            }

            CallStats st = sorted.get(i);
            long totalElapsedTime = st.totalElapsedTime;
            int count = st.invocationCount;
            buf.append("\nCall: " + st.call + " => invocations: " + count +
                    ", total elapsed: " + totalElapsedTime + ", average elapsed: " + (((double) totalElapsedTime) / count) +
                    ", max elapsed: " + st.maxElapsedTime);
        }
        return buf.toString();
    }

    @Override
    public String getAllThreads() {
        return ThreadUtils.getThreadDump();
    }

    @Override
    public void clearStatistics(String trackerName) {
        TrackerFactory.getTracker(trackerName).clearCallStats();
    }

    @Override
    public String getActiveApplicationThreads() {
        Indentor indentor = new Indentor();
        Map<String, Tracker> trackers = TrackerFactory.getTrackers();
        for (Map.Entry<String, Tracker> entry : trackers.entrySet()) {
            indentor.append(ThreadUtils.getDeadlockedThreads());
            indentor.newline();

            String trackerName = entry.getKey();
            indentor.append("Tracker: " + trackerName);

            Tracker tracker = entry.getValue();
            Map<Thread, Stack<CallInProgress>> map = tracker.getCalls();
            for (Map.Entry<Thread, Stack<CallInProgress>> threadEntry : map.entrySet()) {
                Thread thread = threadEntry.getKey();
                ThreadUtils.dumpThreadStatus(thread.getId(), indentor);

                Stack<CallInProgress> calls = threadEntry.getValue();
                StatusSummaries.appendCalls(calls, indentor);
            }
        }

        return indentor.toString();
    }

    @Override
    public void setMonitoredCallTimeout(int timeout) {
        StatusSummaries.setCallTimeout(timeout);
    }
}
