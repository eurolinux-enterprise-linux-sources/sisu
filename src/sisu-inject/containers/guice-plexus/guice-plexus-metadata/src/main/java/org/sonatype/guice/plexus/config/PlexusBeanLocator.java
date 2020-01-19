/*******************************************************************************
 * Copyright (c) 2010-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.sonatype.guice.plexus.config;

import com.google.inject.TypeLiteral;

/**
 * Service that locates beans of various types, using optional Plexus hints as a guide.
 */
public interface PlexusBeanLocator
{
    /**
     * Locates beans of the given type, optionally filtered using the given named hints.
     * 
     * @param role The expected bean type
     * @param hints The optional (canonical) hints
     * @return Sequence of Plexus bean mappings; ordered according to the given hints
     */
    <T> Iterable<PlexusBean<T>> locate( TypeLiteral<T> role, String... hints );
}
