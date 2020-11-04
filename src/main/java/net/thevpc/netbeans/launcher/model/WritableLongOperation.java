package net.thevpc.netbeans.launcher.model;

public interface WritableLongOperation extends LongOperation{
    void setName(String name);

    void setDescription(String description);

    void start(boolean determinate);

    void setPercent(float v);

    void setStatus(LongOperationStatus status);

    void inc(float v);

    void end();
}
