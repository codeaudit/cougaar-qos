# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

buildsrc=$(QUO_ROOT)/examples/cougaar_8.4/src
root=$(QUO_ROOT)/examples/cougaar_8.4/dev
qosketroot=$(QUO_ROOT)/qosket
classes=$(root)/.classes

# COUGAAR_INSTALL_PATH and $COUGAAR_SRC_PATH should be set in the
# $USER's environment

newcore=${COUGAAR_SRC_PATH}/core/src/org/cougaar/core/newcore.jar
CLASSPATH = $(classes):${newcore}:${COUGAAR_INSTALL_PATH}/lib/build.jar:${COUGAAR_INSTALL_PATH}/lib/glm.jar:${COUGAAR_INSTALL_PATH}/lib/planserver.jar:${QUO_ROOT}/lib/QuoKernel.jar:${QUO_ROOT}/lib/QuoInstr.jar:$(root):${QUO_ROOT}/lib/UnixUtils.jar:${qosketroot}/instrumentation/instrumentation.jar:${jsse}/jsse.jar:${jsse}/jnet.jar:${jsse}/jcert.jar
export CLASSPATH

JAVAC=${QUO_ROOT}/bin/jacoc
IDL2JAVA=${JACORB_DIR}/bin/idl
RMIC=rmic
QUOGEN=${QUO_ROOT}/bin/quogen
IDLGEN=${QUO_ROOT}/bin/quoIdlgen

module=examples/cougaar_8.4
srcdir=org/cougaar/lib/quo
pkg=org.cougaar.lib.quo

rmi_classes := org.cougaar.lib.quo.MTInstrumentedInstrumentedServerDelegate_object

includes := -I$(QUO_ROOT)/idl
includes += -I$(root)
includes += -I$(qosketroot)

idl2pkg := -i2jpackage quo:com.bbn.quo.rmi
idl2pkg += -i2jpackage quo_corba:com.bbn.quo.corba
idl2pkg += -i2jpackage qosket:com.bbn.quo.qosket
idl2pkg += -i2jpackage instr:com.bbn.quo.instr.corba
idl2pkg += -i2jpackage instr_shared:com.bbn.quo.instr

















