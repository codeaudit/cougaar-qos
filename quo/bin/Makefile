# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

.SUFFIXES : .dev

nodes=$(wildcard *Node)
#links=$(foreach file, $(nodes), $(file).dev)
links=$(addsuffix .dev, $(nodes))

%.dev: 
	ln -s devwrapper $@


install: $(links)

clean:
	rm -f *.dev


