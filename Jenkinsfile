pipeline {
    agent any

    stages {

        stage('Test') {
            steps {
                echo 'Phase 2.1: Lancement des tests...'
                bat 'gradlew.bat test'
                echo 'Archivage des résultats de tests...'
                junit '**/build/test-results/test/*.xml'
            }
        }

        stage('Code Analysis') {
            steps {
                echo 'Phase 2.2: Analyse SonarQube...'
                withSonarQubeEnv('sonar') {
                    bat 'gradlew.bat sonar'
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Phase 2.3: Vérification Quality Gate...'
                timeout(time: 1, unit: 'HOURS') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Phase 2.4: Génération du JAR et Javadoc...'
                bat 'gradlew.bat jar javadoc'
                archiveArtifacts artifacts: 'build/libs/*.jar, build/docs/javadoc/**', allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                echo 'Phase 2.5: Déploiement sur MyMavenRepo...'
                bat 'gradlew.bat publish'
            }
        }
    }

    post {

        success {
            mail to: 'ma_ghesmoune@esi.dz',
                 subject: "SUCCESS: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                 body: "Le pipeline s'est terminé avec succès. Le JAR est déployé."

             slackSend channel: '#test-canal',
                                  color: 'good',
                                  tokenCredentialId: 'slack-webhook', // L'ID que tu as créé à l'étape 2
                                  failOnError: false,
                                  message: "✅ Build SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                    }


        failure {
            mail to: 'ma_ghesmoune@esi.dz',
                 subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                 body: "Le build a échoué. Vérifiez les logs sur Jenkins."

            slackSend channel: '#test-canal',
                                 color: 'danger',
                                 tokenCredentialId: 'slack-webhook',
                                 failOnError: false,
                                 message: "❌ Build FAILED - ${env.JOB_NAME} #${env.BUILD_NUMBER}"
                   }
    }//
}
