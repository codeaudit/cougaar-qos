classes=.classes
jar=newquo.jar
#cougaar=${COUGAAR_INSTALL_PATH}/lib/core.jar
cougaar=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar
qos=${COUGAAR_SRC_PATH}/qos/src/org/cougaar/core/qos.jar
src=$(shell find .  -name "*.java")
thirdparty=../dev/3rdparty
depends=$(thirdparty)/QuoKernel.jar:$(thirdparty)/QuoInstr.jar:$(thirdparty)/UnixUtils.jar:$(thirdparty)/instrumentation.jar:$(thirdparty)/jacorb.jar

pkg=org.cougaar.lib.quo
srcdir=org/cougaar/lib/quo

rmic_class=$(pkg).MTInstrumentedInstrumentedServerDelegate_object
rmic_classfile=$(classes)/$(srcdir)/MTInstrumentedInstrumentedServerDelegate_object.class
rmic_javafile=$(srcdir)/MTInstrumentedInstrumentedServerDelegate_object.java

CLASSPATH=$(cougaar):${qos}:$(depends)
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


$(rmic_classfile): $(srcdir)/MTInstrumented.java
	rm -f $(rmic_classfile) $(rmic_javafile)
	rmic $(rmic_class) -keep -d $(classes)
	mv $(classes)/$(rmic_javafile) $(rmic_javafile)

$(jar): $(src) 
	javac  -d $(classes) $(src)
	make $(rmic_classfile)
	jar cf $(jar) -C $(classes) .




clean:
	rm -rf $(jar) $(classes)




