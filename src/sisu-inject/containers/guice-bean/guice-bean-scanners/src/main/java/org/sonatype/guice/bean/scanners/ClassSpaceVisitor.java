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

import java.net.URL;

import org.sonatype.guice.bean.reflect.ClassSpace;
import org.sonatype.guice.bean.scanners.asm.ClassVisitor;

/**
 * ASM-style visitor that visits a {@link ClassSpace}. The methods of this interface must be called in the following
 * order: {@code visit ( visitClass )* visitEnd}.
 */
public interface ClassSpaceVisitor
{
    /**
     * Visits the start of the class space.
     * 
     * @param space The class space
     */
    void visit( final ClassSpace space );

    /**
     * Visits a class resource in the class space.
     * 
     * @param url The class resource URL
     * @return Class visitor; {@code null} if this visitor is not interested in visiting the class
     */
    ClassVisitor visitClass( final URL url );

    /**
     * Visits the end of the class space.
     */
    void visitEnd();
}
