src=$(shell find .  -name "*.java")
classes=.classes
jar=newquo.jar
core=${COUGAAR_SRC_PATH}/core/newcore.jar:${COUGAAR_SRC_PATH}/util/newutil.jar
qos=${COUGAAR_SRC_PATH}/qos/newqos.jar
quo=dev/3rdparty/quoSumo.jar


# for rmic
pathdir=org/cougaar/lib/mquo
srcdir=src/$(pathdir)


CLASSPATH=$(core):$(qos):$(quo):$(classes)
export CLASSPATH


all: developers


# This should be what the standard COUGAAR build script does, more or less
compile: $(src)
	javac  -d $(classes) $(src)


# Developers do this

developers:  $(jar)

$(classes):
	mkdir -p $(classes)


$(srcdir)/MetricSCTie_Stub.java: $(srcdir)/MetricSCTie.java
	javac -d . $(srcdir)/MetricSCTie.java
	rmic -v1.2 -d .  -keep org.cougaar.lib.mquo.MetricSCTie
	mv $(pathdir)/*.java $(srcdir)


$(jar): $(classes) $(src) 
	javac -deprecation -d $(classes) $(src)
	jar cf $(jar) -C $(classes) .


clean:
	rm -rf $(jar) $(classes)



