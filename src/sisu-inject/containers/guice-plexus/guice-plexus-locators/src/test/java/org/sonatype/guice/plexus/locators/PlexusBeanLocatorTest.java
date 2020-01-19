/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.locators;

import java.util.Iterator;
import java.util.Map.Entry;

import junit.framework.TestCase;

import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.ClassWorldException;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.classworlds.realm.NoSuchRealmException;
import org.sonatype.guice.bean.locators.DefaultBeanLocator;
import org.sonatype.guice.bean.locators.HiddenBinding;
import org.sonatype.guice.bean.locators.MutableBeanLocator;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;

@SuppressWarnings( "deprecation" )
public class PlexusBeanLocatorTest
    extends TestCase
{
    static final HiddenBinding TEST_HIDDEN_SOURCE = new HiddenBinding()
    {
    };

    @ImplementedBy( BeanImpl.class )
    interface Bean
    {
    }

    static class BeanImpl
        implements Bean
    {
    }

    Injector parent;

    Injector child1;

    Injector child2;

    Injector child3;

    @Override
    public void setUp()
        throws Exception
    {
        parent = Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "-" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "Z" ) ).to( BeanImpl.class );
            }
        } );

        child1 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).to( BeanImpl.class );
                final Binder hiddenBinder = binder().withSource( TEST_HIDDEN_SOURCE );
                hiddenBinder.bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N1" ) ).to( BeanImpl.class );
            }
        } );

        child2 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
            }
        } );

        child3 = parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).to( BeanImpl.class );
                bind( Bean.class ).annotatedWith( Names.named( "N3" ) ).to( BeanImpl.class );
            }
        } );
    }

    public void testInjectorOrdering()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Iterable<? extends Entry<String, Bean>> roles =
            new DefaultPlexusBeanLocator( locator ).locate( TypeLiteral.get( Bean.class ) );

        locator.add( parent, 0 );
        locator.add( child1, 1 );
        locator.add( child2, 2 );
        locator.add( child3, 3 );
        locator.remove( child1 );
        locator.add( child1, 4 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child2 );
        locator.remove( child2 );

        i = roles.iterator();
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child3 );
        locator.add( child3, 5 );
        locator.add( child3, 5 );

        i = roles.iterator();
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( parent );

        i = roles.iterator();
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertFalse( i.hasNext() );

        locator.remove( child1 );
        locator.remove( child3 );

        i = roles.iterator();
        assertFalse( i.hasNext() );
    }

    public void testExistingInjectors()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        locator.add( parent, 0 );
        locator.add( child1, 1 );
        final Iterable<? extends Entry<String, Bean>> roles =
            new DefaultPlexusBeanLocator( locator ).locate( TypeLiteral.get( Bean.class ) );
        locator.add( child2, 2 );
        locator.add( child3, 3 );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );
    }

    public void testRoleHintLookup()
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();

        final Iterable<? extends Entry<String, Bean>> roles =
            new DefaultPlexusBeanLocator( locator ).locate( TypeLiteral.get( Bean.class ), "A", "M1", "N3", "-", "!",
                                                            "-", "M3", "N1", "Z" );

        Iterator<? extends Entry<String, Bean>> i;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        assertEquals( "M1", i.next().getKey() );
        assertEquals( "N3", i.next().getKey() );
        assertEquals( "-", i.next().getKey() );
        final Entry<String, Bean> pling = i.next();
        assertEquals( "!", pling.getKey() );
        assertEquals( "-", i.next().getKey() );
        assertEquals( "M3", i.next().getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertEquals( "!=<missing>", pling.toString() );

        try
        {
            pling.getValue();
            fail( "Expected ProvisionException" );
        }
        catch ( final ProvisionException e )
        {
        }

        try
        {
            pling.setValue( null );
            fail( "Expected UnsupportedOperationException" );
        }
        catch ( final UnsupportedOperationException e )
        {
        }

        locator.add( parent, 0 );
        locator.add( child1, 1 );
        locator.add( child2, 2 );
        locator.add( child3, 3 );

        locator.add( parent.createChildInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "M1" ) ).toProvider( new Provider<Bean>()
                {
                    public Bean get()
                    {
                        return null;
                    }
                } );
                bind( Bean.class ).annotatedWith( Names.named( "M3" ) ).toProvider( new Provider<Bean>()
                {
                    public Bean get()
                    {
                        return null;
                    }
                } );
            }
        } ), 4 );

        Entry<String, Bean> m1, dash1, dash2, m3;

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        m1 = i.next();
        assertEquals( "M1", m1.getKey() );
        assertEquals( "N3", i.next().getKey() );
        dash1 = i.next();
        assertEquals( "-", dash1.getKey() );
        assertEquals( "!", i.next().getKey() );
        dash2 = i.next();
        assertEquals( "-", dash2.getKey() );
        m3 = i.next();
        assertEquals( "M3", m3.getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertNull( m1.getValue() );
        assertSame( dash1.getValue(), dash2.getValue() );
        assertNull( m3.getValue() );

        locator.remove( child1 );
        locator.add( child1, 5 );

        i = roles.iterator();
        assertEquals( "A", i.next().getKey() );
        m1 = i.next();
        assertEquals( "M1", m1.getKey() );
        assertEquals( "N3", i.next().getKey() );
        dash1 = i.next();
        assertEquals( "-", dash1.getKey() );
        assertEquals( "!", i.next().getKey() );
        dash2 = i.next();
        assertEquals( "-", dash2.getKey() );
        m3 = i.next();
        assertEquals( "M3", m3.getKey() );
        assertEquals( "N1", i.next().getKey() );
        assertEquals( "Z", i.next().getKey() );
        assertFalse( i.hasNext() );

        assertEquals( "M1=" + m1.getValue(), m1.toString() );
        assertEquals( BeanImpl.class, m1.getValue().getClass() );
        assertSame( m1.getValue(), m1.getValue() );
        assertSame( dash1.getValue(), dash2.getValue() );
        assertNull( m3.getValue() );
    }

    public void testInjectorVisibility()
        throws NoSuchRealmException
    {
        final MutableBeanLocator locator = new DefaultBeanLocator();
        final ClassWorld world = new ClassWorld();

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.newRealm( "A" ) ).bind( Bean.class ).annotatedWith( Names.named( "A" ) ).to( BeanImpl.class );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 1 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.newRealm( "B" ) ).bind( Bean.class ).annotatedWith( Names.named( "B" ) ).to( BeanImpl.class );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 2 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.newRealm( "C" ) ).bind( Bean.class ).annotatedWith( Names.named( "C" ) ).to( BeanImpl.class );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 3 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.getRealm( "B" ).createChildRealm( "B1" ) ).bind( Bean.class ).annotatedWith( Names.named( "B1" ) ).to( BeanImpl.class );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 4 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.getRealm( "B" ).createChildRealm( "B2" ) ).bind( Bean.class ).annotatedWith( Names.named( "B2" ) ).to( BeanImpl.class );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 5 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.getRealm( "B" ).createChildRealm( "B3" ) ).bind( Bean.class ).annotatedWith( Names.named( "B3" ) ).to( BeanImpl.class );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 6 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.getRealm( "B2" ).createChildRealm( "B2B" ) ).bind( Bean.class ).annotatedWith( Names.named( "B2B" ) ).to( BeanImpl.class );
                }
                catch ( final ClassWorldException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 7 );

        locator.add( Guice.createInjector( new Module()
        {
            public void configure( final Binder binder )
            {
                try
                {
                    binder.withSource( world.newRealm( "?" ) ).bind( Bean.class ).annotatedWith( Names.named( "?" ) ).to( BeanImpl.class );
                }
                catch ( final DuplicateRealmException e )
                {
                    throw new RuntimeException( e );
                }
            }
        } ), 8 );

        world.getRealm( "B" ).importFrom( "A", "A" );
        world.getRealm( "B" ).importFrom( "C", "C" );

        world.getRealm( "B2" ).importFrom( "B1", "B1" );
        world.getRealm( "B2" ).importFrom( "B3", "B3" );

        locator.add( Guice.createInjector( new AbstractModule()
        {
            @Override
            protected void configure()
            {
                bind( Bean.class ).annotatedWith( Names.named( "!" ) ).to( BeanImpl.class );
            }
        } ), 9 );

        final Iterable<? extends Entry<String, Bean>> beans =
            new DefaultPlexusBeanLocator( locator ).locate( TypeLiteral.get( Bean.class ) );

        Iterator<? extends Entry<String, Bean>> i;

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "A" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B2" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B2B" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "B2B", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "B3" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( world.getClassRealm( "C" ) );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertFalse( i.hasNext() );

        Thread.currentThread().setContextClassLoader( null );

        i = beans.iterator();
        assertTrue( i.hasNext() );
        assertEquals( "!", i.next().getKey() );
        assertEquals( "?", i.next().getKey() );
        assertEquals( "B2B", i.next().getKey() );
        assertEquals( "B3", i.next().getKey() );
        assertEquals( "B2", i.next().getKey() );
        assertEquals( "B1", i.next().getKey() );
        assertEquals( "C", i.next().getKey() );
        assertEquals( "B", i.next().getKey() );
        assertEquals( "A", i.next().getKey() );
        assertFalse( i.hasNext() );
    }
}
