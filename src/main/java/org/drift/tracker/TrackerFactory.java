package org.drift.tracker;

import org.drift.tracker.utils.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dima Frid
 */
public class TrackerFactory {
    public static final String DEFAULT_TRACKER_NAME = "Default";

    private static final Map<String, Tracker> trackers = new ConcurrentHashMap<String, Tracker>(10);

    public static Tracker getTracker() {
        return getTracker(DEFAULT_TRACKER_NAME);
    }

    public static Tracker getTracker(String name) {
        String theName = DEFAULT_TRACKER_NAME;
        if (!StringUtils.isEmpty(name)) {
            theName = name;
        }

        Tracker tracker = trackers.get(theName);
        if (tracker == null) {
            synchronized (trackers) {
                tracker = trackers.get(theName);
                if (tracker == null) {
                    tracker = new TrackerImpl();
                    trackers.put(theName, tracker);
                }
            }
        }
        return tracker;
    }

    public static Map<String, Tracker> getTrackers() {
        return Collections.unmodifiableMap(trackers);
    }
}
