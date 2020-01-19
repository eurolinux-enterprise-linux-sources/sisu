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
 * {@link DeferredClass} representing a named class from a {@link ClassSpace}.
 */
final class NamedClass<T>
    extends AbstractDeferredClass<T>
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final ClassSpace space;

    private final String name;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public NamedClass( final ClassSpace space, final String name )
    {
        this.space = space;
        this.name = name;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( "unchecked" )
    public Class<T> load()
    {
        return (Class<T>) space.loadClass( name );
    }

    public String getName()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return ( 17 * 31 + name.hashCode() ) * 31 + space.hashCode();
    }

    @Override
    public boolean equals( final Object rhs )
    {
        if ( this == rhs )
        {
            return true;
        }
        if ( rhs instanceof NamedClass<?> )
        {
            final NamedClass<?> clazz = (NamedClass<?>) rhs;
            return name.equals( clazz.name ) && space.equals( clazz.space );
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "Deferred " + name + " from " + space;
    }
}
