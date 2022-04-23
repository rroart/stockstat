all: files
	docker build -t="tensorflow" .

files:
	mkdir -p files
	install ../../tensorflow/*.py files/
	install ../../tensorflow/flasktf.sh files/
