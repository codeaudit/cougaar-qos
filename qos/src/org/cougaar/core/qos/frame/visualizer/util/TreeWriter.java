package org.cougaar.core.qos.frame.visualizer.util;

import java.io.PrintWriter;
import java.util.Enumeration;

import javax.swing.tree.TreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: May 17, 2005
 * Time: 11:33:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class TreeWriter extends FormatWriter {
    public TreeWriter() {
        super();
    }
    protected void writeTo(PrintWriter w, TreeNode node, int indentation, int offset) {
        write(w, indentation, node.toString());
        if (node.getChildCount() > 0) {
            TreeNode child;
            for (Enumeration ii=node.children(); ii.hasMoreElements();) {
                child = (TreeNode) ii.nextElement();
                writeTo(w, child, indentation+offset, offset);
            }
        }
    }

    public static  void write(PrintWriter w, TreeNode root, int indent, int offset) {
        TreeWriter tw = new TreeWriter();
        tw.writeTo(w, root, indent, offset);
    }
    public static  void write(TreeNode root, int indent, int offset) {
        PrintWriter w = new PrintWriter(System.out, true);
        write(w, root, indent, offset);
    }
}