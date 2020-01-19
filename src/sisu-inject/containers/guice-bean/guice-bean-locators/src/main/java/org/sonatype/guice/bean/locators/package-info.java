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
/**
 * Locate qualified bean implementations across multiple injectors.
 *
 * <p>The principal members of this package are:
 * <dl>
 * <dt>{@link org.sonatype.guice.bean.locators.BeanLocator}
 * <dd>Finds and tracks bean implementations annotated with {@link javax.inject.Qualifier} annotations.
 * <dt>{@link org.sonatype.guice.bean.locators.MutableBeanLocator}
 * <dd>Mutable {@link org.sonatype.guice.bean.locators.BeanLocator} that distributes bindings from zero or more {@link org.sonatype.guice.bean.locators.spi.BindingPublisher}s.
 * <dt>{@link org.sonatype.guice.bean.locators.BeanDescription}
 * <dd>Source location mixin used to supply descriptions to the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * <dt>{@link org.sonatype.guice.bean.locators.HiddenBinding}
 * <dd>Source location mixin used to hide bindings from the {@link org.sonatype.guice.bean.locators.BeanLocator}.
 * </dl>
 */
package org.sonatype.guice.bean.locators;

