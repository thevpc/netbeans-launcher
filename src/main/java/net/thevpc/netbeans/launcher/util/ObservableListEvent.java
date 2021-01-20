package net.thevpc.netbeans.launcher.util;

public class ObservableListEvent<T> implements ObservableEvent{
    private ObservableList<T> observable;
    private T oldValue;
    private T newValue;
    private int index;
    private EventType type;

    public ObservableListEvent(ObservableList<T> observable,T oldValue, T newValue, int index,EventType type) {
        this.observable = observable;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.index = index;
        this.type = type;
    }

    public ObservableList<T> getObservable() {
        return observable;
    }

    public T getOldValue() {
        return oldValue;
    }

    public T getNewValue() {
        return newValue;
    }

    public int getIndex() {
        return index;
    }

    public EventType getEventType() {
        return type;
    }
}
