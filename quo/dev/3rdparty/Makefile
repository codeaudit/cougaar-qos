# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

LINKS := QuoInstr.jar
LINKS += QuoKernel.jar
LINKS += QuoRSS.jar
LINKS += UnixUtils.jar
LINKS += NTUtils.jar
LINKS += event.jar
LINKS += instrumentation.jar
LINKS += jacorb.jar
LINKS += QuoIdlgen.jar
# LINKS  += quoSumo.jar


install: $(LINKS) 

QuoInstr.jar:
	ln -s ${QUO_ROOT}/lib/QuoInstr.jar .

QuoKernel.jar:
	ln -s ${QUO_ROOT}/lib/QuoKernel.jar .

QuoRSS.jar:
	ln -s ${QUO_ROOT}/lib/QuoRSS.jar .

UnixUtils.jar:
	ln -s ${QUO_ROOT}/lib/UnixUtils.jar .

NTUtils.jar:
	ln -s ${QUO_ROOT}/lib/NTUtils.jar .

event.jar:
	ln -s ${QUO_ROOT}/dirm/event/event.jar .

instrumentation.jar:
	ln -s ${QUO_ROOT}/qosket/instrumentation/instrumentation.jar .

QuoIdlgen.jar:
	ln -s ${QUO_ROOT}/lib/QuoIdlgen.jar .

jacorb.jar:
	ln -s ${JACORB_DIR}/lib/jacorb.jar .

quoSumo.jar:
	cd ${QUO_ROOT};make lib/quoSumo.jar
	ln -s ${QUO_ROOT}/lib/quoSumo.jar .

clean:
	rm -rf $(LINKS)

realclean:
	make clean
	cd ${QUO_ROOT}/lib; rm -rf quoSumo.jar






