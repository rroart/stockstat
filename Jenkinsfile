node {
      checkout scm
      def dockerHome = tool 'Docker latest'
      env.PATH = "${dockerHome}/bin:${env.PATH}"
      def buildImage = docker.build("buildimage", "-f docker/jenkins/Dockerfile.build docker/jenkins") 
      buildImage.inside {
        env.npm_config_cache='/tmp/.npm'
        //sh 'mvn verify -pl !web'
        if (env.MYBRANCH == 'develop') {
          env.OTHERBRANCH = 'master'
        } else {
          env.OTHERBRANCH = 'develop'
        }
        sh 'git checkout $OTHERBRANCH'
	sh 'git config --list'
        sh 'git merge -v origin/$MYBRANCH'
        //sh 'mvn verify -pl !web'
        env.MYPUSH = sh(script: 'git config remote.origin.url | cut -c9-', returnStdout: true)
        withCredentials([usernameColonPassword(credentialsId: 'githubtoken', variable: 'TOKEN')]) {
          sh 'git push https://$TOKEN@$MYPUSH'
        }
        if (env.MYBRANCH != 'develop') {
          build(job: 'stockstatdev', wait: false, propagate: false)
        } else {
          build(job: 'stockstatsonarscanner', wait: false, propagate: false)
        }
      }
  }
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
                }
            }
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install' 
            }
        }
    }
}
*/