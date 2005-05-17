package org.cougaar.core.qos.frame.visualizer.util;

import org.cougaar.core.qos.frame.visualizer.tree.FrameNode;

import javax.swing.tree.TreeNode;
import java.io.PrintWriter;
import java.util.Enumeration;


/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 17, 2005
 * Time: 11:33:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class HTMLTreeWriter extends FormatWriter {
    public HTMLTreeWriter() {
        super();
    }
    protected void writeHtml(PrintWriter w, TreeNode node, int indentation, int offset) {
        //write(w, indentation, "<li>");
        if (node instanceof FrameNode) {
            if (((FrameNode) node).isRelationNode())
                write(w, indentation, "<i>"+node+"</i>");
            else
                write(w, indentation, "<b color=\"0000FF\">"+node+"</b>");
        } else
            write(w, indentation, node.toString());
        if (node.getChildCount() > 0) {
            TreeNode child;
            for (Enumeration ii=node.children(); ii.hasMoreElements();) {
                child = (TreeNode) ii.nextElement();
                writeHtml(w, child, indentation+offset, offset);
            }
        }
        //write(w, indentation, "</li>");
    }

    public static void write(PrintWriter w, TreeNode root, int indentation, int offset) {
        HTMLTreeWriter tw = new HTMLTreeWriter();
        tw.write(w, 0, "<html><body><pre>");      //bgcolor="#FFFFFF"
        tw.writeHtml(w, root, indentation+offset, offset);
        tw.write(w, 0, "</pre></body></html>");
    }
    public static  void write(TreeNode root, int indentation, int offset) {
        PrintWriter w = new PrintWriter(System.out, true);
        write(w, root, indentation, offset);
    }
}
