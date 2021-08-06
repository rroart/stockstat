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
                }
            }
            steps {
                sh 'mvn -Dmaven.test.failure.ignore=true install' 
            }
        }
    }
}