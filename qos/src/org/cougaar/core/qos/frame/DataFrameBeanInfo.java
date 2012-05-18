/**
 * Copyright 2006 BBN Technologies Corp.  All Rights Reserved. 
 *
 * $Id: DataFrameBeanInfo.java,v 1.1 2006-06-26 16:50:25 jzinky Exp $
 */
package org.cougaar.core.qos.frame;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * Suppress all bean info
 *
 */
public class DataFrameBeanInfo extends SimpleBeanInfo {
    @Override
   public PropertyDescriptor[] getPropertyDescriptors() {
	return new PropertyDescriptor[] {};
    }
}
