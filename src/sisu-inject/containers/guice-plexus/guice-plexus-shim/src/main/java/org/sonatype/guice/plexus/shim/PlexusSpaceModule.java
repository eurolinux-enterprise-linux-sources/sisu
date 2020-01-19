/*******************************************************************************
 * Copyright (c) 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.shim;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextMapAdapter;
import org.codehaus.plexus.context.DefaultContext;
import org.codehaus.plexus.logging.LoggerManager;
import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.plexus.binders.PlexusBeanManager;
import org.sonatype.guice.plexus.binders.PlexusBindingModule;
import org.sonatype.guice.plexus.binders.PlexusXmlBeanModule;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.config.PlexusBeanModule;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.lifecycles.PlexusLifecycleManager;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;
import org.sonatype.inject.Parameters;

import com.google.inject.Binder;
import com.google.inject.Module;

public final class PlexusSpaceModule
    implements Module
{
    private final ClassSpace space;

    public PlexusSpaceModule( final ClassSpace space )
    {
        this.space = space;
    }

    public void configure( final Binder binder )
    {
        final Context context = new ParameterizedContext();
        binder.bind( Context.class ).toInstance( context );

        final Provider<?> slf4jLoggerFactoryProvider = space.deferLoadClass( "org.slf4j.ILoggerFactory" ).asProvider();
        binder.requestInjection( slf4jLoggerFactoryProvider );

        binder.bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );
        binder.bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
        binder.bind( PlexusContainer.class ).to( PseudoPlexusContainer.class );

        final PlexusBeanManager manager = new PlexusLifecycleManager( binder.getProvider( Context.class ), //
                                                                      binder.getProvider( LoggerManager.class ), //
                                                                      slf4jLoggerFactoryProvider ); // SLF4J (optional)

        binder.bind( PlexusBeanManager.class ).toInstance( manager );

        final PlexusBeanModule xmlModule = new PlexusXmlBeanModule( space, new ContextMapAdapter( context ) );
        binder.install( new PlexusBindingModule( manager, xmlModule ) );
    }

    static final class ParameterizedContext
        extends DefaultContext
    {
        @Inject
        protected void setParameters( @Parameters final Map<?, ?> parameters )
        {
            contextData.putAll( parameters );
        }
    }
}
