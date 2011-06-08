package org.drift.tracker.jmx;

/**
 * @author Dima Frid
 */
public interface MonitoringJmxMBean {
    String getTopInvocations(String trackerName);

    String getTopAvgElapsed(String trackerName);

    String getTopElapsed(String trackerName);

    String getActiveApplicationThreads();

    String getAllThreads();

    void clearStatistics(String trackerName);

    void setMonitoredCallTimeout(int timeout);
}
