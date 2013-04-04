package com.foreach.imageserver.business.container;

import java.util.List;

public interface CircularArrayList<E> {
    void push(E e);

    List<E> popAll();
}