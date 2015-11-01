/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.jpa;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Interface combining EntityManager and AutoCloseable.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public interface EntityManagerAutoCloseable extends EntityManager, AutoCloseable {
// -------------------------- STATIC METHODS --------------------------

    /**
     * creates an implementation to wrap the given EntityManager with Autoclose functionality.
     *
     * @param em
     *         the EntityManager to wrap
     * @return autoclosing implementation
     */
    static EntityManagerAutoCloseable createFor(final EntityManager em) {
        // create a Proxy for EntityManagerAutoCloseable with an anonymous InvocationHandler that contains an
        // EntityManager on which he acts; no need to  handle AutoCloseable.close() as EntityManager already has a
        // close() method, it only does not implement Autocloseable.
        return (EntityManagerAutoCloseable) Proxy.newProxyInstance(EntityManagerAutoCloseable.class.getClassLoader(),
                new Class<?>[]{EntityManagerAutoCloseable.class},
                new InvocationHandler() {
                    private EntityManager em;

                    @Override
                    public Object invoke(Object proxy1, Method method, Object[] args) throws Throwable {
                        return method.invoke(em, args);
                    }

                    private InvocationHandler init(EntityManager em) {
                        this.em = em;
                        return this;
                    }
                }.init(em));
    }
}
