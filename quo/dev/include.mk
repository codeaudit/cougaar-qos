# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

src=$(COUGAAR_SRC_PATH)/quo/src
root=$(COUGAAR_SRC_PATH)/quo/dev
qosketroot=$(QUO_ROOT)/qosket
newcore=$(COUGAAR_SRC_PATH)/core/src/org/cougaar/core/newcore.jar

# COUGAAR_INSTALL_PATH and $COUGAAR_SRC_PATH should be set in the
# $USER's environment

CLASSPATH = $(src):${newcore}:${COUGAAR_INSTALL_PATH}/lib/build.jar:${COUGAAR_INSTALL_PATH}/lib/glm.jar:${COUGAAR_INSTALL_PATH}/lib/planserver.jar:${QUO_ROOT}/lib/QuoKernel.jar:${QUO_ROOT}/lib/QuoInstr.jar:$(root):${QUO_ROOT}/lib/UnixUtils.jar:${qosketroot}/instrumentation/instrumentation.jar
export CLASSPATH

IDL2JAVA=${JACORB_DIR}/bin/idl
QUOGEN=${QUO_ROOT}/bin/quogen
IDLGEN=${QUO_ROOT}/bin/quoIdlgen

srcdir=org/cougaar/lib/quo
pkg=org.cougaar.lib.quo


msrcdir=org/cougaar/lib/mquo
mpkg=org.cougaar.lib.mquo



includes := -I$(QUO_ROOT)/idl
includes += -I$(root)
includes += -I$(qosketroot)

idl2pkg := -i2jpackage quo:com.bbn.quo.rmi
idl2pkg += -i2jpackage quo_data:com.bbn.quo.data
idl2pkg += -i2jpackage quo_corba:com.bbn.quo.corba
idl2pkg += -i2jpackage qosket:com.bbn.quo.qosket
idl2pkg += -i2jpackage instr:com.bbn.quo.instr.corba
idl2pkg += -i2jpackage instr_shared:com.bbn.quo.instr
idl2pkg += -i2jpackage metric:org.cougaar.core.qos.rss

















