package org.drift.tracker.utils;

import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dima Frid
 */
public class ThreadUtils {

    public static String getCurrentThreadStatus() {
        Indentor dumper = new Indentor();
        dumpThreadStatus(Thread.currentThread().getId(), dumper);
        return dumper.toString();
    }

    public static String getThreadDump() {
        Indentor dumper = new Indentor();

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        dumper.append("Found " + bean.getThreadCount() + " live threads");

        dumper.append(getDeadlockedThreads());
        dumper.newline();

        long[] threadIds = bean.getAllThreadIds();
        ThreadInfo[] allInfos = bean.getThreadInfo(threadIds, Integer.MAX_VALUE);
        for (ThreadInfo threadInfo : allInfos) {
            if (threadInfo == null) {
                continue;
            }

            if (threadInfo.getThreadName().equals(Thread.currentThread().getName())) {
                continue;
            }

            dumpThreadStatus(threadInfo, dumper);
            dumper.newline();
        }

        return dumper.toString();
    }

    public static String getDeadlockedThreads() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        long[] deadlocked = bean.findMonitorDeadlockedThreads();
        if (deadlocked == null || deadlocked.length == 0) {
            return "";
        }

        String buf = "Deadlocked thread IDs: ";
        for (long id : deadlocked) {
            buf += id + " ";
        }

        return buf;
    }

    public static String[] getStackTrace(long threadID) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadID, Integer.MAX_VALUE);

        List<String> buf = new ArrayList<String>();

        if (threadInfo == null) {
            return buf.toArray((String[])Array.newInstance(String.class, buf.size()));
        }

        StackTraceElement[] stackTrace = threadInfo.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement ste = stackTrace[i];
            buf.add("\t" + ste.toString());

            if (i == 0 && threadInfo.getLockInfo() != null) {
                Thread.State ts = threadInfo.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        buf.add("\t-  blocked on " + threadInfo.getLockInfo());
                        break;
                    case WAITING:
                        buf.add("\t-  waiting on " + threadInfo.getLockInfo());
                        break;
                    case TIMED_WAITING:
                        buf.add("\t-  waiting on " + threadInfo.getLockInfo());
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : threadInfo.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    buf.add("\t-  locked " + mi);
                }
            }
        }

        LockInfo[] locks = threadInfo.getLockedSynchronizers();
        if (locks.length > 0) {
            buf.add("\n\tNumber of locked synchronizers = " + locks.length);
            for (LockInfo li : locks) {
                buf.add("\t- " + li);
            }
        }

        return buf.toArray((String[])Array.newInstance(String.class, buf.size()));
    }

    public static void dumpThreadStatus(long threadID, Indentor dumper) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadID, Integer.MAX_VALUE);
        dumpThreadStatus(threadInfo, dumper);
    }

    public static void dumpThreadStatus(ThreadInfo threadInfo, Indentor dumper) {
        if (threadInfo == null) {
            return;
        }

        String buf = getThreadHeadline(threadInfo);
        dumper.append(buf);

        String[] stack = getStackTrace(threadInfo.getThreadId());
        for (String ste : stack) {
            dumper.append(ste);
        }
    }

    public static String getThreadHeadline(long threadID) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadID, Integer.MAX_VALUE);
        return getThreadHeadline(threadInfo);
    }

    public static String getThreadHeadline(ThreadInfo threadInfo) {
        String buf =
            "\"" + threadInfo.getThreadName() + "\"" +
            " Id=" + threadInfo.getThreadId() + " " +
            threadInfo.getThreadState();

        if (threadInfo.getLockName() != null) {
            buf += " on " + threadInfo.getLockName();
        }

        if (threadInfo.getLockOwnerName() != null) {
            buf += " owned by \"" + threadInfo.getLockOwnerName() + "\" Id=" + threadInfo.getLockOwnerId();
        }

        if (threadInfo.isSuspended()) {
            buf += " (suspended)";
        }

        if (threadInfo.isInNative()) {
            buf += " (in native)";
        }

        return buf;
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) { }
    }
}
