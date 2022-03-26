VERSION = 0.6-SNAPSHOT
SUBDIRS = tensorflowdata pytorchdata tensorflow pytorch pdfred postgresql webr iwebr core icore iwebcore dbclone weba iweba rabbitmq sim evolve

all: $(SUBDIRS) skaffoldserver.yaml skaffoldml.yaml skaffoldfrontr.yaml skaffoldfronta.yaml

$(SUBDIRS):
	$(MAKE) -C $@ -f ../Makefile deployment.yaml
#%: %/deployment.yaml

%/deployment.yaml: %/deployment.yaml.tmpl
	envsubst < %/deployment.yaml.tmpl > %/deployment.yaml

deployment.yaml: deployment.yaml.tmpl
	envsubst < deployment.yaml.tmpl > deployment.yaml

skaffoldserver.yaml: skaffoldserver.yaml.tmpl
	envsubst < skaffoldserver.yaml.tmpl > skaffoldserver.yaml

skaffoldml.yaml: skaffoldml.yaml.tmpl
	envsubst < skaffoldml.yaml.tmpl > skaffoldml.yaml

skaffoldfrontr.yaml: skaffoldfrontr.yaml.tmpl
	envsubst < skaffoldfrontr.yaml.tmpl > skaffoldfrontr.yaml

skaffoldfronta.yaml: skaffoldfronta.yaml.tmpl
	envsubst < skaffoldfronta.yaml.tmpl > skaffoldfronta.yaml

.PHONY: all $(SUBDIRS)
