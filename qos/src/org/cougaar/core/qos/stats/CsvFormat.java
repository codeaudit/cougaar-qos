/* =============================================================================
 *
 *                  COPYRIGHT 2007 BBN Technologies Corp.
 *                  10 Moulton St
 *                  Cambridge MA 02138
 *                  (617) 873-8000
 *
 *       This program is the subject of intellectual property rights
 *       licensed from BBN Technologies
 *
 *       This legend must continue to appear in the source code
 *       despite modifications or enhancements by any party.
 *
 *
 * =============================================================================
 *
 * Created : Aug 24, 2007
 * Workfile: CSVLog.java
 * $Revision: 1.2 $
 * $Date: 2008-04-02 14:55:49 $
 * $Author: jzinky $
 *
 * =============================================================================
 */

//Manages a CVS File in the run directory
package org.cougaar.core.qos.stats;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

public class CsvFormat<T> {
    private final Map<Integer, String> fieldLabelMap;
    private final Map<Integer, DecimalFormat> fieldFormatterMap;
    private final Map<Integer, Method> fieldGetters;
    private final PropertyDescriptor[] descriptors;
    private int lastIndex = -1;
    
    public CsvFormat(Class<T> rowClass) {
    	BeanInfo in;
        try {
            in = Introspector.getBeanInfo(rowClass);
        } catch (IntrospectionException e) {
            // Provided class that really is not a bean
            throw new IllegalStateException(e);
        }
    	this.descriptors = in.getPropertyDescriptors();
    	new TreeMap<String, Integer>();
    	this.fieldLabelMap = new HashMap<Integer, String>();
    	this.fieldFormatterMap = new HashMap<Integer, DecimalFormat>();
    	this.fieldGetters = new HashMap<Integer, Method>();
    	
    }
    
    public void defineField(String field, String label, DecimalFormat format) {
    	int index = ++lastIndex;
    	fieldLabelMap.put(index, label != null ? label : field);
    	if (format != null) {
    		fieldFormatterMap.put(index, format);
    	}
    	for (PropertyDescriptor desc : descriptors) {
    		if (desc.getName().equals(field)) {
    			fieldGetters.put(index, desc.getReadMethod());
    			break;
    		}
    	}
    	if (!fieldGetters.containsKey(index)) {
    	    Logger log = Logging.getLogger(getClass().getName());
    		log.error("No field named \"" + field+ "\"");
    	}
    }
    
   public void writeLabels(PrintStream out) {
	   boolean first = true;
	   for (int i=0; i<fieldLabelMap.size(); i++) {
		   if (!first) {
			   out.print(",");
		   }
		   first = false;
		   out.print(fieldLabelMap.get(i));
	   }
	   out.println();
   }
   
   public void writeRow(T row, PrintStream out) 
   throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
       boolean first = true;
       for (int i=0; i<fieldLabelMap.size(); i++) {
           if (!first) {
               out.print(",");
           }
           first = false;
           Method reader = fieldGetters.get(i);
           if (reader != null) {
               Object value = reader.invoke(row);
               DecimalFormat fmt = fieldFormatterMap.get(i);
               if (value != null) {
                   if (fmt != null) {
                       out.print(fmt.format(value));
                   } else {
                       out.print(value);
                   }
               }
           }
       }
       out.println();
   }

}
