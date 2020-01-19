/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.codehaus.plexus.logging;

public interface LoggerManager
{
    String ROLE = LoggerManager.class.getName();

    Logger getLoggerForComponent( String role );

    Logger getLoggerForComponent( String role, String hint );

    void returnComponentLogger( String role );

    void returnComponentLogger( String role, String hint );

    int getThreshold();

    void setThreshold( int threshold );

    void setThresholds( int threshold );

    int getActiveLoggerCount();
}
