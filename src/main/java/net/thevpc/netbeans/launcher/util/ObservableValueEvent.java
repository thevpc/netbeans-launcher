package net.thevpc.netbeans.launcher.util;

public class ObservableValueEvent<T> implements ObservableEvent{
    private ObservableValue<T> observable;
    private T oldValue;
    private T newValue;
    private EventType type;

    public ObservableValueEvent(ObservableValue<T> observable,T oldValue, T newValue, EventType type) {
        this.observable = observable;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.type = type;
    }

    public ObservableValue<T> getObservable() {
        return observable;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    public EventType getEventType() {
        return type;
    }
}
