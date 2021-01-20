package net.thevpc.netbeans.launcher.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ObservableValue<T> {
    private List<ObservableValueListener<T>> observers = new ArrayList<>();
    private T value;

    public ObservableValue(T value) {
        this.value = value;
    }

    public ObservableValue<T> addListener(ObservableValueListener<T> a) {
        observers.add(a);
        return this;
    }

    public ObservableValue<T> set(T t) {
        T old = this.value;
        if (!Objects.equals(old, t)) {
            this.value = t;
            ObservableValueEvent<T> event = new ObservableValueEvent<>(
                    this, old, t, ObservableEvent.EventType.UPDATE
            );
            for (ObservableValueListener<T> observer : observers) {
                observer.onChange(event);
            }
        }
        return this;
    }

    public T get() {
        return value;
    }

    public interface ObservableValueListener<T> {
        void onChange(ObservableValueEvent<T> t);
    }
}
