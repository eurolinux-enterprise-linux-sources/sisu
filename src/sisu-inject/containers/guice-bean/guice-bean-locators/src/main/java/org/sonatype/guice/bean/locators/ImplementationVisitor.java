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

import org.sonatype.guice.bean.reflect.DeferredProvider;

import com.google.inject.Provider;
import com.google.inject.spi.BindingTargetVisitor;
import com.google.inject.spi.ConstructorBinding;
import com.google.inject.spi.DefaultBindingTargetVisitor;
import com.google.inject.spi.ExposedBinding;
import com.google.inject.spi.InstanceBinding;
import com.google.inject.spi.LinkedKeyBinding;
import com.google.inject.spi.ProviderInstanceBinding;
import com.google.inject.spi.UntargettedBinding;

/**
 * {@link BindingTargetVisitor} that attempts to discover implementations behind bindings.
 */
final class ImplementationVisitor
    extends DefaultBindingTargetVisitor<Object, Class<?>>
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    static final ImplementationVisitor THIS = new ImplementationVisitor();

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public Class<?> visit( final UntargettedBinding<?> binding )
    {
        return binding.getKey().getTypeLiteral().getRawType();
    }

    @Override
    public Class<?> visit( final LinkedKeyBinding<?> binding )
    {
        // this assumes only one level of indirection: api-->impl
        return binding.getLinkedKey().getTypeLiteral().getRawType();
    }

    @Override
    public Class<?> visit( final ConstructorBinding<?> binding )
    {
        return binding.getConstructor().getDeclaringType().getRawType();
    }

    @Override
    public Class<?> visit( final InstanceBinding<?> binding )
    {
        return binding.getInstance().getClass();
    }

    @Override
    public Class<?> visit( final ProviderInstanceBinding<?> binding )
    {
        final Provider<?> provider = binding.getProviderInstance();
        if ( provider instanceof DeferredProvider<?> )
        {
            try
            {
                // deferred providers let us peek at the underlying implementation type
                return ( (DeferredProvider<?>) provider ).getImplementationClass().load();
            }
            catch ( final TypeNotPresentException e ) // NOPMD
            {
                // fall-through
            }
        }
        return null;
    }

    @Override
    public Class<?> visit( final ExposedBinding<?> binding )
    {
        return binding.getPrivateElements().getInjector().getBinding( binding.getKey() ).acceptTargetVisitor( this );
    }
}
