/*
 * =====================================================================
 * (c) Copyright 2001  BBNT Solutions, LLC
 * =====================================================================
 */

/*
 * <copyright>
 *  Copyright 1997-2000 Defense Advanced Research Projects
 *  Agency (DARPA) and ULTRALOG (a BBN Technologies (BBN) and
 *  Raytheon Systems Company (RSC) Consortium).
 *  This software to be used only in accordance with the
 *  COUGAAR licence agreement.
 * </copyright>
 */
package org.cougaar.lib.mquo;

import org.cougaar.core.qos.metrics.QosComponent;
import com.bbn.quo.rmi.QuoKernel;

/*
 * Enables the QuO GUI for debugging
 * QuO Gui uses raw java windows, not a servlet.
 * For linux you must have X-windows running.
 */

public class QuoGuiComponent extends QosComponent
{
    public void load() {
	super.load();
	QuoKernel kernel = Utils.getKernel();
	try { kernel.newFrame(); }
	catch (java.rmi.RemoteException dont_care) {}
    }
}
