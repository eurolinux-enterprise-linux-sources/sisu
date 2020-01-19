/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.scanners;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.reflect.DeferredClass;
import org.sonatype.guice.bean.reflect.Logs;
import org.sonatype.guice.bean.reflect.URLClassSpace;
import org.sonatype.guice.plexus.annotations.ComponentImpl;
import org.sonatype.guice.plexus.config.Hints;
import org.sonatype.guice.plexus.config.Roles;
import org.sonatype.guice.plexus.config.Strategies;

/**
 * Enhanced Plexus component map with additional book-keeping.
 */
final class PlexusTypeRegistry
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final Component LOAD_ON_START_PLACEHOLDER = new ComponentImpl( Object.class, "",
                                                                                  Strategies.LOAD_ON_START, "" );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private final Map<String, Component> components = new HashMap<String, Component>();

    private final Map<String, DeferredClass<?>> implementations = new HashMap<String, DeferredClass<?>>();

    private final Set<String> deferredNames = new HashSet<String>();

    final ClassSpace space;

    private ClassSpace cloningClassSpace;

    private int cloneCounter;

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    PlexusTypeRegistry( final ClassSpace space )
    {
        this.space = space;
    }

    // ----------------------------------------------------------------------
    // Locally-shared methods
    // ----------------------------------------------------------------------

    /**
     * @return Current class space
     */
    ClassSpace getSpace()
    {
        return space;
    }

    /**
     * Records that the given Plexus component should be loaded when the container starts.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     */
    void loadOnStart( final String role, final String hint )
    {
        final String key = Roles.canonicalRoleHint( role, hint );
        final Component c = components.get( key );
        if ( null == c )
        {
            components.put( key, LOAD_ON_START_PLACEHOLDER );
        }
        else if ( !Strategies.LOAD_ON_START.equals( c.instantiationStrategy() ) )
        {
            components.put( key, new ComponentImpl( c.role(), c.hint(), Strategies.LOAD_ON_START, c.description() ) );
        }
    }

    /**
     * Registers the given component, automatically disambiguating between implementations bound multiple times.
     * 
     * @param role The Plexus role
     * @param hint The Plexus hint
     * @param instantiationStrategy The instantiation strategy
     * @param description The component description
     * @param implementation The implementation
     * @return The implementation the component was successfully registered with; otherwise {@code null}
     */
    String addComponent( final String role, final String hint, final String instantiationStrategy,
                         final String description, final String implementation )
    {
        final Class<?> roleType = loadRole( role, implementation );
        if ( null == roleType )
        {
            return null;
        }

        final String canonicalHint = Hints.canonicalHint( hint );
        final String key = Roles.canonicalRoleHint( role, canonicalHint );

        /*
         * COMPONENT...
         */
        final Component oldComponent = components.get( key );
        if ( null == oldComponent )
        {
            components.put( key, new ComponentImpl( roleType, canonicalHint, instantiationStrategy, description ) );
        }
        else if ( LOAD_ON_START_PLACEHOLDER == oldComponent )
        {
            components.put( key, new ComponentImpl( roleType, canonicalHint, Strategies.LOAD_ON_START, description ) );
        }

        /*
         * ...IMPLEMENTATION
         */
        DeferredClass<?> implementationType = implementations.get( key );
        if ( null == implementationType )
        {
            if ( deferredNames.add( implementation ) )
            {
                implementationType = space.deferLoadClass( implementation );
            }
            else
            {
                // type already used for another role, so we must clone it
                implementationType = cloneImplementation( implementation );
            }
            implementations.put( key, implementationType );
            return implementationType.getName();
        }
        final String oldImplementation = implementationType.getName();
        if ( implementation.equals( CloningClassLoader.getRealName( oldImplementation ) ) )
        {
            return oldImplementation; // merge configuration
        }

        Logs.debug( "Duplicate implementations for Plexus role: {}", key, null );
        Logs.debug( "Using: {} ignoring: {}", oldImplementation, implementation );

        return null;
    }

    /**
     * @return Plexus component map
     */
    Map<Component, DeferredClass<?>> getComponents()
    {
        final Map<Component, DeferredClass<?>> map = new HashMap<Component, DeferredClass<?>>();
        for ( final Entry<String, DeferredClass<?>> i : implementations.entrySet() )
        {
            map.put( components.get( i.getKey() ), i.getValue() );
        }
        return map;
    }

    // ----------------------------------------------------------------------
    // Implementation methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the given Plexus role, checks constructors for concrete types.
     * 
     * @param role The Plexus role
     * @param implementation The implementation
     * @return Loaded Plexus role
     */
    private Class<?> loadRole( final String role, final String implementation )
    {
        try
        {
            return space.loadClass( role );
        }
        catch ( final TypeNotPresentException e )
        {
            Logs.debug( "Ignoring Plexus role: {}", role, e );
        }
        return null;
    }

    private DeferredClass<?> cloneImplementation( final String implementation )
    {
        if ( null == cloningClassSpace )
        {
            cloningClassSpace = new URLClassSpace( AccessController.doPrivileged( new PrivilegedAction<ClassLoader>()
            {
                public ClassLoader run()
                {
                    return new CloningClassLoader( space );
                }
            } ), null );
        }
        return cloningClassSpace.deferLoadClass( CloningClassLoader.proxyName( implementation, ++cloneCounter ) );
    }
}
