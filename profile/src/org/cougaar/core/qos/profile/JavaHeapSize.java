package org.cougaar.core.qos.profile;

/**
 * This component profiles the JVM heap size (used, free, total,
 * max).
 * <p>
 * Example output:<pre>
 *   jvmheap - #used_bytes, free_bytes, total_bytes, max_bytes
 *   jvmheap - 5430096, 5084336, 10514432, 66650112
 * </pre>
 * In this example, the current JVM heap size is 5.18 mb. 
 *
 * @see ProfilerCoordinator required coordinator component
 */
public class JavaHeapSize extends ProfilerBase {
  // 0.07 0.36 0.37 1/262 5191
  private static final String[] FIELDS = new String[] {
    "used_bytes",
    "free_bytes",
    "total_bytes",
    "max_bytes"
  };
  private static final String HEADER = toHeader(FIELDS);
  @Override
public void run() {
    log("org.cougaar.core.qos.profile.jvmheap", HEADER, getJavaHeap());
  }
  private String getJavaHeap() {
    Runtime rt = Runtime.getRuntime();
    long freeMemory = rt.freeMemory();
    long totalMemory = rt.totalMemory();
    long maxMemory = rt.maxMemory();
    long usedMemory = totalMemory - freeMemory;
    return usedMemory+", "+freeMemory+", "+totalMemory+", "+maxMemory;
  }
}
