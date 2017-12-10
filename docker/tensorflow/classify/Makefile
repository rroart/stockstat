all: files
	docker build -t="tensorflow-classify" .

files: files/tfmain.py

files/tfmain.py:
	mkdir -p files
	install ../../../tensorflow/tfmain.py files/
