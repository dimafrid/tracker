package org.drift.tracker;

import org.drift.tracker.utils.Indentor;
import org.drift.tracker.utils.StringUtils;
import org.drift.tracker.utils.ThreadUtils;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * @author Dima Frid
 */
public class StatusSummaries {
    private static DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final DecimalFormat formatter = new DecimalFormat();

    static {
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(1);
        formatter.setDecimalSeparatorAlwaysShown(true);
        formatter.setGroupingUsed(false);
    }

    private static int callTimeout = 30000;

    public String getSlowCallsAsString(String trackerName) {
        Tracker tracker = TrackerFactory.getTracker(trackerName);
        Map<Thread, Stack<CallInProgress>> map = tracker.getCalls();
        Indentor indentor = new Indentor();
        for (Map.Entry<Thread, Stack<CallInProgress>> threadEntry : map.entrySet()) {
            Thread thread = threadEntry.getKey();
            Stack<CallInProgress> calls = threadEntry.getValue();

            CallInProgress firstCall;
            try {
                firstCall = calls.peek();
            } catch (EmptyStackException e) {
                continue;
            }

            long startTime = firstCall.timestamp;
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > callTimeout) {
                indentor.newline();
                indentor.append("Maximum time allowed for application call [" + firstCall.call + "] elapsed (" + elapsed + " ms)");
                indentor.newline();
                ThreadUtils.dumpThreadStatus(thread.getId(), indentor);
                appendCalls(calls, indentor);
            }
        }

        String message = indentor.toString();
        if (!StringUtils.isEmpty(message)) {
            indentor.newline();
            indentor.append(ThreadUtils.getThreadDump());
            return indentor.toString();
        }

        return "";
    }

    public static String getJVMStatusAsString() {
        String str = getMemoryState();

        str += "; " + getCLState();

        str += "; " + getThreadState();

        return str;
    }

    private static String getCLState() {
        ClassLoadingMXBean clBean = ManagementFactory.getClassLoadingMXBean();
        String str = "CLASSES: ";

        long total = clBean.getTotalLoadedClassCount();
        str += "[Loaded: " + total;

        long unloaded = clBean.getUnloadedClassCount();
        str += ", Unloaded: " + unloaded;

        str += ", Left: " + (total - unloaded);

        str += "]";

        return str;
    }

    private static String getThreadState() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        return "THREADS: [Count: " + threadBean.getThreadCount() + "]";
    }

    private static String getMemoryState() {
        MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = bean.getHeapMemoryUsage();

        String str = "HEAP: ";

        str += "[USAGE: " + toMegs(heap.getUsed());

        str += ", FREE: " + toMegs(heap.getCommitted() - heap.getUsed());

        str += ", TOTAL: " + toMegs(heap.getCommitted());

        str += ", MAX: " + toMegs(heap.getMax());

        str += "];";

        MemoryUsage nonHeap = bean.getNonHeapMemoryUsage();

        str += " PERM: ";

        str += "[USAGE: " + toMegs(nonHeap.getUsed());

        str += ", FREE: " + toMegs(nonHeap.getCommitted() - nonHeap.getUsed());

        str += ", MAX: " + toMegs(nonHeap.getMax());

        str += "]";

        return str;
    }

    private static String toMegs(long val) {
        double megs = ((double) val) / 1024 / 1024;
        return formatter.format(megs);
    }

    public static String getAllCallsAsString(Stack<CallInProgress> calls) {
        Indentor indentor = new Indentor();
        appendCalls(calls, indentor);
        return indentor.toString();
    }

    public static void appendCalls(Stack<CallInProgress> calls, Indentor indentor) {
        indentor.newline();
        indentor.append("Calls:");
        indentor.increaseIndent();
        List<CallInProgress> callList = new ArrayList<CallInProgress>(calls);
        for (CallInProgress callInProgress : callList) {
            MonitoredCall call = callInProgress.call;
            long startTime = callInProgress.timestamp;
            long elapsed = System.currentTimeMillis() - startTime;
            String formattedStartTime = dateFormatter.format(new Date(startTime));
            indentor.append("Time: started at " + formattedStartTime + ", elapsed: " + elapsed + " ms");
            indentor.append("Call: " + call);
            indentor.newline();
        }
        indentor.decreaseIndent();
    }

    public static void setCallTimeout(int _callTimeout) {
        callTimeout = _callTimeout;
    }
}
