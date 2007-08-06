/*
 * * =====================================================================
 * (c) Copyright 2006 BBN Technologies
 * =====================================================================
 */

package org.cougaar.qos.qrs.gui;

import javax.swing.tree.*;

public class ResourceContextTreeModel extends DefaultTreeModel
{
    public ResourceContextTreeModel() 
    {
	super(new ResourceContextRoot());
    }
    
    public void update() 
    {
	((ResourceContextRoot) getRoot()).updateChildren(this);
    }

}

