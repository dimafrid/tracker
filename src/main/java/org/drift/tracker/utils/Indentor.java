package org.drift.tracker.utils;

/**
 * @author Dima Frid
 */
public class Indentor {
    private StringBuilder indent;
    private StringBuilder buf;

    public Indentor() {
        this(new StringBuilder());
    }

    public Indentor(StringBuilder indent) {
        this.indent = indent;
        buf = new StringBuilder();
    }

    public void increaseIndent() {
        indent.append("\t");
    }

    public void decreaseIndent() {
        indent.delete(indent.lastIndexOf("\t"), indent.length());
    }

    public void append(String info) {
        if (info == null || info.length() == 0) {
            return;
        }
        
        buf.append(indent).append(info).append("\n");
    }

    public void newline() {
        buf.append("\n");
    }

    public String toString() {
        return buf.toString();
    }
}
