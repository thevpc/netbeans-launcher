package net.thevpc.netbeans.launcher.util;

public interface ObservableEvent {
    EventType getEventType();

    public static enum EventType{
        UPDATE, ADD_ELEMENT, REMOVE_ELEMENT, UPDATE_ELEMENT
    }
}
