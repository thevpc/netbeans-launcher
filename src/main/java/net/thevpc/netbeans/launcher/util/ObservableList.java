package net.thevpc.netbeans.launcher.util;

import net.thevpc.netbeans.launcher.model.NetbeansWorkspace;
import net.thevpc.nuts.NutsDependencyManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class ObservableList<T> implements ObservableObject, Iterable<T>{
    private List<T> data = new ArrayList<>();
    private List<ObservableListListener<T>> observers = new ArrayList<>();

    public ObservableList<T> setAll(List<T> others) {
        if(others==null){
            clear();
        }else{
            int r = others.size();
            int m=Math.min(r,size());
            while(size()>r){
                removeAt(size()-1);
            }
            for (int i = 0; i < m; i++) {
                setAt(i,others.get(i));
            }
            int s=size();
            for (int i = m; i < r; i++) {
                add(others.get(i));
            }
        }
        return this;
    }

    public ObservableList<T> addListener(ObservableListListener<T> a) {
        observers.add(a);
        return this;
    }

    public ObservableList<T> add(T a) {
        int index = data.size();
        data.add(a);
        ObservableListEvent<T> event=new ObservableListEvent<>(
                this, null,a,index, ObservableEvent.EventType.ADD_ELEMENT
        );
        for (ObservableListListener<T> observer : observers) {
            observer.onAdd(event);
        }
        return this;
    }

    public void clear(){
        while(size()>0){
            removeAt(0);
        }
    }

    public ObservableList<T> addAt(int index, T a) {
        if (index > data.size() || index < 0) {
            throw new IllegalArgumentException("invalid index");
        } else if (index == data.size()) {
            add(a);
        } else {
            data.add(index, a);
            ObservableListEvent<T> event=new ObservableListEvent<>(
                    this, null,a,index, ObservableEvent.EventType.ADD_ELEMENT
            );
            for (ObservableListListener<T> observer : observers) {
                observer.onAdd(event);
            }
        }
        return this;
    }

    public ObservableList<T> setAt(int index, T a) {
        if (index >= data.size() || index < 0) {
            throw new IllegalArgumentException("invalid index");
        }else {
            T old=data.get(index);
            if(!Objects.equals(old,a)) {
                data.set(index, a);
                ObservableListEvent<T> event = new ObservableListEvent<>(
                        this, old, a, index, ObservableEvent.EventType.UPDATE_ELEMENT
                );
                for (ObservableListListener<T> observer : observers) {
                    observer.onUpdate(event);
                }
            }
        }
        return this;
    }

    public int size() {
        return data.size();
    }

    public T get(int index) {
        return data.get(index);
    }

    public ObservableList<T> remove(T a) {
        int index = data.indexOf(a);
        if (index >= 0) {
            removeAt(index);
        }
        return this;
    }

    public ObservableList<T> removeAt(int index) {
        if (index >= 0 && index < data.size()) {
            T old = data.remove(index);
            ObservableListEvent<T> event=new ObservableListEvent<>(
                    this, old,null,index, ObservableEvent.EventType.REMOVE_ELEMENT
            );
            for (ObservableListListener<T> observer : observers) {
                observer.onRemove(event);
            }
        }
        return this;
    }

    public Stream<T> stream() {
        return data.stream();
    }

    public T[] toArray(T[] a) {
        return data.toArray(a);
    }

    public interface ObservableListListener<T> {
        void onAdd(ObservableListEvent<T> event);

        void onRemove(ObservableListEvent<T> event);

        void onUpdate(ObservableListEvent<T> event);
    }

    public List<T> list() {
        return new ArrayList<>(data);
    }

    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
}
