package org.drift.tracker;

/**
 * @author Dima Frid
 */
public class CallStats {
    public String call;
    public int invocationCount;
    public long totalElapsedTime;
    public long maxElapsedTime = 0;

    CallStats(String call) {
        this.call = call;
    }

    void incrementInvocationCount() {
        invocationCount++;
    }

    public void addElapsedTime(long elapsedTime) {
        totalElapsedTime += elapsedTime;
        if (maxElapsedTime < elapsedTime) {
            maxElapsedTime = elapsedTime;
        }
    }
}
