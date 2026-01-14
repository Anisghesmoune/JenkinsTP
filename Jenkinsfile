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
        stage('Slack Notification') {
            steps {
                script {
                    echo "Envoi de la notification Slack..."

                    // Méthode 1 : Utilisation correcte de l'environnement variable
                    def slackUrl = env.'slack-token'

                    if (slackUrl) {
                        // Nettoyage de l'URL
                        slackUrl = slackUrl.trim()

                        // Création du message
                        def message = "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est deploye !"

                        // Création d'un fichier JSON temporaire pour éviter les problèmes d'échappement
                        def jsonPayload = """{"text": "${message}"}"""
                        writeFile file: 'slack-payload.json', text: jsonPayload

                        // Envoi via curl avec le fichier
                        bat "curl -X POST -H \"Content-type: application/json\" --data @slack-payload.json \"${slackUrl}\""

                        // Nettoyage
                        bat 'del slack-payload.json'
                    } else {
                        echo "ATTENTION : La variable d'environnement 'slack-token' n'est pas définie."
                        echo "Veuillez configurer cette variable dans Jenkins (Manage Jenkins > Configure System > Global properties)"
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                echo "Le build a échoué. Envoi des notifications..."

                // Notification Email
                try {
                    mail to: 'ma_ghesmoune@esi.dz',
                         subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                         body: """Le build a échoué.

Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Vérifiez les logs sur Jenkins pour plus de détails."""
                } catch (Exception e) {
                    echo "Erreur lors de l'envoi de l'email : ${e.message}"
                }

                // Notification Slack
                def slackUrl = env.'slack-token'
                if (slackUrl) {
                    slackUrl = slackUrl.trim()
                    def message = "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Verifiez les logs."
                    def jsonPayload = """{"text": "${message}"}"""
                    writeFile file: 'slack-payload-failure.json', text: jsonPayload

                    try {
                        bat "curl -X POST -H \"Content-type: application/json\" --data @slack-payload-failure.json \"${slackUrl}\""
                        bat 'del slack-payload-failure.json'
                    } catch (Exception e) {
                        echo "Erreur lors de l'envoi de la notification Slack : ${e.message}"
                    }
                }
            }
        }
        success {
            echo "Build réussi avec succès !"
        }
        always {
            echo "Nettoyage post-build..."
            // Ajoutez ici des tâches de nettoyage si nécessaire
        }
    }
}