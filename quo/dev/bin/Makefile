# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

LINKS := Node
LINKS += QuoNode
LINKS += SSLNode
LINKS += CorbaNode
LINKS += ComboNode
LINKS += NoisyNode
LINKS += SecureNode
LINKS += SeqNode
LINKS += SerializeNode
LINKS += SimpleNode
LINKS += StatNode
LINKS += TraceNode

install: $(LINKS)

Node:
	ln -s wrapper Node

QuoNode:
	ln -s wrapper QuoNode

SSLNode:
	ln -s wrapper SSLNode

CorbaNode:
	ln -s wrapper CorbaNode

ComboNode:
	ln -s wrapper ComboNode

NoisyNode:
	ln -s wrapper NoisyNode

SecureNode:
	ln -s wrapper SecureNode

SeqNode:
	ln -s wrapper SeqNode

SerializeNode:
	ln -s wrapper SerializeNode

SimpleNode:
	ln -s wrapper SimpleNode

StatNode:
	ln -s wrapper StatNode

TraceNode:
	ln -s wrapper TraceNode

clean:
	rm -f $(LINKS)

