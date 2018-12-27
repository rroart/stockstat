#!/bin/bash

RUN=`minikube status|grep host:|grep Run`

[ -z "$RUN" ] && minikube start

#kubectl config use-context minikube

NAMESPACE=`kubectl get namespace $1 | grep not.found`

[ -n "$NAMESPACE" ] && kubectl create namespace $1

. bin/kubeenv.sh

kubectl config set-context minikube --namespace=$1

cd distribution/target/stockstat-distribution-0.5-SNAPSHOT-bin/stockstat-distribution-0.5-SNAPSHOT/docker

make

kubectl get deployment postgresql-10-centos7 && kubectl delete deployment postgresql-10-centos7
kubectl get deployment pd && kubectl delete deployment pd
kubectl get deployment stockstat-eureka && kubectl delete deployment stockstat-eureka
kubectl get deployment stockstat-core && kubectl delete deployment stockstat-core 
kubectl get deployment stockstat-web && kubectl delete deployment stockstat-web
kubectl get deployment stockstat-iclij-core && kubectl delete deployment stockstat-iclij-core
kubectl get deployment stockstat-iclij-web && kubectl delete deployment stockstat-iclij-web
kubectl get deployment tensorflow && kubectl delete deployment tensorflow
kubectl get deployment spark && kubectl delete deployment spark

kubectl get service postgresql-10-centos7 && kubectl delete service postgresql-10-centos7
kubectl get service stockstat-eureka && kubectl delete service stockstat-eureka
kubectl get service stockstat-web && kubectl delete service stockstat-web
kubectl get service stockstat-iclij-web && kubectl delete service stockstat-iclij-web
kubectl get service spark && kubectl delete service spark
kubectl get service tensorflow && kubectl delete service tensorflow

kubectl run --image=centos/postgresql-10-centos7 --env="POSTGRESQL_USER=stockstat" --env="POSTGRESQL_PASSWORD=password" --env="POSTGRESQL_DATABASE=stockstat" --port=5432 --expose=true postgresql-10-centos7
kubectl run --image=pd --image-pull-policy=Never pd
kubectl run --image=stockstat-eureka --image-pull-policy=Never stockstat-eureka
kubectl run --image=stockstat-core --image-pull-policy=Never stockstat-core
kubectl run --image=stockstat-web --image-pull-policy=Never stockstat-web
kubectl run --image=stockstat-iclij-core --image-pull-policy=Never stockstat-iclij-core
kubectl run --image=stockstat-iclij-web --image-pull-policy=Never stockstat-iclij-web
kubectl run --image=tensorflow --image-pull-policy=Never tensorflow
kubectl run --image=spark --image-pull-policy=Never spark

kubectl expose deployment stockstat-eureka --port 8761 --type=LoadBalancer
kubectl expose deployment stockstat-web --port 8180 --type=LoadBalancer
kubectl expose deployment stockstat-iclij-web --port 8181 --type=LoadBalancer
kubectl expose deployment spark --port 7077 --type=LoadBalancer
#kubectl expose deployment sparkui --port 8080 --type=LoadBalancer
kubectl expose deployment tensorflow --port 8000 --type=LoadBalancer

docker image prune -f
