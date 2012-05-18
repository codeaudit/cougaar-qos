/*
 * <copyright>
 *  
 *  Copyright 1997-2004 BBNT Solutions, LLC
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

package org.cougaar.core.qos.tmatrix;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cougaar.core.mts.MessageAddress;
import org.cougaar.core.qos.metrics.DecayingHistory;
import org.cougaar.util.log.Logger;
import org.cougaar.util.log.Logging;

/*
 * Traffic Matrix of TrafficRecords, with accessor methods
 */
public class TrafficMatrix implements Serializable
	       
{
    /**
    * 
    */
   private static final long serialVersionUID = 1L;
   private HashMap outermatrix; 

    /*
     * Inner class defining a TrafficRecord - there are two records per agent-pair in 
     * the global traffic matrix, defining traffic flow. 
     * E.g. A1->A2 & A2->A1
     */
    public static class TrafficRecord 
	extends DecayingHistory.SnapShot 
	implements Serializable 
    {
	/**
       * 
       */
      private static final long serialVersionUID = 1L;
   public double msgCount;
	public double byteCount;
	
	public TrafficRecord() {
	}
	
      // deep copy
	public  TrafficRecord(TrafficRecord record) {
	    this.msgCount = record.msgCount;
	    this.byteCount = record.byteCount;
	}
	
	// pass through methods
	public double getMsgCount() {
	    return msgCount;
	}
	
	public double getByteCount() {
	    return byteCount;
	}
	
	@Override
   public String toString() {
	    return "Traffic Record: msg="+msgCount+" bytes="+byteCount;
	}

	public TrafficRecord ratifyRecord(TrafficRecord newR) 
	{
	    TrafficRecord result =  new TrafficRecord();

	    double deltaTime = (newR.timestamp - timestamp)/1000.0;
	    if (deltaTime == 0.0) {
		return null;
	    }
	
	    double msgCount = newR.getMsgCount() - getMsgCount(); 
	    double msgRate = msgCount / deltaTime;
	    result.msgCount = msgRate;
	
	    double byteCount = newR.getByteCount() - getByteCount();
	    double byteRate = byteCount / deltaTime;
	    result.byteCount = byteRate;
	    return result; 
	}
    

	
    } //TrafficRecord
    
    /* construct new traffic matrix -  
     * outer = agent to agent
     * inner = Agent1 to Record1, Record2, Record3,...etc, Agent2 to Record2,...etc.
     *
     ----       ----------
     |A1| ----> |A2|..|..|
     ---        |--|--|--|
     |A2|       |TR|..|..|
     ---        ----------
     |A3|
     ---
     
     Which essentially looks conceptually like: 
     
      A1  A2  A3
      
      A1  R   R   R
      
      A2  R   R   R 
      
      A3  R   R   R
      
    */
  
  public TrafficMatrix() {
    // something extraordinarily large - don't know how many agents we're mapping to
    this.outermatrix = new HashMap(89);  
  }     
    
    // Deep copy
    public TrafficMatrix(TrafficMatrix matrix) {
	//log.debug("TrafficMatrix passed to constructor: "+matrix);
	this.outermatrix = new HashMap(89);  	
	synchronized (matrix) {	  	    
	    Iterator outer = matrix.outermatrix.entrySet().iterator(); 	    
	    while (outer.hasNext()) {
		Map.Entry entry = (Map.Entry) outer.next();
		MessageAddress orig = (MessageAddress) entry.getKey();
		HashMap row = (HashMap) entry.getValue();
		HashMap new_row = new HashMap();
		this.outermatrix.put(orig, new_row);
		Iterator inner = row.entrySet().iterator();
		while (inner.hasNext()) {
		    Map.Entry sub_entry = (Map.Entry) inner.next();
		    MessageAddress target = (MessageAddress) sub_entry.getKey();
		    TrafficRecord data = (TrafficRecord) sub_entry.getValue();
		    new_row.put(target, new TrafficRecord(data));
		}
	    }
	    
	}
    }

    private transient Logger log;
    
    private synchronized Logger getLogger () {
	if (log == null) 
	    log = Logging.getLogger(getClass().getName());
	return log;
    }


    public HashMap getOuter() {
	return this.outermatrix;
    }
    	
    public Set getKeySet() {
	return outermatrix.keySet();
    }
    

