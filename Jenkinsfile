node {
  stage('SCM') {
    git 'https://github.com/rroart/stockstat.git'
  }
  stage('SonarQube analysis') {
    def scannerHome = tool 'sonarqube-scanner';
    withSonarQubeEnv('sonarqube-scanner') {
      sh "${scannerHome}/bin/sonar-scanner"
    }
  }
}
