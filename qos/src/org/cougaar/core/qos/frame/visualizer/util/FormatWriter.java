package org.cougaar.core.qos.frame.visualizer.util;

import java.io.PrintWriter;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 17, 2005
 * Time: 11:18:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormatWriter {
        public void indent(PrintWriter w, int numSpaces) {
           for (int i=0; i<numSpaces; i++)
               w.print(' ');
        }
        public void write(PrintWriter w, int indentation, String str){
            indent(w, indentation);
            w.println(str);
        }
        public void writeLines(PrintWriter w, int indentation, String lines[]){
            for (int i=0; i < lines.length; i++)
                write(w, indentation, lines[i]);
        }
}
