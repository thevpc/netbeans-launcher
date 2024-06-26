package net.thevpc.netbeans.launcher.model;

import net.thevpc.netbeans.launcher.service.NetbeansLauncherModule;

public class DefaultLongOperation implements WritableLongOperation {
    private String name;
    private String description;
    private float percent;
    private boolean indeterminate;
    private LongOperationStatus status;
    private NetbeansLauncherModule module;

    public DefaultLongOperation(NetbeansLauncherModule module) {
        this.module = module;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public float getPercent() {
        return percent;
    }

    @Override
    public LongOperationStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(LongOperationStatus status) {
        this.status = status;
    }

    @Override
    public boolean isIndeterminate() {
        return indeterminate;
    }

    public DefaultLongOperation setIndeterminate(boolean indeterminate) {
        this.indeterminate = indeterminate;
        return this;
    }

    @Override
    public void start(boolean indeterminate) {
        this.setIndeterminate(indeterminate);
        this.setStatus(LongOperationStatus.STARTED);
        this.setPercent(0);
        module.rt().fire(this);
    }

    @Override
    public void setPercent(float v) {
        if (!isIndeterminate()) {
            float p = v;
            if (p <= 0) {
                p = 0;
            }
            if (p >= 100) {
                p = 100;
            }
            this.percent = p;

        } else {
            this.percent = 0;
        }
        module.rt().fire(this);
    }

    @Override
    public void inc(float v) {
        if (!isIndeterminate()) {
            setPercent(getPercent() + v);
        }
    }

    @Override
    public void end() {
        this.status = LongOperationStatus.ENDED;
        module.rt().fire(this);
    }

}
