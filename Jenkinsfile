pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile.build'
            dir 'docker/jenkins'
        }
    }
    tools {
        maven 'Maven 3.6.0'
        //jdk 'jdk8'
    }
    stages {
        stage ('Initialize') {
            steps {
                def dockerHome = tool 'Docker latest'
                env.PATH = "${dockerHome}/bin:${env.PATH}"
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('Build') {
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