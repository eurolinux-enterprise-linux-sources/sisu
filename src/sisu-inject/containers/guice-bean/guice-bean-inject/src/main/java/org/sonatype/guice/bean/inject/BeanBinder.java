/*******************************************************************************
 * Copyright (c) 2009-2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 * The Eclipse Public License is available at
 *   http://www.eclipse.org/legal/epl-v10.html
 * The Apache License v2.0 is available at
 *   http://www.apache.org/licenses/LICENSE-2.0.html
 * You may elect to redistribute this code under either of these licenses.
 *******************************************************************************/
package org.sonatype.guice.bean.inject;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;

/**
 * Provides custom {@link PropertyBinder}s for beans that contain one or more properties.
 */
public interface BeanBinder
{
    /**
     * Returns the appropriate {@link PropertyBinder} for the given bean type.
     * 
     * @param type The bean type
     * @param encounter The Guice type encounter
     * @return Property binder for the given type; {@code null} if no binder is applicable
     */
    <B> PropertyBinder bindBean( TypeLiteral<B> type, TypeEncounter<B> encounter );
}
