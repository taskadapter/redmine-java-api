package com.taskadapter.redmineapi.bean;

import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;

public class TransportDecorator {
    static void decorate(Collection<?> collection, Transport transport) {
        collection.forEach(e -> {
            if (e instanceof FluentStyle) {
                ((FluentStyle) e).setTransport(transport);
            }
        });
    }
}
