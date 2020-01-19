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
package org.sonatype.guice.bean.locators;

import org.sonatype.guice.bean.locators.spi.BindingPublisher;
import org.sonatype.guice.bean.reflect.Logs;

import com.google.inject.Binding;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvidedBy;
import com.google.inject.TypeLiteral;

/**
 * Source of "implicit" bindings; includes @{@link ImplementedBy}, @{@link ProvidedBy}, and concrete types.
 */
final class ImplicitBindings
{
    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Iterable<BindingPublisher> publishers;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    ImplicitBindings( final Iterable<BindingPublisher> publishers )
    {
        this.publishers = publishers;
    }

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @SuppressWarnings( { "unchecked", "rawtypes" } )
    public <T> Binding<T> get( final TypeLiteral<T> type )
    {
        final Key implicitKey = Key.get( type.getRawType(), Implicit.class );
        for ( final BindingPublisher p : publishers )
        {
            if ( p instanceof InjectorPublisher )
            {
                // first round: check for any re-written implicit bindings
                final Injector i = ( (InjectorPublisher) p ).getInjector();
                final Binding binding = i.getBindings().get( implicitKey );
                if ( null != binding )
                {
                    Logs.debug( "Using implicit binding: {} from: <>", binding, i );
                    return binding;
                }
            }
        }

        final Key justInTimeKey = Key.get( type );
        for ( final BindingPublisher p : publishers )
        {
            if ( p instanceof InjectorPublisher )
            {
                // second round: fall back to just-in-time binding lookup
                final Injector i = ( (InjectorPublisher) p ).getInjector();
                try
                {
                    final Binding binding = i.getBinding( justInTimeKey );
                    if ( InjectorPublisher.isVisible( binding ) )
                    {
                        Logs.debug( "Using just-in-time binding: {} from: <>", binding, i );
                        return binding;
                    }
                }
                catch ( final RuntimeException e ) // NOPMD
                {
                    // no luck, move onto next injector
                }
                catch ( final LinkageError e ) // NOPMD
                {
                    // no luck, move onto next injector
                }
            }
        }
        return null;
    }
}
