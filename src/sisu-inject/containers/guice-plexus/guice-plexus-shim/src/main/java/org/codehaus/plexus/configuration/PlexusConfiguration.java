/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.codehaus.plexus.configuration;

public interface PlexusConfiguration
{
    String getName();

    String getValue();

    String getValue( String defaultValue );

    void setValue( String value );

    String[] getAttributeNames();

    String getAttribute( String attributeName );

    String getAttribute( String attributeName, String defaultValue );

    void setAttribute( String name, String value );

    PlexusConfiguration getChild( String childName );

    PlexusConfiguration getChild( String childName, boolean create );

    PlexusConfiguration[] getChildren();

    PlexusConfiguration[] getChildren( String childName );

    int getChildCount();

    PlexusConfiguration getChild( int index );

    void addChild( PlexusConfiguration child );

    PlexusConfiguration addChild( String name, String value );
}
