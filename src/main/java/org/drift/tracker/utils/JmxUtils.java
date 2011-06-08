package org.drift.tracker.utils;

import org.drift.tracker.jmx.MonitoringJmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Dima Frid
 */
public class JmxUtils {
    public static void register() {
        MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

        try {
            mbeanServer.registerMBean(new MonitoringJmx(), new ObjectName("tracker:service=Monitoring"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
