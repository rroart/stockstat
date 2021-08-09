node {
      checkout scm
      def dockerHome = tool 'Docker latest'
      env.PATH = "${dockerHome}/bin:${env.PATH}"
      def buildImage = docker.build("buildimage", "-f docker/jenkins/Dockerfile.build docker/jenkins") 
      buildImage.inside {
        sh 'ls -al / /.npm'
	sh 'id'
	sh 'touch /xyz'
        sh 'sudo ls'
        sh 'mvn verify -pl !web'
        if (env.MYBRANCH == 'develop') {
          env.OTHERBRANCH = 'master'
        } else {
          env.OTHERBRANCH = 'develop'
        }
        sh 'git checkout $OTHERBRANCH'
        sh 'git merge origin/$MYBRANCH'
        sh 'mvn verify -pl !web'
        env.MYPUSH = sh(script: 'git config remote.origin.url | cut -c9-', returnStdout: true)
        withCredentials([usernameColonPassword(credentialsId: 'githubtoken', variable: 'TOKEN')]) {
          sh 'git push https://$TOKEN@$MYPUSH'
        }
        if (env.MYBRANCH != 'develop') {
          build 'stockstatdev'
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