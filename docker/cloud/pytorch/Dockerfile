FROM debian:buster
MAINTAINER <roart@nvg.ntnu.no>

ARG PYTHON_VERSION=3.7
ARG WITH_TORCHVISION=0
ARG myport
ENV MYPORT ${myport}

RUN apt-get update && apt-get install -y --no-install-recommends build-essential cmake git curl ca-certificates libjpeg-dev libpng-dev && rm -rf /var/lib/apt/lists/*
RUN curl -o ~/miniconda.sh -LO  https://repo.continuum.io/miniconda/Miniconda3-latest-Linux-x86_64.sh && \
     chmod +x ~/miniconda.sh && \
     ~/miniconda.sh -b -p /opt/conda && \
     rm ~/miniconda.sh && \
     /opt/conda/bin/conda install -y python=$PYTHON_VERSION numpy pyyaml scipy ipython mkl mkl-include ninja cython typing && \
     /opt/conda/bin/conda clean -ya
ENV PATH /opt/conda/bin:$PATH
RUN conda create --name gem
RUN conda install pytorch torchvision cpuonly -c pytorch
# This must be done before pip so that requirements.txt is available
WORKDIR /opt/pytorch
# COPY . .

#RUN git submodule update --init --recursive
#RUN TORCH_NVCC_FLAGS="-Xfatbin -compress-all" \
#    CMAKE_PREFIX_PATH="$(dirname $(which conda))/../" \
#    pip install -v .

#RUN if [ "$WITH_TORCHVISION" = "1" ] ; then git clone https://github.com/pytorch/vision.git && cd vision && pip install -v . ; else echo "building without torchvision" ; fi

WORKDIR /workspace
RUN chmod -R a+w .

RUN pip install flask

ADD pytorch/ /usr/local/pytorch/

WORKDIR /usr/local/pytorch

CMD ./flaskpt.sh ${MYPORT}