# =====================================================================
# (c) Copyright 2001  BBNT Solutions, LLC
# =====================================================================


# Zinky: DO NOT CHANGE THIS BACK THE WAY IT WAS
# The quo and qos Makefiles depend on finding an quoSumo

install: quoSumo.jar

quoSumo.jar:
	cd ${QUO_ROOT};make lib/quoSumo.jar
	ln -s ${QUO_ROOT}/lib/quoSumo.jar .

clean:
	rm -rf *.jar *~

realclean: clean







