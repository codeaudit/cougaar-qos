# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

# define SRCDIR
include include.mk

idldir=.idl
idl=$(idldir)/$(srcdir)/MT.java
jar=newquo.jar
sources := $(srcdir)/*.java
sources += $(srcdir)/rmisocfac/*.java

all: build install cvsprep

build: prep $(jar)

install:
	cd 3rdparty; make

deinstall:
	cd 3rdparty; make clean

$(idl): idl/MT.idl
	install -d $(idldir)
	install -d $(classes)
	$(IDL2JAVA) -i2jpackage cougaar:$(pkg) -d $(idldir) idl/MT.idl
	$(JAVAC) -d $(classes) $(idldir)/$(srcdir)/*.java

prep: $(idl)
	cd qdl; make

$(jar):  $(sources)
	$(JAVAC) -d $(classes) $(sources)
	$(RMIC) -keepgenerated  -d $(classes)  $(rmi_classes)
	jar cf $(jar) -C $(classes) .

clean: 
	rm -rf $(jar) 


realclean: clean
	rm -rf $(idldir) $(classes)
	cd qdl; make clean
	cd 3rdparty; make clean

# test comment
cvsprep:
	install -d  $(buildsrc)/$(srcdir)
	install -d  $(buildsrc)/$(srcdir)/rmisocfac
	install -C $(srcdir)/*.java $(buildsrc)/$(srcdir)
	install -C $(srcdir)/rmisocfac/*.java $(buildsrc)/$(srcdir)/rmisocfac
	install -C $(idldir)/$(srcdir)/*.java $(buildsrc)/$(srcdir)
	install -C qdl/$(srcdir)/*.java $(buildsrc)/$(srcdir)
	install -C $(classes)/$(srcdir)/*.java $(buildsrc)/$(srcdir)






