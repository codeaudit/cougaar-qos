core=${COUGAAR_SRC_PATH}/core/newcore.jar:${COUGAAR_SRC_PATH}/mtsstd/newmts.jar:${COUGAAR_INSTALL_PATH}/lib/sys/servlet.jar:${COUGAAR_INSTALL_PATH}/lib/util.jar
rss=${COUGAAR_SRC_PATH}/quo/dev/3rdparty/quoSumo.jar

CLASSPATH=$(core):$(rss)
export CLASSPATH

src=$(shell find src -name '*.java')

jar=newqos.jar

all: $(jar)



$(jar):  $(src) 
	javac -d . $(src) 
	jar cf $(jar) org


clean:
	rm -rf $(jar) org 

