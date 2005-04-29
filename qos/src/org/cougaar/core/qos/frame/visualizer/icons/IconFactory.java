package org.cougaar.core.qos.frame.visualizer.icons;

import javax.swing.*;
import java.util.HashMap;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: mwalczak
 * Date: Apr 28, 2005
 * Time: 1:08:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class IconFactory {
    private static IconFactory instance = null;
    public static IconFactory getInstance() {
        if (instance == null)
            instance = new IconFactory();
        return instance;
    }

    public static Icon getIcon(String iconName) {
        return (Icon) getInstance().iconMap.get(iconName);
    }

    public final static String FRAME_ICON = "frame";
    public final static String FRAME_PROTOYPE_ICON = "frameProto";
    public final static String RELATION_ICON = "relationIcon";
    public final static String CONTAINER_ICON = "container";
    public final static String COMPONENT_ICON = "component";
    public final static String CONTAINER_PROTOTYPE_ICON = "containerPrototype";
    public final static String COMPONENT_PROTOTYPE_ICON = "componentPrototype";


    private HashMap iconMap;

    private IconFactory() {
        iconMap = new HashMap();
        loadIcons();
    }

    private void load(ClassLoader cl, String key, String filename) {
        URL icnURL = cl.getResource(filename);
        ImageIcon icon = new ImageIcon(icnURL);
        iconMap.put(key, icon);
    }

    private void loadIcons() {
        ClassLoader cl = IconFactory.class.getClassLoader();
        String prefix = "org/cougaar/core/qos/frame/visualizer/icons/";
        load(cl, FRAME_ICON, prefix+"frame.gif");
        load(cl, FRAME_PROTOYPE_ICON, prefix+"frameProto.gif");
        load(cl, RELATION_ICON, prefix+"relation.gif");
        load(cl, CONTAINER_ICON, prefix+"container.gif");
        load(cl, COMPONENT_ICON, prefix+"comp.gif");
        load(cl, CONTAINER_PROTOTYPE_ICON, prefix+"proto.gif");
        load(cl, COMPONENT_PROTOTYPE_ICON, prefix+"proto.gif");
    }
}
