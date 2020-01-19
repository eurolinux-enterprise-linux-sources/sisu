/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.reflect;

/**
 * Pseudo {@link DeferredClass} backed by an already loaded {@link Class}.
 */
public final class LoadedClass<T>
    extends AbstractDeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Class<T> clazz;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public LoadedClass( final Class<? extends T> clazz )
    {
        this.clazz = (Class<T>) clazz;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public Class<T> load()
    {
        return clazz;
    }

    public String getName()
    {
        return clazz.getName();
    }

    @Override
    public int hashCode()
    {
        return clazz.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof LoadedClass<?> )
        {
            return clazz == ( (LoadedClass<?>) rhs ).clazz;
        }
        return false;
    }

    @Override
    public String toString()
    {
        final String id = "Loaded " + clazz;
        final ClassLoader space = clazz.getClassLoader();
        return null != space ? id + " from " + space : id;
    }
}
