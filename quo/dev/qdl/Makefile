# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================

# define CLASSPATH, IDLGEN, QUOGEN, SRCDIR, INCLUDES, IDL2JAVA
include ../include.mk

tag=.tag

super=$(qosketroot)/instrumentation

traffic_idl=Traffic.idl
traffic_files=$(src)/$(srcdir)/TrafficMaskControl.java
traffic_flags := -rmi
traffic_flags += -ifdef TRAFFIC_IDL
traffic_flags += $(idl2pkg)
traffic_flags += -idl $(traffic_idl)
traffic_flags += -include "quo.idl"

dl_idl=DestinationLink.idl
dl_files=$(COUGAAR_SRC_PATH)/core/src/org/cougaar/core/mts/DestinationLink.java
dl_flags := -rmi
dl_flags += -ifdef COUGAAR_DL
dl_flags += $(idl2pkg)
dl_flags += -idl $(dl_idl)
dl_flags += -interface org.cougaar.core.society.Message
dl_flags += -interface org.cougaar.core.society.MessageAddress
dl_flags += -interface java.lang.Class

mt_idl=MT.idl

mt_files=$(COUGAAR_SRC_PATH)/core/src/org/cougaar/core/mts/MT.java

mt_flags := -rmi
mt_flags += -ifdef COUGAAR_MT
mt_flags += $(idl2pkg)
mt_flags += -idl $(mt_idl)
mt_flags += -interface org.cougaar.core.society.Message
mt_flags += -interface org.cougaar.core.society.MessageAddress



mt_inst_idl=MTInstrumented.idl

mt_inst_files=$(src)/$(srcdir)/MTInstrumented.java
#mt_inst_files=$(COUGAAR_SRC_PATH)/$(module)/$(srcdir)/MTInstrumented.java

mt_inst_flags := -rmi
mt_inst_flags += -ifdef COUGAAR_MT_INST
mt_inst_flags += $(idl2pkg)
mt_inst_flags += -include "quo.idl"
mt_inst_flags += -include "quo_instr.idl"
mt_inst_flags += -idl $(mt_inst_idl)
mt_inst_flags += -interface org.cougaar.core.society.Message
mt_inst_flags += -interface org.cougaar.core.society.MessageAddress
mt_inst_flags += -interface org.cougaar.lib.quo.Zippy


qdl_flags := --debuglevel=warnings
qdl_flags += --language=java
qdl_flags += --package=org.cougaar.lib.quo
qdl_flags += --prefixdir=$(src)
qdl_flags += $(includes)
qdl_flags += $(idl2pkg)

rmi_qdl_flags := $(qdl_flags)
rmi_qdl_flags += --middleware=rmi

aspect_qdl_flags := $(qdl_flags)
aspect_qdl_flags += --middleware=none
aspect_qdl_flags += --kernel_middleware=rmi


all: build

build: $(tag)


$(mt_idl): $(mt_files)
	$(IDLGEN) $(mt_flags) $(mt_files)

$(mt_inst_idl): $(mt_inst_files)
	$(IDLGEN) $(mt_inst_flags) $(mt_inst_files)

$(dl_idl): $(dl_files)
	$(IDLGEN) $(dl_flags) $(dl_files)

$(traffic_idl): $(traffic_files)
	$(IDLGEN) $(traffic_flags) $(traffic_files)


# arbitrary generated files, one per csl
compress=$(src)/$(srcdir)/Compress.java
diagnose=$(src)/$(srcdir)/Diagnose.java
logging=$(src)/$(srcdir)/Logging.java
instserver=$(src)/$(srcdir)/MTInstrumentedServerAdapter.java
traffic=$(src)/$(srcdir)/TrafficMask.java
remotessl=$(src)/$(srcdir)/RemoteSSL.java

# Qosket and contract
$(traffic): RemoteSSL.idl TrafficMask.cdl $(traffic_idl) $(dl_idl)
	$(QUOGEN) \
	--target=client \
	--cdl=TrafficMask.cdl \
	--idl=$(traffic_idl) \
	--idl=RemoteSSL.idl \
	--idl=quo.idl \
	--adapterqosket=org::cougaar::lib::quo::TrafficMaskQosket \
	$(rmi_qdl_flags)


