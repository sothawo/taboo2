/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Interface combining the EntityManagerFactory interface with the AutoCloseable interface. Implementations of this
 * Interface mjust return EntityManagerAutoCloseable instances on the createEntityManager() calls.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public interface EntityManagerFactoryAutoCloseable extends EntityManagerFactory, AutoCloseable {
// -------------------------- STATIC METHODS --------------------------

    /**
     * creates an implementation to wrap the given EntityManagerFactory with Autoclose functionality.
     *
     * @param emf
     *         the EntityManagerFactory to wrap
     * @return autoclosing implementation
     */
    static EntityManagerFactoryAutoCloseable createFor(EntityManagerFactory emf) {
        // create a Proxy for EntityManagerFactoryAutoCloseable with an anonymous InvocationHandler that contains an
        // EntityManagerFactory on which he acts; no need to handle AutoCloseable.close() as EntityManagerFactory
        // already has a close() method, it only does not implement Autocloseable.
        return (EntityManagerFactoryAutoCloseable) Proxy
                .newProxyInstance(EntityManagerFactoryAutoCloseable.class.getClassLoader(),
                        new Class<?>[]{EntityManagerFactoryAutoCloseable.class},
                        new InvocationHandler() {
                            private EntityManagerFactory emf;

                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                Object result = method.invoke(emf, args);
                                // when an EntityManager is created, wrap it up in a autocloseable proxy.
                                if (result instanceof EntityManager) {
                                    result = EntityManagerAutoCloseable.createFor((EntityManager) result);

                                }
                                return result;
                            }

                            private InvocationHandler init(EntityManagerFactory emf) {
                                this.emf = emf;
                                return this;
                            }
                        }.init(emf));
    }
}
