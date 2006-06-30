/*
 * <copyright>
 *  
 *  Copyright 2000-2004 BBNT Solutions, LLC
 *  under sponsorship of the Defense Advanced Research Projects
 *  Agency (DARPA).
 * 
 *  You can redistribute this software and/or modify it under the
 *  terms of the Cougaar Open Source License as published on the
 *  Cougaar Open Source Website (www.cougaar.org).
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 *  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 *  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * </copyright>
 */

package org.cougaar.core.qos.frame;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cougaar.core.service.LoggingService;
import org.cougaar.core.servlet.ComponentServlet;
import org.cougaar.core.util.UID;
import org.cougaar.util.Sortings;
import org.xml.sax.Attributes;

/**
 * This servlet displays {@link FrameSet}s and allows the user
 * to modify slot values.
 * <p> 
 * To load this servlet, add the following to any agent's XML
 * configuration:<pre> 
 *    &lt;component
 *      class="org.cougaar.core.qos.frame.FrameViewerServlet"/&gt;
 * </pre>
 */
public class FrameViewerServlet extends ComponentServlet {

  private LoggingService log;
  private FrameSetService fss;

  protected String getPath() {
    String s = super.getPath();
    return (s == null ? "/frames" : s);
  }

  public void setLoggingService(LoggingService log) {
    this.log = log;
  }

  public void load() {
    super.load();
    fss = (FrameSetService)
      getServiceBroker().getService(
          this, FrameSetService.class, null);
    if (fss == null && log.isWarnEnabled()) {
      log.warn("Unable to obtain FrameSetService");
    }
  }

    static class PrototypeComparator 
	implements java.util.Comparator
    {
	public int compare(Object x, Object y)
	{
	    String x_name = ((PrototypeFrame) x).getName();
	    String y_name = ((PrototypeFrame) y).getName();
	    return x_name.compareTo(y_name);
	}

	public boolean equals(Object thing)
	{
	    return thing == this;
	}
    }

    private static final java.util.Comparator cmp = new PrototypeComparator();


  public void doGet(
      HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    (new Worker(request, response)).execute();
  }

  private class Worker {
    // from the "doGet(..)":
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private PrintWriter out;

    private static final int NUM_SEARCH_SLOTS = 3;

    // from URL-params:
    private static final String PAGE_PARAM = "page";
    private static final String SEARCH_PAGE = "search";
    private static final String RESULT_PAGE = "result";
    private String page;

    private static final String NAME_PARAM = "name";
    private String name;

    private static final String KIND_PARAM = "kind";
    private String kind;

    private static final String SLOT_PARAM = "slot";
    private final String[] slots = new String[NUM_SEARCH_SLOTS];
    private static final String VALUE_PARAM = "value";
    private final String[] values = new String[NUM_SEARCH_SLOTS];

    private static final String UID_PARAM = "uid";
    private UID modifyUID;

    private static final String NEW_SLOT_PARAM = "newSlot";
    private String newSlot;
    private static final String NEW_VALUE_PARAM = "newValue";
    private String newValue;


    public Worker(
        HttpServletRequest request,
        HttpServletResponse response) {
      this.request = request;
      this.response = response;
    }

    public void execute() throws IOException {
      parseParams();
      writeResponse();
    }

    private void parseParams() throws IOException {
      page = getParameter(PAGE_PARAM);
      if (page == null) {
        return;
      }
      name = getParameter(NAME_PARAM);
      kind = getParameter(KIND_PARAM);
      for (int i = 0; i < NUM_SEARCH_SLOTS; i++) {
        String s = getParameter(SLOT_PARAM+i);
        if (s == null) continue;
        slots[i] = s;
        s = request.getParameter(VALUE_PARAM+i);
        if (s != null) s = s.trim();
        values[i] = s;
      }
      newSlot = getParameter(NEW_SLOT_PARAM);
      newValue = getParameter(NEW_VALUE_PARAM);

      String suid = getParameter(UID_PARAM);
      if (suid != null) {
        modifyUID = decodeUID(suid);
      }
    }

    private void writeResponse() throws IOException {
      response.setContentType("text/html");
      out = response.getWriter();

      if (SEARCH_PAGE.equals(page)) {
        writeSearchPage();
      } else if (RESULT_PAGE.equals(page)) {
        writeResultPage();
      } else {
        writeMainPage();
      }
      out.flush();
    }

