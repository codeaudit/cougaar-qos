classes=.classes
jar=newquo.jar
cougaar=${COUGAAR_INSTALL_PATH}/lib/core.jar
src=$(shell find .  -name "*.java")
thirdparty=../dev/3rdparty
depends=$(thirdparty)/QuoKernel.jar:$(thirdparty)/QuoInstr.jar:$(thirdparty)/UnixUtils.jar:$(thirdparty)/instrumentation.jar:$(thirdparty)/jacorb.jar


CLASSPATH=$(classes):$(cougaar):$(depends)
export CLASSPATH


$(jar): $(src)
	install -d $(classes)
	javac  -d $(classes) $(src)
	jar cf $(jar) -C $(classes) .


clean:
	rm -rf $(jar) $(classes)




