classes=.classes
jar=newquo.jar
#cougaar=${COUGAAR_INSTALL_PATH}/lib/core.jar
cougaar=${COUGAAR_SRC_PATH}/core/newcore.jar:${COUGAAR_SRC_PATH}/util/newutil.jar:${COUGAAR_INSTALL_PATH}/lib/planning.jar
qos=${COUGAAR_SRC_PATH}/qos/newqos.jar
src=$(shell find .  -name "*.java")
thirdparty=dev/3rdparty
depends=$(thirdparty)/QuoKernel.jar:$(thirdparty)/QuoRSS.jar:$(thirdparty)/jacorb.jar


# for rmic
pathdir=org/cougaar/lib/mquo
srcdir=src/$(pathdir)


CLASSPATH=$(cougaar):$(qos):$(depends):$(classes)
export CLASSPATH


all: developers


# This should be what the standard COUGAAR build script does, more or less
compile: $(src)
	javac  -d $(classes) $(src)




# Developers do this

developers: gen $(jar)

gen:
	mkdir -p $(classes)




$(srcdir)/MetricSCTie_Stub.java: $(srcdir)/MetricSCTie.java
	javac -d . $(srcdir)/MetricSCTie.java
	rmic -v1.2 -d .  -keep org.cougaar.lib.mquo.MetricSCTie
	mv $(pathdir)/*.java $(srcdir)


$(jar): $(src) 
	javac -deprecation -d $(classes) $(src)
	jar cf $(jar) -C $(classes) .




clean:
	rm -rf $(jar) $(classes)



