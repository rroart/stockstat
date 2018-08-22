all: files
#	docker build -t="pd" .
	echo done

files: 
	cd .
	mkdir -p files; cd .
	ln -f ../../python/pd/*.py files
	ln -f ../../input/target/stockstat-input-0.5-SNAPSHOT-jar-with-dependencies.jar files
	docker build -t="pd" .
#	ln -f ../../python/pd/*.py files

.PHONY: all $(SUBDIRS)
