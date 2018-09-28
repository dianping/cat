package com.dianping.cat.status.model;

public interface IEntity<T> {
    void accept(IVisitor visitor);

    void mergeAttributes(T other);

}
