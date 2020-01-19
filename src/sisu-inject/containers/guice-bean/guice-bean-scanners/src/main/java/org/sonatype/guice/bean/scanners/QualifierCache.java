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
package org.sonatype.guice.bean.scanners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Qualifier;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.asm.AnnotationVisitor;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;
import org.sonatype.guice.bean.scanners.asm.Type;

/**
 * Caching {@link ClassVisitor} that maintains a map of known {@link Qualifier} annotations.
 */
final class QualifierCache
    extends EmptyClassVisitor
{
    // ----------------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------------

    private static final String QUALIFIER_DESC = Type.getDescriptor( Qualifier.class );

    // ----------------------------------------------------------------------
    // Implementation fields
    // ----------------------------------------------------------------------

    private static final Map<String, Boolean> cachedResults = new ConcurrentHashMap<String, Boolean>( 32, 0.75f, 1 );

    private boolean isQualified;

    // ----------------------------------------------------------------------
    // Public methods
    // ----------------------------------------------------------------------

    @Override
    public AnnotationVisitor visitAnnotation( final String desc, final boolean visible )
    {
        isQualified |= QUALIFIER_DESC.equals( desc );
        return null;
    }

    // ----------------------------------------------------------------------
    // Local methods
    // ----------------------------------------------------------------------

    /**
     * Attempts to load the potential {@link Qualifier} annotation and return its class.
     * 
     * @param space The class space
     * @param desc The annotation descriptor
     * @return {@code true} if the annotation is a qualifier; otherwise {@code false}
     */
    boolean qualify( final ClassSpace space, final String desc )
    {
        final Boolean result = cachedResults.get( desc );
        if ( null == result )
        {
            isQualified = false;

            final String name = desc.substring( 1, desc.length() - 1 );
            ClassSpaceScanner.accept( this, space.getResource( name + ".class" ) );
            cachedResults.put( desc, Boolean.valueOf( isQualified ) );

            return isQualified;
        }
        return result.booleanValue();
    }
}
