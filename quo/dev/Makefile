# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

# define SRCDIR
include include.mk

idl=$(src)/$(srcdir)/MT.java

all: $(idl) gen thirdparty

thirdparty:
	cd 3rdparty; make

$(idl): idl/MT.idl
	$(IDL2JAVA) -i2jpackage cougaar:$(pkg) -d $(src) idl/MT.idl

gen:
	cd qdl; make


clean:
	cd qdl; make clean
