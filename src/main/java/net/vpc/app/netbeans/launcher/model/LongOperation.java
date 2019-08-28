package net.vpc.app.netbeans.launcher.model;

public interface LongOperation {

    String getName();

    String getDescription();

    float getPercent();

    LongOperationStatus getStatus();

    boolean isIndeterminate();
}
