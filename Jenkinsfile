podTemplate(label: 'dind', containers: [
  containerTemplate(name: 'docker', image: 'docker:dind', ttyEnabled: true, privileged: true,
    command: 'dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=overlay')
  ],
  volumes: [emptyDirVolume(memory: false, mountPath: '/var/lib/docker')]) {
     node {
    container('docker') {
     checkout scm
     def dockerHome = tool 'Docker latest'
     env.PATH = "${dockerHome}/bin:${env.PATH}"
     def buildImage = docker.build("buildimage", "-f docker/jenkins/Dockerfile.build docker/jenkins") 
        buildImage.inside {
         sh 'mvn -Dmaven.test.failure.ignore=true install'
       }	
     }
     }
  }

/*
node {
  checkout scm
  def dockerHome = tool 'Docker latest'
  env.PATH = "${dockerHome}/bin:${env.PATH}"
  env.DOCKER_HOST = "tcp://192.168.39.74:2376"
  docker.withServer('tcp://192.168.39.74:2376') {
    def buildImage = docker.build("buildimage", "-f docker/jenkins/Dockerfile.build docker/jenkins") 
      buildImage.inside {
        sh 'mvn -Dmaven.test.failure.ignore=true install'
      }	
    }
  }
  */
/*
pipeline {
    agent any
    stages {
        stage ('Initialize') {
            steps {
                script {
                    def dockerHome = tool 'Docker latest'
                    env.PATH = "${dockerHome}/bin:${env.PATH}"
                }
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }
        stage ('Build') {
            agent {
                dockerfile {
                    filename 'Dockerfile.build'
                    dir 'docker/jenkins'
                    reuseNode true
                    args '-H tcp://192.168.39.74:2376'
                }
            }
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install' 
            }
        }
    }
}
*/