COUGAARJAR=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar

COUGAAR3RDPARTY=${COUGAAR_SRC_PATH}/quo/dev/3rdparty

CLASSPATH=${COUGAARJAR}:${COUGAAR3RDPARTY}/QuoRSS.jar:${COUGAAR3RDPARTY}/QuoKernel.jar:${COUGAAR3RDPARTY}/event.jar:.
#CLASSPATH=${COUGAAR_INSTALL_PATH}/lib/core.jar:${QUO_ROOT}/lib/quoSumo.jar

export CLASSPATH

src := src/org/cougaar/core/qos/rss/*.java

jar := newqos.jar

all: $(jar)


$(jar):  $(src) 
	javac -d . $(src) 
	rmic -d . org.cougaar.core.qos.rss.MetricSCTie
	jar cf $(jar) org


clean:
	rm -rf $(jar) org 

