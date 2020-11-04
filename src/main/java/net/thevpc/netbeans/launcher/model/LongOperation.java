package net.thevpc.netbeans.launcher.model;

public interface LongOperation {

    String getName();

    String getDescription();

    float getPercent();

    LongOperationStatus getStatus();

    boolean isIndeterminate();
}
