package org.drift.tracker;

import org.drift.tracker.utils.StringUtils;

/**
 * @author Dima Frid
 */
public class MonitoredCall {
    public String typeName;
    public String methodName;
    public Object[] args;
    public boolean logArgs;
    private String msg;

    public MonitoredCall(String typeName, String methodName, Object[] args) {
        this.typeName = typeName;
        this.methodName = methodName;
        this.args = args;
    }

    public String getCall() {
        return typeName + "." + methodName;
    }

    String getMsg() {
        return StringUtils.isEmpty(msg) ? "" : msg + "; ";
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(typeName + "." + methodName);
        if (logArgs) {
            buf.append(" (");
            for (Object arg : args) {
                buf.append(arg + ",");
            }
            if (args.length > 0) {
                buf.deleteCharAt(buf.length() - 1);
            }
            buf.append(")");
        }
        return buf.toString();
    }
}
