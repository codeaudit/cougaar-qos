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
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.cougaar.core.servlet.ComponentServlet;
import org.cougaar.core.service.LoggingService;
import org.cougaar.core.util.UID;

/**
 * This servlet displays {@link FrameSet}s and allows the user
 * to modify slot values.
 * <p> 
 * To load this servlet, add the following to any agent's XML
 * configuration:<pre> 
 *    &lt;component
 *      class="org.cougaar.core.qos.frame.FrameViewerServlet"&gt;
 *      &lt;argument&gt;/color&lt;/argument&gt;
 *    &lt;/component&gt;
 * </pre>
 */
public class FrameViewerServlet extends ComponentServlet {

  private LoggingService log;
  private FrameSetService fss;

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
      out.print(
          "<html><body>\n"+
          "<form method=\"GET\" action=\""+
          request.getRequestURI()+
          "\" target=\""+RESULT_PAGE+"\">\n"+
          "<b>Find FrameSet:</b><br>\n"+
          "<input type=hidden name=\""+PAGE_PARAM+
          "\" value=\""+RESULT_PAGE+"\"/>\n"+
          "<table border=\"0\">\n");
      writeInput("FrameSet Name:&nbsp;", NAME_PARAM, name);
      writeInput("Prototype Kind:&nbsp;", KIND_PARAM, kind);
      out.print("<tr><td colspan=3>Properties:</td></tr>\n");
      for (int i = 0; i < NUM_SEARCH_SLOTS; i++) {
        writeInput(
            "&nbsp;&nbsp;"+i+":&nbsp;",
            SLOT_PARAM+i, slots[i],
            VALUE_PARAM+i, values[i]);
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

      FrameSet fs = fss.findFrameSet(name, null);
      if (fs == null) {
        out.print(
            "Unknown FrameSet Name.\n"+
            "</body></html>\n");
        return;
      }

      Properties slot_value_pairs = new Properties();
      for (int i = 0; i < NUM_SEARCH_SLOTS; i++) {
        if (slots[i] == null) continue;
        slot_value_pairs.setProperty(slots[i], values[i]);
      }

      Set frames = fs.findFrames(kind, slot_value_pairs);

      int nframes = (frames == null ? 0 : frames.size());
      out.print(
          "Found "+nframes+" matching Frame"+
          (nframes == 1 ? "" : "s")+
          (nframes > 0 ? ":<p>" : ".")+
          "\n");
      if (nframes == 0) {
        out.print("</body></html>");
        return;
      }

      int i = 0;
      for (Iterator itr = frames.iterator();
          itr.hasNext();
          ) {
        Frame f = (Frame) itr.next();
        i++;
        writeFrame(f, i);
        out.print("<p>\n");
      }
      out.print(
          "</body></html>");
    }

    private void writeFrame(Frame f, int i) {
      out.print(
          "<a name=\"frame"+i+"\">");

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
          "<tr><th colspan=4>"+
          (f instanceof PrototypeFrame ? "Prototype" : "Frame")+
          " "+i+"</th></tr>\n"+
          "<tr><td><table border=0>\n"+
          "<tr><td>FrameSet Name: &nbsp;</td><td colspan=3>"+
          name+"</td></tr>\n"+
          "<tr><td>Kind: &nbsp;</td><td colspan=3>"+
          f.getKind()+"</td></tr>\n"+
          "<tr><td>UID: &nbsp;</td><td colspan=3>"+
          linkToUID(f.getUID())+
          "</td></tr>\n");
      Frame proto = f.getPrototype();
      out.print(
          "<tr><td>Prototype: &nbsp;</td><td colspan=3>"+
          (proto == null ? "<i>null</i>" :
           proto.getKind()+" ("+linkToUID(proto.getUID())+")")+
          "</td></tr>\n");
      Frame parent = f.getParent();
      out.print(
          "<tr><td>Parent: &nbsp;</td><td colspan=3>"+
          (parent == null ? "<i>null</i>" :
           parent.getKind()+" ("+linkToUID(parent.getUID())+")")+
          "</td></tr>\n");
      Properties vp = f.getLocalSlots();
      int nvp = (vp == null ? 0 : vp.size());
      out.print(
          "<tr><td colspan=4>Properties["+nvp+"]:</td></tr>\n");
      if (nvp > 0) {
        int j = 0;
        for (Enumeration en = vp.propertyNames();
            en.hasMoreElements();
            ) {
          String key = (String) en.nextElement();
          String val = vp.getProperty(key); // must be string!
          j++;
          out.print(
              "<tr><td>&nbsp;&nbsp;"+j+":&nbsp;</td>"+
              "<td>"+key+"</td><td>"+val+"</td>"+
              "<td>"+
              (modified && key.equals(newSlot) ?
               "<b>(was "+oldValue+")</b>" : "")+
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

    private void writeInput(
        String description, String name, String value) {
      out.print(
          "<tr><td>"+description+"</td>"+
          "<td colspan=2><input name=\""+
          name+"\" type=\"text\" size=\"40\" value=\""+
          (value == null ? "" : value)+
          "\"></td>"+
          "</tr>\n");
    }
    private void writeInput(
        String description,
        String n1, String v1, String n2, String v2) {
      out.print(
          "<tr><td>"+description+"</td>"+
          "<td><input name=\""+
          n1+"\" type=\"text\" size=\"20\" value=\""+
          (v1 == null ? "" : v1)+
          "\"></td>"+
          "<td><input name=\""+
          n2+"\" type=\"text\" size=\"20\" value=\""+
          (v2 == null ? "" : v2)+
          "\"></td>"+
          "</tr>\n");
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
