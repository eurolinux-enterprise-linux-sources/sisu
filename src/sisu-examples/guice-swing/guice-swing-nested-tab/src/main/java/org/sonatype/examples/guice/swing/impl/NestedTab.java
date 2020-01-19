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
package org.sonatype.examples.guice.swing.impl;

import java.awt.Graphics;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Named;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

@Named( "Nested" )
final class NestedTab
    extends JPanel
{
    static int instanceCount;

    final JTabbedPane pane = new JTabbedPane();

    @Inject
    Map<String, JPanel> tabs;

    NestedTab()
    {
        setName( "NestedTab instance #" + ++instanceCount );
        add( pane );
    }

    @Override
    public void paint( final Graphics g )
    {
        if ( pane.getTabCount() == 0 )
        {
            pane.setPreferredSize( getSize() );
            for ( final Entry<String, JPanel> e : tabs.entrySet() )
            {
                pane.addTab( e.getKey(), e.getValue() );
            }
        }
        super.paint( g );
    }
}
