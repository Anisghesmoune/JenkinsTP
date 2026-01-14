pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                // 1. Run unit tests
                bat './gradlew test'
                // 2. Archive unit test results
                junit '**/build/test-results/test/*.xml'
                // 3. Generate Cucumber reports (requires plugin setup)
            }
        }

        stage('Code Analysis') {
            steps {
                // Run SonarQube analysis
                withSonarQubeEnv('My SonarQube Server') {
                    sh './gradlew sonarqube'
                }
            }
        }

        stage('Code Quality') {
            steps {
                // Check Quality Gate
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                // 1. Generate Jar and 2. Generate Documentation
                sh './gradlew jar javadoc'
                // 3. Archive Jar and Docs
                archiveArtifacts artifacts: 'build/libs/*.jar, build/docs/javadoc/**', allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                // Deploy to mymavenrepo.com
                sh './gradlew publish'
            }
        }
    }

    post {
        success {
            // Notification: Email and Slack on success
//             slackSend color: 'good', message: "Build Successful: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
            mail to: 'team@example.com', subject: "Success: ${env.JOB_NAME}", body: "Build finished successfully."
        }
        failure {
            // Notification on failure
//             slackSend color: 'danger', message: "Build Failed: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
        }
    }
}