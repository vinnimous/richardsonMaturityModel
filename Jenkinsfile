pipeline {
    agent any
    stages {
	    stage('Build') { 
            steps {
            	sh 'mvn -B -DskipTests clean package' 
            }
        }
        stage('Test') {
            steps {
            	sh 'mvn test'
            }
        }
        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv(installationName: 'SonarQube', credentialsId: 'sonarqube') {
                    sh 'mvn clean package sonar:sonar'
                }
            }
        }
        stage("SonarQube quality gate") {
            steps {
                waitForQualityGate abortPipeline: true
            }
        }
    }
	post {
    	always {
        	junit(
		        allowEmptyResults: true,
		        testResults: '*/test-reports/.xml'
      		)
     	}
   } 
}