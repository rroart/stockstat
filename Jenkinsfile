pipeline {
    agent any
    tools {
        maven 'Maven 3.6.0'
        dockerTool 'Docker latest'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    dockerHome = tool 'Docker latest'
                    PATH = "${dockerHome}/bin:${PATH}"
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
            post {
                success {
                    junit 'target/surefire-reports/**/*.xml' 
                }
            }
        }
    }
}