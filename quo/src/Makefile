classes=.classes
jar=newquo.jar
#cougaar=${COUGAAR_INSTALL_PATH}/lib/core.jar
cougaar=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar
qos=${COUGAAR_SRC_PATH}/qos/src/org/cougaar/core/qos.jar
src=$(shell find .  -name "*.java")
thirdparty=../dev/3rdparty
depends=$(thirdparty)/QuoKernel.jar:$(thirdparty)/QuoRSS.jar:$(thirdparty)/QuoInstr.jar:$(thirdparty)/UnixUtils.jar:$(thirdparty)/instrumentation.jar:$(thirdparty)/jacorb.jar

pkg=org.cougaar.lib.quo
srcdir=org/cougaar/lib/quo
msrcdir=org/cougaar/lib/mquo

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
	cd ../dev; make
	mkdir -p $(classes)


$(jar): $(src) 
	javac  -d $(classes) $(src)
	rmic $(rmic_class) -d $(classes)
	jar cf $(jar) -C $(classes) .




clean:
	rm -rf $(jar) $(classes)
	cd ../dev; make clean


