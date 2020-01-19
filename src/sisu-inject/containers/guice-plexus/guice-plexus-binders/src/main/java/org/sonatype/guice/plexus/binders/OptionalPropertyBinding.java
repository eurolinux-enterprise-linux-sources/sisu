/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.binders;

import javax.inject.Provider;

import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;

/**
 * Represents a {@link BeanProperty} bound to an optional {@link Provider}.
 */
final class OptionalPropertyBinding<T>
    implements PropertyBinding
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final BeanProperty<T> property;

    private final Provider<T> provider;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    OptionalPropertyBinding( final BeanProperty<T> property, final Provider<T> provider )
    {
        this.property = property;
        this.provider = provider;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    public <B> void injectProperty( final B bean )
    {
        try
        {
            property.set( bean, provider.get() );
        }
        catch ( final RuntimeException e ) // NOPMD
        {
            // binding is optional, ignore failures
        }
    }
}
