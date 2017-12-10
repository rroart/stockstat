all: files
	docker build -t="tensorflow-predict" .

files: files/kemain.py

files/kemain.py:
	mkdir -p files
	install ../../../tensorflow/kemain.py files/
	install ../../../tensorflow/config2.yaml files/