    private void writeMainPage() {
      out.print(
          "<html>\n"+
          "<head>\n"+
          "<title>"+
          "FrameSet Viewer"+
          "</title>\n"+
          "</head>\n"+
          "<frameset rows=\"35%,65%\">\n"+
          "<frame"+
          " src=\""+
          request.getRequestURI()+
          "?"+PAGE_PARAM+"="+SEARCH_PAGE+
          "\" name=\""+SEARCH_PAGE+"\">\n"+
          "<frame src=\""+
          request.getRequestURI()+
          "?"+PAGE_PARAM+"="+RESULT_PAGE+
          "\" name=\""+RESULT_PAGE+"\">\n"+
          "</frameset>\n");
    }

    private void writeSearchPage() {
      Set names = fss.getNames();
      out.print(
          "<html><head>\n"+
          "<script language=\"javascript\">\n"+
          "<!--\n"+
          "var names = new Array();\n"+
          "var kinds = new Array();\n");
      if (names != null && names.size() > 0) {
        Set allKinds = new HashSet();
        int i = 0;
        for (Iterator itr = Sortings.sort(names).iterator();
            itr.hasNext();
            i++) {
          String ni = (String) itr.next();
          FrameSet fs = fss.findFrameSet(ni, null);
          if (fs == null) continue;
          out.print(
              "names["+i+"] = '"+ni+"';\n"+
              "kinds["+i+"] = [ '*'" +
	      ", '--Prototypes--'");
	  
          Collection kinds = fs.getPrototypes();
          if (kinds != null) {
            allKinds.addAll(kinds);
            for (Iterator i2 = Sortings.sort(kinds, cmp).iterator();
                i2.hasNext();
                ) {
		out.print(", '"+((PrototypeFrame) i2.next()).getName()+"'");
            }
          }
          out.print(" ];\n");
        }
        out.print(
            "names["+i+"] = '*';\n"+
            "kinds["+i+"] = [ '*'");
        for (Iterator i2 = Sortings.sort(allKinds, cmp).iterator();
            i2.hasNext();
            ) {
	    out.print(", '"+((PrototypeFrame) i2.next()).getName()+"'");
        }
        out.print(" ];\n");
      }
      out.print(
          "\n"+
          "function init() {\n"+
          "  var box = document.myform."+NAME_PARAM+";\n"+
          "  for (var i = 0; i < names.length; i++) {\n"+
          "    box.options[i] = new Option(names[i], names[i]);\n"+
          "  }\n"+
          "  box.options[0].selected = true;\n"+
          "  setKinds();\n"+
          "}\n"+
          "\n"+
          "function setKinds() {\n"+
          "  var nameIdx = document.myform."+NAME_PARAM+
          ".selectedIndex;\n"+
          "  var vals = kinds[nameIdx]\n"+
          "  var box = document.myform."+KIND_PARAM+";\n"+
          "  box.options.length = 0;\n"+
          "  for (var i = 0; i < vals.length; i++) {\n"+
          "    box.options[i] = new Option(vals[i], vals[i]);\n"+
          "  }\n"+
          "  box.options[0].selected = true;\n"+
          "}\n"+
          "-->\n"+
          "</script>\n"+
          "</head>\n");
      out.print(
          "<body onload=\"init()\">\n"+
          "<form name=\"myform\" method=\"GET\" action=\""+
          request.getRequestURI()+
          "\" target=\""+RESULT_PAGE+"\">\n"+
          "<b>Find FrameSet:</b><br>\n"+
          "<input type=hidden name=\""+PAGE_PARAM+
          "\" value=\""+RESULT_PAGE+"\"/>\n"+
          "<table border=\"0\">\n");
      // drop-down names
      out.print(
          "<tr><td>FrameSet Name:&nbsp;</td><td>"+
          "<select name=\""+NAME_PARAM+
          "\" onchange=\"setKinds();\"/>\n"+
          "</td></tr>\n");
      // drop-down kinds
      out.print(
          "<tr><td>Prototype Kind:&nbsp;</td><td>"+
          "<select name=\""+KIND_PARAM+"\"/>\n");
      out.print("<tr><td colspan=3>Properties:</td></tr>\n");
      for (int i = 0; i < NUM_SEARCH_SLOTS; i++) {
        out.print(
            "<tr><td>&nbsp;&nbsp;"+i+":&nbsp;</td>"+
            "<td><input name=\""+
            SLOT_PARAM+i+"\" type=\"text\" size=\"20\" value=\""+
            (slots[i] == null ? "" : slots[i])+
            "\"></td>"+
            "<td><input name=\""+
            VALUE_PARAM+i+"\" type=\"text\" size=\"20\" value=\""+
            (values[i] == null ? "" : values[i])+
            "\"></td>"+
            "</tr>\n");
      }
      out.print(
          "<tr><td>"+
          "<input type=\"submit\" value=\"Search\"/>"+
          "</td></tr>\n"+
          "</table>\n"+
          "</form>\n"+
          "</body></html>");
    }
    
