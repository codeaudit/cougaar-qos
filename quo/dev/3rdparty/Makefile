# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

LINKS := QuoInstr.jar
LINKS += QuoKernel.jar
LINKS += UnixUtils.jar
LINKS += event.jar
LINKS += instrumentation.jar
#LINKS += quo_remos.jar
#LINKS += remos.jar
LINKS += jacorb.jar

JAR = quoSumo.jar
LINKS += $(JAR)

VERSION=version.txt

install: $(LINKS)

QuoInstr.jar:
	ln -s ${QUO_ROOT}/lib/QuoInstr.jar .

QuoKernel.jar:
	ln -s ${QUO_ROOT}/lib/QuoKernel.jar .

UnixUtils.jar:
	ln -s ${QUO_ROOT}/lib/UnixUtils.jar .

event.jar:
	ln -s ${QUO_ROOT}/dirm/event/event.jar .

instrumentation.jar:
	ln -s ${QUO_ROOT}/qosket/instrumentation/instrumentation.jar .

#quo_remos.jar:
#ln -s ${QUO_ROOT}/remos/quo_remos.jar .

#remos.jar:
#ln -s ${REMOS_LIB}/remos.jar .

jacorb.jar:
	ln -s ${JACORB_DIR}/lib/jacorb.jar .

perpVersion:
	version.sh

quoSumo.jar:
	jar -cvf $(JAR) $(VERSION) 

clean:
	rm -f $(LINKS) $(JAR) $(VERSION)


