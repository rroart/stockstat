pipeline {
    agent none

    stages {
        stage('Initialize') {
            agent any
            steps {
                script {
                    // Ensure Docker tool is available in PATH
                    env.DOCKER_HOME = tool 'Docker latest'
                    env.PATH = "${env.DOCKER_HOME}/bin:${env.PATH}"
                }
                // Checkout repository early so subsequent stages can use workspace
                checkout scm

                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage('Build') {
            // Build inside a docker image built from docker/jenkins/Dockerfile.build
            agent {
                dockerfile {
                    filename 'Dockerfile.build'
                    dir 'docker/jenkins'
                    reuseNode true
                }
            }
            steps {
                script {
                    // Notify start
                    if (binding.hasVariable('mattermostSend')) {
                        mattermostSend "Build Started - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.MYBRANCH}"
                    }
                    env.npm_config_cache = '/tmp/.npm'
                }

                // Primary build
                sh 'mvn clean verify -pl !webr -pl !weba -Dcyclonedx.skip'

                script {
                    if (binding.hasVariable('mattermostSend')) {
                        mattermostSend "Build Finished - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.MYBRANCH}"
                    }

                    // Determine other branch
                    if (env.MYBRANCH == 'develop') {
                        env.OTHERBRANCH = 'master'
                    } else {
                        env.OTHERBRANCH = 'develop'
                    }
                }

                // Merge workflow
                sh 'git checkout $OTHERBRANCH'
                sh 'git pull'
                sh 'git merge origin/$MYBRANCH || (git merge --abort && exit 1)'

                script {
                    if (binding.hasVariable('mattermostSend')) {
                        mattermostSend "Merged ${env.MYBRANCH} to ${env.OTHERBRANCH}"
                    }
                }

                // Build again after merge
                sh 'mvn clean verify -pl !webr -pl !weba -Dcyclonedx.skip'

                script {
                    if (binding.hasVariable('mattermostSend')) {
                        mattermostSend "Build Finished - ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.OTHERBRANCH}"
                    }
                }

                // Compute push URL and push using credentials
                script {
                    env.MYPUSH = sh(script: 'git config remote.origin.url | cut -c9-', returnStdout: true).trim()
                }

                withCredentials([usernameColonPassword(credentialsId: 'githubtoken', variable: 'TOKEN')]) {
                    sh 'git push https://$TOKEN@$MYPUSH'
                }

                // Trigger downstream job based on branch
                script {
                    if (env.MYBRANCH != 'develop') {
                        build(job: 'stockstatdev', wait: false, propagate: false)
                    } else {
                        // previously triggered external 'stockstatsonarscanner' job here;
                        // Sonar scan is now inlined as its own stage (see 'Sonar Scan' stage below)
                        echo 'Skipping external stockstatsonarscanner trigger; sonar will run in-pipeline.'
                    }
                }
            }
        }

        // Inline the former stockstatsonarscanner job as a declarative stage
        stage('Sonar Scan') {
            agent {
                dockerfile {
                    filename 'Dockerfile.build'
                    dir 'docker/jenkins'
                    reuseNode true
                }
            }
            when {
                expression { return env.MYBRANCH == 'develop' }
            }
            steps {
                script {
                    // replicate Jenkinsfile.sonar behavior
                    def scannerHome = tool 'sonarqube-scanner'
                    env.PATH = "${scannerHome}/bin:${env.PATH}"
                    env.GIT_DEPTH = 0
                    sh 'ls -R ${scannerHome}'
                    sh 'mvn clean verify -pl !webr -pl !weba -Dcyclonedx.skip'
                    sh 'sonar-scanner'
                }
            }
        }
    }

    // Keep the workspace and timestamps for better logs when running in Jenkins
    //options {
        // preserveStashes(buildsToKeepStr: '1')
        // TODO timestamps()
    //}
}
