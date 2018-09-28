package com.dianping.cat.configuration.client;

public interface IEntity<T> {
    void accept(IVisitor visitor);

    void mergeAttributes(T other);

}
