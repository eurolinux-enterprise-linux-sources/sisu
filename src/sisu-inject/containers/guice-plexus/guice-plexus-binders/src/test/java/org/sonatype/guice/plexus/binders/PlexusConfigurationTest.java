/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.binders;

import javax.inject.Inject;

import junit.framework.TestCase;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.sonatype.guice.bean.inject.PropertyBinding;
import org.sonatype.guice.bean.reflect.BeanProperty;
import org.sonatype.guice.plexus.config.PlexusBeanConverter;
import org.sonatype.guice.plexus.config.PlexusBeanLocator;
import org.sonatype.guice.plexus.converters.PlexusDateTypeConverter;
import org.sonatype.guice.plexus.converters.PlexusXmlBeanConverter;
import org.sonatype.guice.plexus.locators.DefaultPlexusBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;

public class PlexusConfigurationTest
    extends TestCase
{
    @Inject
    ConfiguredComponent component;

    @Inject
    Injector injector;

    static class ComponentManager
        implements PlexusBeanManager
    {
        static int SEEN;

        public boolean manage( final Class<?> clazz )
        {
            return ConfiguredComponent.class.isAssignableFrom( clazz );
        }

        public PropertyBinding manage( final BeanProperty<?> property )
        {
            return null;
        }

        public boolean manage( final Object bean )
        {
            SEEN++;
            return true;
        }

        public boolean unmanage( final Object bean )
        {
            return false;
        }

        public boolean unmanage()
        {
            return false;
        }
    }

    @Override
    protected void setUp()
    {
        Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                install( new PlexusDateTypeConverter() );

                bind( PlexusBeanLocator.class ).to( DefaultPlexusBeanLocator.class );
                bind( PlexusBeanConverter.class ).to( PlexusXmlBeanConverter.class );

                install( new PlexusBindingModule( new ComponentManager(), new PlexusAnnotatedBeanModule( null, null ) ) );

                requestInjection( PlexusConfigurationTest.this );
            }
        } );
    }

    @Component( role = Object.class )
    static class ConfiguredComponent
    {
        @Configuration( "1" )
        String a;

        @Configuration( "2" )
        Integer b;

        @Configuration( "3" )
        int c;

        @Configuration( "4" )
        Double d;

        @Configuration( "5" )
        double e;
    }

    @Component( role = Object.class )
    static class MisconfiguredComponent
    {
        @Configuration( "misconfigured" )
        SomeBean bean;
    }

    public static class SomeBean
    {
        public SomeBean( final String data )
        {
            if ( "misconfigured".equals( data ) )
            {
                throw new NoClassDefFoundError();
            }
        }
    }

    public void testConfiguration()
    {
        assertEquals( "1", component.a );
        assertEquals( Integer.valueOf( 2 ), component.b );
        assertEquals( 3, component.c );
        assertEquals( Double.valueOf( 4.0 ), component.d );
        assertEquals( 5.0, component.e, 0 );

        assertEquals( 1, ComponentManager.SEEN );

        final ConfiguredComponent jitComponent = injector.getInstance( ConfiguredComponent.class );

        assertEquals( "1", jitComponent.a );
        assertEquals( Integer.valueOf( 2 ), jitComponent.b );
        assertEquals( 3, jitComponent.c );
        assertEquals( Double.valueOf( 4.0 ), jitComponent.d );
        assertEquals( 5.0, jitComponent.e, 0 );

        assertEquals( 2, ComponentManager.SEEN );

        try
        {
            injector.getInstance( MisconfiguredComponent.class );
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        assertEquals( 2, ComponentManager.SEEN );
    }
}
