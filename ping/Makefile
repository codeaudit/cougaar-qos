core=${COUGAAR_SRC_PATH}/core/newcore.jar:${COUGAAR_INSTALL_PATH}/lib/sys/servlet.jar:${COUGAAR_SRC_PATH}/util/newutil.jar

classes=.classes
CLASSPATH=$(core):$(classes)
export CLASSPATH

src=$(shell find src -name '*.java')

jar=newping.jar

all: $(jar)



$(jar): $(classes) $(src) 
	javac -d $(classes) $(src) 
	jar cf $(jar) $(classes)

$(classes):
	mkdir -p $(classes)

clean:
	rm -rf $(jar) $(classes)

