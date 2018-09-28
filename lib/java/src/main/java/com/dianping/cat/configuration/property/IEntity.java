package com.dianping.cat.configuration.property;

public interface IEntity<T> {
   void accept(IVisitor visitor);

   void mergeAttributes(T other);

}
