// Stub class generated by rmic, do not edit.
// Contents subject to change without notice.

package org.cougaar.core.qos.rss;

public final class MetricSCTie_Stub
    extends java.rmi.server.RemoteStub
    implements org.cougaar.core.qos.rss.MetricSC, java.rmi.Remote
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("boolean booleanValue()"),
	new java.rmi.server.Operation("char charValue()"),
	new java.rmi.server.Operation("double doubleValue()"),
	new java.rmi.server.Operation("float floatValue()"),
	new java.rmi.server.Operation("void init(org.cougaar.core.qos.metrics.MetricsService)"),
	new java.rmi.server.Operation("boolean isReady()"),
	new java.rmi.server.Operation("int longValue()"),
	new java.rmi.server.Operation("long longlongValue()"),
	new java.rmi.server.Operation("void newPath(java.lang.String)"),
	new java.rmi.server.Operation("byte octetValue()"),
	new java.rmi.server.Operation("short shortValue()"),
	new java.rmi.server.Operation("java.lang.String stringValue()")
    };
    
    private static final long interfaceHash = -926429695856777445L;
    
    private static final long serialVersionUID = 2;
    
    private static boolean useNewInvoke;
    private static java.lang.reflect.Method $method_booleanValue_0;
    private static java.lang.reflect.Method $method_charValue_1;
    private static java.lang.reflect.Method $method_doubleValue_2;
    private static java.lang.reflect.Method $method_floatValue_3;
    private static java.lang.reflect.Method $method_init_4;
    private static java.lang.reflect.Method $method_isReady_5;
    private static java.lang.reflect.Method $method_longValue_6;
    private static java.lang.reflect.Method $method_longlongValue_7;
    private static java.lang.reflect.Method $method_newPath_8;
    private static java.lang.reflect.Method $method_octetValue_9;
    private static java.lang.reflect.Method $method_shortValue_10;
    private static java.lang.reflect.Method $method_stringValue_11;
    
    static {
	try {
	    java.rmi.server.RemoteRef.class.getMethod("invoke",
		new java.lang.Class[] {
		    java.rmi.Remote.class,
		    java.lang.reflect.Method.class,
		    java.lang.Object[].class,
		    long.class
		});
	    useNewInvoke = true;
	    $method_booleanValue_0 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("booleanValue", new java.lang.Class[] {});
	    $method_charValue_1 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("charValue", new java.lang.Class[] {});
	    $method_doubleValue_2 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("doubleValue", new java.lang.Class[] {});
	    $method_floatValue_3 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("floatValue", new java.lang.Class[] {});
	    $method_init_4 = org.cougaar.core.qos.rss.MetricSCOperations.class.getMethod("init", new java.lang.Class[] {org.cougaar.core.qos.metrics.MetricsService.class});
	    $method_isReady_5 = com.bbn.quo.rmi.SysCondOperations.class.getMethod("isReady", new java.lang.Class[] {});
	    $method_longValue_6 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("longValue", new java.lang.Class[] {});
	    $method_longlongValue_7 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("longlongValue", new java.lang.Class[] {});
	    $method_newPath_8 = org.cougaar.core.qos.rss.MetricSCOperations.class.getMethod("newPath", new java.lang.Class[] {java.lang.String.class});
	    $method_octetValue_9 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("octetValue", new java.lang.Class[] {});
	    $method_shortValue_10 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("shortValue", new java.lang.Class[] {});
	    $method_stringValue_11 = com.bbn.quo.rmi.ReadOnlyValueSCOperations.class.getMethod("stringValue", new java.lang.Class[] {});
	} catch (java.lang.NoSuchMethodException e) {
	    useNewInvoke = false;
	}
    }
    
    // constructors
    public MetricSCTie_Stub() {
	super();
    }
    public MetricSCTie_Stub(java.rmi.server.RemoteRef ref) {
	super(ref);
    }
    
    // methods from remote interfaces
    
    // implementation of booleanValue()
    public boolean booleanValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_booleanValue_0, null, 321797395366213756L);
		return ((java.lang.Boolean) $result).booleanValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
		ref.invoke(call);
		boolean $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readBoolean();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of charValue()
    public char charValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_charValue_1, null, -4288014995842806239L);
		return ((java.lang.Character) $result).charValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 1, interfaceHash);
		ref.invoke(call);
		char $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readChar();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of doubleValue()
    public double doubleValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_doubleValue_2, null, -2735701587009932691L);
		return ((java.lang.Double) $result).doubleValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 2, interfaceHash);
		ref.invoke(call);
		double $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readDouble();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of floatValue()
    public float floatValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_floatValue_3, null, 7335563984231238833L);
		return ((java.lang.Float) $result).floatValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 3, interfaceHash);
		ref.invoke(call);
		float $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readFloat();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of init(MetricsService)
    public void init(org.cougaar.core.qos.metrics.MetricsService $param_MetricsService_1)
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		ref.invoke(this, $method_init_4, new java.lang.Object[] {$param_MetricsService_1}, 3202323540986498357L);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 4, interfaceHash);
		try {
		    java.io.ObjectOutput out = call.getOutputStream();
		    out.writeObject($param_MetricsService_1);
		} catch (java.io.IOException e) {
		    throw new java.rmi.MarshalException("error marshalling arguments", e);
		}
		ref.invoke(call);
		ref.done(call);
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of isReady()
    public boolean isReady()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_isReady_5, null, 5601281247722995301L);
		return ((java.lang.Boolean) $result).booleanValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 5, interfaceHash);
		ref.invoke(call);
		boolean $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readBoolean();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of longValue()
    public int longValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_longValue_6, null, -2681803097244643466L);
		return ((java.lang.Integer) $result).intValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 6, interfaceHash);
		ref.invoke(call);
		int $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readInt();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of longlongValue()
    public long longlongValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_longlongValue_7, null, -4309978758569769243L);
		return ((java.lang.Long) $result).longValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 7, interfaceHash);
		ref.invoke(call);
		long $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readLong();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of newPath(String)
    public void newPath(java.lang.String $param_String_1)
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		ref.invoke(this, $method_newPath_8, new java.lang.Object[] {$param_String_1}, -2942907285579791263L);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 8, interfaceHash);
		try {
		    java.io.ObjectOutput out = call.getOutputStream();
		    out.writeObject($param_String_1);
		} catch (java.io.IOException e) {
		    throw new java.rmi.MarshalException("error marshalling arguments", e);
		}
		ref.invoke(call);
		ref.done(call);
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of octetValue()
    public byte octetValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_octetValue_9, null, 7314518805575796051L);
		return ((java.lang.Byte) $result).byteValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 9, interfaceHash);
		ref.invoke(call);
		byte $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readByte();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of shortValue()
    public short shortValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_shortValue_10, null, -797248613395323126L);
		return ((java.lang.Short) $result).shortValue();
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 10, interfaceHash);
		ref.invoke(call);
		short $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = in.readShort();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
    
    // implementation of stringValue()
    public java.lang.String stringValue()
	throws java.rmi.RemoteException
    {
	try {
	    if (useNewInvoke) {
		Object $result = ref.invoke(this, $method_stringValue_11, null, 1588607059140066992L);
		return ((java.lang.String) $result);
	    } else {
		java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 11, interfaceHash);
		ref.invoke(call);
		java.lang.String $result;
		try {
		    java.io.ObjectInput in = call.getInputStream();
		    $result = (java.lang.String) in.readObject();
		} catch (java.io.IOException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} catch (java.lang.ClassNotFoundException e) {
		    throw new java.rmi.UnmarshalException("error unmarshalling return", e);
		} finally {
		    ref.done(call);
		}
		return $result;
	    }
	} catch (java.lang.RuntimeException e) {
	    throw e;
	} catch (java.rmi.RemoteException e) {
	    throw e;
	} catch (java.lang.Exception e) {
	    throw new java.rmi.UnexpectedException("undeclared checked exception", e);
	}
    }
}
