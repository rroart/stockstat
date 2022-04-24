VERSION = 0.6-SNAPSHOT
SUBDIRS = tensorflowdata pytorchdata sparkdata tensorflow pytorch pdfred postgresql webr iwebr core icore iwebcore dbclone weba iweba rabbitmq sim evolve

all: $(SUBDIRS) skaffoldserver.yaml skaffoldml.yaml skaffoldfrontr.yaml skaffoldfronta.yaml skaffolddbclone.yaml conf

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

skaffolddbclone.yaml: skaffolddbclone.yaml.tmpl
	envsubst < skaffolddbclone.yaml.tmpl > skaffolddbclone.yaml

conf:
	$(MAKE) -C ../../cloud -B

.PHONY: all $(SUBDIRS) conf
