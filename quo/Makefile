classes=.classes
jar=newquo.jar
#cougaar=${COUGAAR_INSTALL_PATH}/lib/core.jar
cougaar=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar:${COUGAAR_INSTALL_PATH}/lib/util.jar
qos=${COUGAAR_SRC_PATH}/qos/newqos.jar
src=$(shell find .  -name "*.java")
thirdparty=dev/3rdparty
depends=$(thirdparty)/QuoKernel.jar:$(thirdparty)/QuoRSS.jar:$(thirdparty)/QuoInstr.jar:$(thirdparty)/UnixUtils.jar:$(thirdparty)/instrumentation.jar:$(thirdparty)/jacorb.jar

pkg=org.cougaar.lib.quo
pathdir=org/cougaar/lib/quo
srcdir=src/$(pathdir)

stub_file=$(srcdir)/MTInstrumentedInstrumentedServerDelegate_object_Stub.java
rmic_file=$(srcdir)/MTInstrumentedInstrumentedServerDelegate_object.java
rmic_class=$(pkg).MTInstrumentedInstrumentedServerDelegate_object

CLASSPATH=$(cougaar):$(qos):$(depends):$(classes)
export CLASSPATH


all: developers


# This should be what the standard COUGAAR build script does, more or less
compile: $(src)
	javac  -d $(classes) $(src)




# Developers do this

developers: gen $(jar)

gen:
	cd dev; make
	mkdir -p $(classes)


#$(stub_file): $(rmic_file)
#	javac  -d $(classes) $(rmic_file)
#	rmic $(rmic_class) -d $(classes) -keep
#	mv $(classes)/$(pathdir)/*.java $(srcdir)

$(jar): $(src) 
	javac  -d $(classes) $(src)
	rmic -d $(classes)  org.cougaar.lib.mquo.ZippyTestServerImpl
	jar cf $(jar) -C $(classes) .




clean:
	rm -rf $(jar) $(classes)
	cd dev; make clean