    private void writeResultPage() {
      out.print(
          "<html><body>\n");
      if (name == null || kind == null) {
        out.print(
            "Specify a FrameSet Name and Frame Kind\n"+
            "</body></html>\n");
        return;
      }

      if (name.equals("*")) {
        Set names = fss.getNames();
        if (names != null && names.size() > 0) {
          int i = 0;
          for (Iterator itr = Sortings.sort(names).iterator();
              itr.hasNext();
              i++) {
            String ni = (String) itr.next();
            out.print(
                "<table border=1>\n"+
                "<tr bgcolor=\"DDDDDD\"><th>"+
                "FrameSet "+i+": "+ni+"</th></tr>"+
                "<tr><td>\n");
            writeFrameSet(ni);
            out.print(
                "</td></tr>\n"+
                "</table>\n");
          }
        }
      } else {
        writeFrameSet(name);
      }

      out.print(
          "</body></html>");
    }

    private void writeFrameSet(String fsname) {
      FrameSet fs = fss.findFrameSet(fsname, null);
      if (fs == null) {
        out.print("Unknown FrameSet Name: "+fsname+"<p>\n");
        return;
      }
      writeFrameSet(fs);
    }

    private void writeFrameSet(FrameSet fs) {

      Properties slot_value_pairs = new Properties();
      for (int i = 0; i < NUM_SEARCH_SLOTS; i++) {
        if (slots[i] == null) continue;
        slot_value_pairs.setProperty(slots[i], values[i]);
      }

      Set frames;
      if (kind.equals("*")) {
        frames = new HashSet();
        Collection kinds = fs.getPrototypes();
        if (kinds != null && !kinds.isEmpty()) {
	    for (Iterator i2 = Sortings.sort(kinds, cmp).iterator();
              i2.hasNext();
              ) {
	      String k = ((PrototypeFrame) i2.next()).getName();
            Set set = fs.findFrames(k, slot_value_pairs);
            if (set == null) continue;
            frames.addAll(set);
          }
        }
      } else if (kind.equals("--Prototypes--")) {
	  frames = new HashSet();
	  frames.addAll(fs.getPrototypes());
      } else {
        frames = fs.findFrames(kind, slot_value_pairs);
      }

      int nframes = (frames == null ? 0 : frames.size());
      out.print(
          "Found "+nframes+" matching Frame"+
          (nframes == 1 ? "" : "s")+
          (nframes > 0 ? ":<p>" : ".")+
          "\n");
      if (nframes == 0) {
        return;
      }

      int i = 0;
      for (Iterator itr = frames.iterator();
          itr.hasNext();
          ) {
        Frame f = (Frame) itr.next();
        i++;
        writeFrame(f, fs, i);
        out.print("<p>\n");
      }
    }

