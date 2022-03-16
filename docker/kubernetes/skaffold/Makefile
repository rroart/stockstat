VERSION = 0.6-SNAPSHOT
SUBDIRS = tensorflowdata pytorchdata tensorflow pytorch pdfred postgresql webr iwebr core icore iwebcore dbclone weba iweba rabbitmq

all: $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ -f ../Makefile deployment.yaml
#%: %/deployment.yaml

%/deployment.yaml: %/deployment.yaml.tmpl
	envsubst < %/deployment.yaml.tmpl > %/deployment.yaml

deployment.yaml: deployment.yaml.tmpl
	envsubst < deployment.yaml.tmpl > deployment.yaml

.PHONY: all $(SUBDIRS)
