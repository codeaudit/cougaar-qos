package org.cougaar.core.qos.profile;

import java.lang.reflect.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.cougaar.core.agent.*;
import org.cougaar.core.component.*;
import org.cougaar.core.mts.*;
import org.cougaar.core.node.*;
import org.cougaar.core.qos.metrics.*;
import org.cougaar.core.service.*;
import org.cougaar.core.service.wp.*;
import org.cougaar.core.thread.*;
import org.cougaar.core.wp.resolver.*;
import org.cougaar.util.*;

/**
 * This component profiles the aggregate message traffic of
 * all local agents to any target.
 * <p>
 * Example output:<pre> 
 *   nt - #send_avg_queue_len,
 *     send_msg_bytes, send_header_bytes, send_ack_bytes, send_bytes, send_node_count, send_count, 
 *     recv_msg_bytes, recv_header_bytes, recv_ack_bytes, recv_bytes, recv_node_count, recv_count, 
 *     histogram_100_bytes, histogram_200_bytes, histogram_500_bytes, histogram_1000_bytes, histogram_2000_bytes, histogram_5000_bytes, histogram_10000_bytes, histogram_20000_bytes, histogram_50000_bytes, histogram_100000_bytes, histogram_200000_bytes, histogram_500000_bytes, histogram_1000000_bytes, histogram_2000000_bytes, histogram_5000000_bytes, histogram_10000000_bytes,
 *   nt - 0.87,
 *     228327741, 87931736, 87931242, 404190719, 552, 178546,
 *     161952252, 87931242, 93093562, 342977056, 552, 178545,
 *     0, 0, 2, 70, 177922, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
 * <pre> 
 *
 * @see ProfilerCoordinator required coordinator component
 */ 
public class NodeTraffic extends ProfilerBase {
  private static final DecimalFormat FORMAT_DECIMAL = new DecimalFormat("#0.00");
  private static final String[] FIELDS;
  static {
    List l = new ArrayList();
    l.add("send_avg_queue_len");
    l.add("send_msg_bytes");
    l.add("send_header_bytes");
    l.add("send_ack_bytes");
    l.add("send_bytes");
    l.add("send_node_count");
    l.add("send_count");
    l.add("recv_msg_bytes");
    l.add("recv_header_bytes");
    l.add("recv_ack_bytes");
    l.add("recv_bytes");
    l.add("recv_node_count");
    l.add("recv_count");
    for (int i = 1; i < MessageStatistics.NBINS; i++) {
      l.add("histogram_"+MessageStatistics.BIN_SIZES[i]+"_bytes");
    }
    FIELDS = (String[]) l.toArray(new String[0]);
  };
  private static final String HEADER = toHeader(FIELDS);

  public MessageStatisticsService mss;

  public void load() {
    super.load();
    findServiceLater(
        "mss",
        "org.cougaar.core.service.MessageStatisticsService");
  }
  public void run() {
    log("org.cougaar.core.qos.profile.node_traffic.nt",
        HEADER, getNodeTraffic());
  }
  private String getNodeTraffic() {
    final MessageStatistics.Statistics stats = 
      mss.getMessageStatistics(false);
    if (stats == null) {
      return "err";
    }
    StringBuffer buf = new StringBuffer();
    buf.append(FORMAT_DECIMAL.format(
          stats.averageMessageQueueLength
          )).append(", ");
    long[] numbers = new long[] {
      stats.totalSentMessageBytes,
      stats.totalSentHeaderBytes,
      stats.totalSentAckBytes,
      (stats.totalSentMessageBytes + 
       stats.totalSentHeaderBytes +
       stats.totalSentAckBytes),
      stats.histogram[0],
      stats.totalSentMessageCount,
      stats.totalRecvMessageBytes,
      stats.totalRecvHeaderBytes,
      stats.totalRecvAckBytes,
      (stats.totalRecvMessageBytes + 
       stats.totalRecvHeaderBytes +
       stats.totalRecvAckBytes),
      stats.histogram[0],
      stats.totalRecvMessageCount,
    };
    for (int i = 0; i < numbers.length; i++) {
      buf.append(numbers[i]).append(", ");
    }
    for (int i = 1; i < MessageStatistics.NBINS; i++) {
      buf.append(stats.histogram[i]).append(", ");
    }
    return buf.toString();
  }
}