  public void addMsgCount(MessageAddress orig, MessageAddress target, int msgCount) {
    TrafficRecord record = getOrMakeRecord(orig, target);
    record.msgCount+=msgCount;
  }
  
  public void addByteCount(MessageAddress orig, MessageAddress target, int byteCount) {
    TrafficRecord record = getOrMakeRecord(orig, target);
    record.byteCount+=byteCount;
  }
    
    /*
     * Just like getOrMakeRecord, but returns false if either record, orig, or target is null
     */
    public boolean checkForRecord(MessageAddress orig_param, MessageAddress target_param) {
	if (orig_param==null || target_param==null) {
	    return false;
	}
	// else check for both present source & target
	Logger log = getLogger();
	if (log != null && log.isDebugEnabled() ) {
	    log.debug("checkForRecord: orig_param="+orig_param+", target_param="+target_param);
	}
	MessageAddress orig = orig_param.getPrimary();
	MessageAddress target = target_param.getPrimary();
	HashMap inner = null;
	
	// hash to record
	if(outermatrix.containsKey(orig)) { // has orig
	    inner = (HashMap) outermatrix.get(orig); // get the inner
	} else {
	    return false;
	}
	
	if(inner.containsKey(target)) { // has target
	    return true;
	} else { // no inner or record
	    return false;
	}
    }
    
  public synchronized TrafficRecord getOrMakeRecord(MessageAddress orig_param,
				       MessageAddress target_param) 
  {
    MessageAddress orig = orig_param.getPrimary();
    MessageAddress target = target_param.getPrimary();
    TrafficRecord record = null;
    HashMap inner = null;

    // hash to record
    if(outermatrix.containsKey(orig)) { // has orig
      inner = (HashMap) outermatrix.get(orig); // get the inner
    } else {
      inner = new HashMap();
      outermatrix.put(orig, inner);
    }

    if(inner.containsKey(target)) { // has target
	record = (TrafficRecord) inner.get(target);
    } else { // no inner or record
	record = new TrafficRecord();
	inner.put(target, record);
    }
    
    return record;
  }

  public synchronized TrafficRecord getRecord(MessageAddress orig_param,
				       MessageAddress target_param) 
  {
    MessageAddress orig = orig_param.getPrimary();
    MessageAddress target = target_param.getPrimary();
    HashMap inner = null;

    // return record if present

    if(!outermatrix.containsKey(orig)) return null; // not present

    inner = (HashMap) outermatrix.get(orig); // get the inner
    return (TrafficRecord) inner.get(target);
  }
  
  public synchronized void putRecord(MessageAddress orig_param, 
			MessageAddress target_param, 
			TrafficRecord record)
  {
    
      if( orig_param != null && target_param != null)
	{
	  MessageAddress orig = orig_param.getPrimary();
	  MessageAddress target = target_param.getPrimary();
	  // should check for existing key,  later
	  if(outermatrix.get(orig_param)!=null) {
	      HashMap inner = (HashMap)outermatrix.get(orig_param);
	      inner.put(target_param, record);
	  } 
	  else {
	      HashMap new_inner = new HashMap(1);
	      new_inner.put(target, record);
	      outermatrix.put(orig, new_inner);
	  }
	} else {
	  throw new NullPointerException("Cannot put in TrafficRecord, either Originator or Target is null");
	}
  }
  
    public TrafficIterator getIterator() {
	return new TrafficIterator();
    }
    

    /*
     * Iterator for Traffic Matrix
     */
    public class TrafficIterator implements Iterator 
    {
	
	private Iterator inner_i, outer_i;
	private Map.Entry next_inner_entry, next_outer_entry; // look-ahead
	public MessageAddress inner_key, outer_key;    // made public for snapshotMatrix()
	

	TrafficIterator()
	{
	    outer_i = outermatrix.entrySet().iterator();
	    getNextInnerIterator();
	    getNext();
	}

	private void getNextInnerIterator() 
	{
	    if (outer_i.hasNext()) {
		next_outer_entry = (Map.Entry) outer_i.next();
		HashMap inner = (HashMap) next_outer_entry.getValue();
		inner_i = inner.entrySet().iterator();
	    } else {
		inner_i = null;
	    }
	}

	private void getNext() 
	{
	    if (inner_i == null) {
		next_inner_entry = null;
	    }  else if (inner_i.hasNext()) {
		next_inner_entry = (Map.Entry) inner_i.next();
	    } else {
		getNextInnerIterator();
		getNext();
	    }
	}