    private void writeFrame(Frame f, FrameSet fs, int i) {
      UID uid = f.getUID();

      boolean selected = false;
      boolean modified = false;
      Object oldValue = null;
      if (modifyUID != null &&
          modifyUID.equals(uid) &&
          newSlot != null) {
        selected = true;
        out.print("<a name=\"modified\">");
        Object o = f.getValue(newSlot);
        if (!(o == null ?
              newValue == null :
              o.equals(newValue))) {
          if (newValue == null) {
            System.out.println("FIXME: removeValue("+newSlot+")");
          }
          f.setValue(newSlot, newValue);
          modified = true;
          oldValue = o;
        }
      }

      out.print(
          "<table border=1>\n"+
          "<tr bgcolor=\"DDDDDD\"><th colspan=4>"+
          (f instanceof PrototypeFrame ? "Prototype" : "Frame")+
          " "+i+"</th></tr>\n"+
          "<tr><td><table border=0>\n"+
          "<tr><td>FrameSet Name: &nbsp;</td><td colspan=3>"+
          fs.getName()+"</td></tr>\n"+
          "<tr><td>Kind: &nbsp;</td><td colspan=3>"+
          (f instanceof PrototypeFrame ? ((PrototypeFrame) f).getName() : f.getKind())+
	   "</td></tr>\n"+
          "<tr><td>UID: &nbsp;</td><td colspan=3>"+
          linkToUID(f.getUID())+
          "</td></tr>\n");
      PrototypeFrame proto = f.getPrototype();
      out.print(
          "<tr><td>Prototype: &nbsp;</td><td colspan=3>"+
          (proto == null ? "<i>null</i>" :
           proto.getName()+" ("+linkToUID(proto.getUID())+")")+
          "</td></tr>\n");
      if (f instanceof DataFrame) {
	  DataFrame container = ((DataFrame) f).containerFrame();
	  out.print(
		    "<tr><td>Container: &nbsp;</td><td colspan=3>"+
		    (container == null ? "<i>null</i>" :
		     container.getKind()+" ("+linkToUID(container.getUID())+")")+
		    "</td></tr>\n");
      }
      Map vp = null;
      String description = null;
      if (f instanceof DataFrame) {
	  vp = ((DataFrame) f).slotDescriptions();
	  description = "All Slots";
      } else if (f instanceof PrototypeFrame) {
	  vp = ((PrototypeFrame) f).getSlotDefinitions();
	  description = "Declared Slots";
      }
      int nvp = (vp == null ? 0 : vp.size());
      out.print(
		"<tr><td colspan=4>"+description+"["+nvp+"]:</td></tr>\n");
      if (nvp > 0) {
	  int j = 0;
	  Iterator itr = vp.entrySet().iterator();
	  while (itr.hasNext()) {
	      Map.Entry entry = (Map.Entry) itr.next();
	      String key = (String) entry.getKey();
	      Object val = entry.getValue();
	      if (val instanceof SlotDescription) 
		  val = ((SlotDescription)val).value;
	      else if (val instanceof Attributes)
		  val = ((Attributes) val).getValue("default-value");
	      j++;
	      out.print(
			"<tr><td>&nbsp;&nbsp;"+j+":&nbsp;</td>"+
			"<td>"+key+"</td><td>"+
			(val == DataFrame.NIL || val == null ? "&lt;no-value&gt;" : val)+
			"</td>"+
			"<td>"+
			(modified && key.equals(newSlot) ?
			 "<b>(was "+
			 (oldValue == DataFrame.NIL ? "&lt;no-value&gt;" : oldValue)+
			 ")</b>" : "")+
			"</td>"+
			"</tr>\n");
	  }
      }
      out.print(
          "<tr>"+
          "<form method=\"GET\" action=\""+
          request.getRequestURI()+
          "#modified"+
          "\" target=\""+RESULT_PAGE+"\">\n"+
          "<input type=hidden name=\""+PAGE_PARAM+
          "\" value=\""+RESULT_PAGE+"\"/>\n"+
          "<input type=hidden name=\""+NAME_PARAM+
          "\" value=\""+name+"\"/>\n"+
          "<input type=hidden name=\""+KIND_PARAM+
          "\" value=\""+kind+"\"/>\n");
      for (int j = 0; j < NUM_SEARCH_SLOTS; j++) {
        if (slots[j] == null) continue;
        out.print(
            "<input type=hidden name=\""+SLOT_PARAM+j+
            "\" value=\""+slots[j]+"\"/>\n");
        if (values[j] != null) {
          out.print(
              "<input type=hidden name=\""+VALUE_PARAM+j+
              "\" value=\""+values[j]+"\"/>\n");
        }
      }
      out.print(
          "<input type=hidden name=\""+UID_PARAM+
          "\" value=\""+encodeUID(f.getUID())+"\"/>\n");
      out.print(
          "<td>"+
          (modified ? "<b>Modified</b>" : "Modify")+
          " slot:&nbsp;</td>"+
          "<td><input name=\""+
          NEW_SLOT_PARAM+"\" type=\"text\" size=\"20\" value=\""+
          (selected && newSlot != null ? newSlot : "")+
          "\"></td>"+
          "<td><input name=\""+
          NEW_VALUE_PARAM+"\" type=\"text\" size=\"20\" value=\""+
          (selected && newValue != null ? newValue : "")+
          "\"></td>"+
          "<td><input type=\"submit\" value=\"Modify\"/>\n"+
          "</td>"+
          "</form>\n"+
          "</tr>\n"+
          "</table>"+
          "</td></tr></table>\n");
    }

    private String linkToUID(UID uid) {
      if (uid == null) {
        return "<i>null</i>";
      }
      return
        "<a href=\"/$"+getEncodedAgentName()+
        "/tasks?mode=10&uid="+encodeUID(uid)+
        "\">"+uid+"</a>";
    }
    private String encodeUID(UID uid) {
      try {
        return URLEncoder.encode(uid.toString(), "UTF-8");
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Unable to encode UID: "+uid);
      }
    }
    private UID decodeUID(String s) {
      try {
        String s2 = URLDecoder.decode(s, "UTF-8");
        return UID.toUID(s2);
      } catch (Exception e) {
        throw new IllegalArgumentException(
            "Unable to decode UID: "+s);
      }
    }

    private String getParameter(String name) {
      return getParameter(name, null);
    }
    private String getParameter(String name, String defaultValue) {
      String value = request.getParameter(name);
      if (value != null) {
        value = value.trim();
        if (value.length() == 0) {
          value = null;
        }
      }
      if (value == null) {
        value = defaultValue;
      }
      return value;
    }
  }
}
