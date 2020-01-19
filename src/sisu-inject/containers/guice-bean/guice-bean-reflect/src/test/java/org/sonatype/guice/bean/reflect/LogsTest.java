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

import java.net.URLClassLoader;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.TestCase;

import org.slf4j.LoggerFactory;

public class LogsTest
    extends TestCase
{
    public void testLogging()
    {
        new LoggingExample();
    }

    public void testProductionLogging()
        throws Exception
    {
        try
        {
            ( (ch.qos.logback.classic.Logger) LoggerFactory.getLogger( Logs.class ) ).setLevel( ch.qos.logback.classic.Level.WARN );

            final ClassLoader productionLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.startsWith( "ch" ) || name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            productionLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
        }
    }

    public void testFallBackToJDKLogging()
        throws Exception
    {
        final Logger rootLogger = Logger.getLogger( "" );

        final Handler[] handlers = rootLogger.getHandlers();
        if ( handlers.length > 0 )
        {
            handlers[0].setLevel( Level.FINE );
        }

        final Level level = rootLogger.getLevel();
        try
        {
            rootLogger.setLevel( Level.FINE );

            final ClassLoader noSLF4JLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "slf4j" ) )
                        {
                            throw new ClassNotFoundException( name );
                        }
                        if ( name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            noSLF4JLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            rootLogger.setLevel( level );
        }
    }

    public void testConsoleLogging()
        throws Exception
    {
        System.setProperty( "org.sonatype.inject.debug", "true" );
        try
        {
            final ClassLoader consoleLoader =
                new URLClassLoader( ( (URLClassLoader) getClass().getClassLoader() ).getURLs(), null )
                {
                    @Override
                    protected synchronized Class<?> loadClass( final String name, final boolean resolve )
                        throws ClassNotFoundException
                    {
                        if ( name.contains( "cobertura" ) )
                        {
                            return LogsTest.class.getClassLoader().loadClass( name );
                        }
                        return super.loadClass( name, resolve );
                    }
                };

            consoleLoader.loadClass( LoggingExample.class.getName() ).newInstance();
        }
        finally
        {
            System.clearProperty( "org.sonatype.inject.debug" );
        }
    }
}