# Qosket and contract
$(remotessl): RemoteSSL.idl RemoteSSL.cdl $(mt_idl) $(dl_idl) RemoteSSL.asl
	$(QUOGEN) \
	--generate=both \
	--target=client \
	--cdl=RemoteSSL.cdl \
	--idl=$(mt_idl) \
	--idl=$(dl_idl) \
	--idl=RemoteSSL.idl \
	--idl=quo.idl \
	--adapterqosket=org::cougaar::lib::quo::RemoteSSLQosket \
	--delegateqosket=org::cougaar::lib::quo::RemoteSSLDelegateQosket \
	--business=org::cougaar::core::mts::DestinationLink \
	--qosketimpl=org.cougaar.lib.quo.RemoteSSLQosketImpl \
	--adapter=DestinationLinkRemoteSSLAdapter \
	--asl=RemoteSSL.asl \
	$(aspect_qdl_flags)


$(diagnose): $(mt_idl) $(mt_inst_idl)  Qosket.idl Diagnose.asl
	$(QUOGEN)   \
        --generate=both \
	--target=client \
	--cdl=Diagnose.cdl \
	--idl=$(mt_idl) \
	--idl=Qosket.idl \
	--idl=quo_corba.idl \
	--business=org::cougaar::core::mts::MT \
	--adapter=MTDiagnoseAdapter \
	--qosketimpl=org.cougaar.lib.quo.DiagnoseQosketImpl \
	--adapterqosket=org::cougaar::lib::quo::DiagnoseQosket \
	--delegateqosket=org::cougaar::lib::quo::DiagnoseDelegateQosket \
	--asl=$(qosketroot)/instrumentation/qdl/InstrumentationTemplate.asl \
	--asl=DiagnoseInst.asl \
	--asl=Diagnose.asl \
	--properties=$(super)/qdl/InstrumentationDerivedRmi.prop \
	${rmi_qdl_flags}

$(compress): $(mt_idl) $(mt_inst_idl)  Qosket.idl Compress.asl Compress.cdl
	$(QUOGEN)  \
        --generate=both \
	--target=client \
	--cdl=Compress.cdl \
	--idl=Qosket.idl \
	--idl=$(mt_idl) \
	--business=org::cougaar::core::mts::MT \
	--adapter=MTCompressAdapter \
	--qosketimpl=org.cougaar.lib.quo.CompressQosketImpl \
	--adapterqosket=org::cougaar::lib::quo::CompressQosket \
	--delegateqosket=org::cougaar::lib::quo::CompressDelegateQosket \
	--asl=Compress.asl \
	${rmi_qdl_flags}


$(logging):$(mt_idl) $(mt_inst_idl)  Qosket.idl Logging.asl 
	$(QUOGEN)   \
        --generate=both \
	--target=client \
	--cdl=Logging.cdl \
	--idl=Qosket.idl \
	--idl=$(mt_idl) \
	--business=org::cougaar::core::mts::MT \
	--adapter=MTLoggingAdapter \
	--qosketimpl=org.cougaar.lib.quo.LoggingQosketImpl \
	--adapterqosket=org::cougaar::lib::quo::LoggingQosket \
	--asl=Logging.asl \
	${rmi_qdl_flags}



$(instserver): $(mt_idl) $(mt_inst_idl) Qosket.idl InstrumentedServer.asl Server.asl
	$(QUOGEN)  \
	--target=server \
	--cdl=$(qosketroot)/instrumentation/qdl/SimpleInstrumentedServer.cdl \
	--idl=$(qosketroot)/instrumentation/qdl/Instrumentation.idl \
	--idl=$(mt_inst_idl) \
	--idl=quo_corba.idl \
	--business=org::cougaar::lib::quo::MTInstrumented \
	--adapter=MTInstrumentedServerAdapter \
	--adapterqosket=qosket::instrumentation::ServerInstrumentationQosket \
	--delegateqosket=qosket::instrumentation::ServerInstrumentationDelegateQosket \
	--properties=$(qosketroot)/instrumentation/qdl/ServerInstrumentationRmi.prop \
	--asl=$(qosketroot)/instrumentation/qdl/SimpleInstrumentedServerTemplate.asl \
	--asl=InstrumentedServer.asl \
	--asl=Server.asl \
	${rmi_qdl_flags}



$(tag): $(traffic) $(logging) $(diagnose)  $(compress) $(instserver) $(remotessl)
	touch $(tag)

clean:
	rm -rf $(tag)  $(mt_idl) $(mt_inst_idl)

realclean: clean