	public void remove() 
	{
	    throw new RuntimeException("TrafficIterator does not support remove()");
	}

	public MessageAddress getOrig() {
	    return outer_key;
	}
    
	public MessageAddress getTarget() {
	    return inner_key;
	}

	public boolean hasNext() 
	{
	    return next_inner_entry != null;
	}

	public Object next() 
	{
	    Object result = null;
	    if (next_inner_entry != null) {
		outer_key = (MessageAddress) next_outer_entry.getKey();
		inner_key = (MessageAddress) next_inner_entry.getKey();
		result = next_inner_entry.getValue();
		getNext();
	    } else {
		inner_key = null;
		outer_key = null;
	    }
	    return result;
	}

    }

    // more utility methods here 
    /*
     * Run through new matrix, merge into main one
     */
    public void addMatrix(TrafficMatrix matrix) {		
	synchronized (outermatrix) {
	    Iterator outer = matrix.outermatrix.entrySet().iterator();
	    while (outer.hasNext()) {
		Map.Entry entry = (Map.Entry) outer.next();
		MessageAddress orig = (MessageAddress) entry.getKey();
		HashMap row = (HashMap) entry.getValue();	
		Iterator inner = row.entrySet().iterator();
		while (inner.hasNext()) {
		    Map.Entry sub_entry = (Map.Entry) inner.next();
		    MessageAddress target = (MessageAddress) sub_entry.getKey();
		    TrafficRecord data = (TrafficRecord) sub_entry.getValue();
		    putRecord(orig, target, data);
		}
	    }
	}
    }
    
    /*
     * Easy access methods for getting record information
     * These can return null
     */
    public double getByteCount(MessageAddress A1, MessageAddress A2) {
	TrafficRecord record = getRecord(A1, A2);
	if (record != null) 
	    return record.getByteCount();
	else
	    return -1.0;
    }
    public double getMsgCount(MessageAddress A1, MessageAddress A2) {
	TrafficRecord record = getRecord(A1, A2);
	if (record != null)
	    return record.getMsgCount();
	else
	    return -1.0;
	}
        
    
    @Override
   public String toString() {
	return prettyString("");
    }
    public String toPrettyString() {
	return prettyString("\n");
    }

    private String prettyString(String lineBreak) {

	TrafficIterator itr = this.getIterator();
	StringBuffer s = new StringBuffer();

	s.append("TrafficMatrix:");
	s.append(lineBreak);
	TrafficRecord record;
        String orig;
	String target;
	
	while (itr.hasNext()) {
	    record = (TrafficRecord) itr.next();	    	    
	    orig = itr.getOrig().toString();
	    target = itr.getTarget().toString();
	    s.append("org=");
	    s.append(orig);;
	    s.append(" trg=");
	    s.append(target);
	    s.append(" ");
	    s.append(record);
	    s.append(lineBreak);
	}
	return s.toString(); 
    }

    public TrafficMatrix ratify(TrafficMatrix newMatrix)
    {
	TrafficMatrix result = new TrafficMatrix();
	ratifyIncremental(newMatrix, result);
	return result;
    }

    public void ratifyIncremental(TrafficMatrix newMatrix,
				  TrafficMatrix result)
    {
	Logger log = getLogger();
	if (log.isDebugEnabled())
	    log.debug("New Matrix" + newMatrix.toPrettyString()+
		      "last Matrix" + toPrettyString());
	TrafficMatrix.TrafficIterator iter = newMatrix.getIterator();
	while(iter.hasNext()) {
	    TrafficMatrix.TrafficRecord newRecord =
		(TrafficMatrix.TrafficRecord) iter.next();
	    MessageAddress orig = iter.getOrig();
	    MessageAddress target = iter.getTarget();
	    // if no entry in old matrix
	    if( !checkForRecord(orig, target)) {
		// skip this entry
		continue;
	    } else {		    
		TrafficMatrix.TrafficRecord oldRecord = 
		    getOrMakeRecord(orig, target);
		TrafficMatrix.TrafficRecord rateRecord = 
		    oldRecord.ratifyRecord(newRecord);
		if (rateRecord != null) {
		    result.putRecord(orig, target, rateRecord);
		    if(log.isDebugEnabled()) {
			log.debug("Putting rateRecord: " + orig + ", " + target + ": " + rateRecord);
		    }
		}
	    }	
	}
    }
 
 
} // TrafficMatrix
