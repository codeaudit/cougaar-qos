COUGAARJAR=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar:${COUGAAR_INSTALL_PATH}/lib/util.jar

COUGAAR3RDPARTY=${COUGAAR_SRC_PATH}/quo/dev/3rdparty

CLASSPATH=${COUGAARJAR}:${COUGAAR3RDPARTY}/QuoRSS.jar:${COUGAAR3RDPARTY}/QuoKernel.jar:${COUGAAR3RDPARTY}/event.jar:.
#CLASSPATH=${COUGAAR_INSTALL_PATH}/lib/core.jar:${QUO_ROOT}/lib/quoSumo.jar

export CLASSPATH
pathdir=org/cougaar/core/qos/rss
srcdir=src/$(pathdir)
src=$(srcdir)/*.java

jar=newqos.jar

all: $(jar)


$(srcdir)/MetricSCTie_Stub.java: $(srcdir)/MetricSCTie.java
	javac -d . $(srcdir)/MetricSCTie.java
	rmic -d . -keep org.cougaar.core.qos.rss.MetricSCTie
	mv $(pathdir)/*.java $(srcdir)


$(jar):  $(src) 
	javac -d . $(src) 
	jar cf $(jar) org


clean:
	rm -rf $(jar) org 

