# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

LINKS := Node.Link
LINKS += QuoNode.Link
LINKS += SSLNode.Link
LINKS += CorbaNode.Link
LINKS += ComboNode.Link
LINKS += NoisyNode.Link
LINKS += SecureNode.Link
LINKS += SeqNode.Link
LINKS += SerializeNode.Link
LINKS += SimpleNode.Link
LINKS += StatNode.Link
LINKS += TraceNode.Link

install: $(LINKS)

Node.Link:
	ln -s wrapper Node

QuoNode.Link:
	ln -s wrapper QuoNode

SSLNode.Link:
	ln -s wrapper SSLNode

CorbaNode.Link:
	ln -s wrapper CorbaNode

ComboNode.Link:
	ln -s wrapper ComboNode

NoisyNode.Link:
	ln -s wrapper NoisyNode

SecureNode.Link:
	ln -s wrapper SecureNode

SeqNode.Link:
	ln -s wrapper SeqNode

SerializeNode.Link:
	ln -s wrapper SerializeNode

SimpleNode.Link:
	ln -s wrapper SimpleNode

StatNode.Link:
	ln -s wrapper StatNode

TraceNode.Link:
	ln -s wrapper TraceNode

clean:
	rm -f $(LINKS)

