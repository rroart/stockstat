node {
      checkout scm
      def dockerHome = tool 'Docker latest'
      env.PATH = "${dockerHome}/bin:${env.PATH}"
      def buildImage = docker.build("buildimage", "-f docker/jenkins/Dockerfile.build docker/jenkins") 
      buildImage.inside {
        mattermostSend "Build Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.MYBRANCH}"
        env.npm_config_cache='/tmp/.npm'
        sh 'mvn verify -pl !webr -pl !weba -Dcyclonedx.skip'
        mattermostSend "Build Finished - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.MYBRANCH}"
        if (env.MYBRANCH == 'develop') {
          env.OTHERBRANCH = 'master'
        } else {
          env.OTHERBRANCH = 'develop'
        }
        sh 'git checkout $OTHERBRANCH'
	sh 'git pull'
        sh 'git merge origin/$MYBRANCH || (git merge --abort && exit 1)'
        mattermostSend "Merged ${env.MYBRANCH} to ${env.OTHERBRANCH}"
        sh 'mvn verify -pl !webr -pl !weba -Dcyclonedx.skip'
        mattermostSend "Build Finished - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.OTHERBRANCH}"
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
                sh 'mvn -Dmaven.test.failure.ignore=true install -DskipTests -Dcyclonedx.skip' 
            }
        }
    }
}
*/
