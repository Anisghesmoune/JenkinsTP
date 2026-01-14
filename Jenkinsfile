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

        stage('slack') {
            steps {
                script {
                    // Nettoyage de l'URL pour enlever l'espace invisible à la fin
                    def slackUrl = env.getProperty('slack-token')?.trim()

                    if (slackUrl) {
                        echo "Envoi de la notification Slack..."
                        // Note : Suppression des émojis pour éviter l'erreur d'encodage (ßÉº)
                        bat "curl -X POST -H \"Content-type: application/json\" --data \"{\\\"text\\\": \\\"SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est deploye !\\\"}\" ${slackUrl}"
                    } else {
                        echo "ERREUR : La variable slack-token est vide."
                    }
                }
            }
        }
    }

    post {
        failure {
            script {
                // Email
                mail to: 'ma_ghesmoune@esi.dz',
                     subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                     body: "Le build a échoué. Vérifiez les logs sur Jenkins."

                // Slack (Nettoyé et sans émojis)
                def slackUrl = env.getProperty('slack-token')?.trim()
                if (slackUrl) {
                    bat "curl -X POST -H \"Content-type: application/json\" --data \"{\\\"text\\\": \\\"FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Verifiez les logs.\\\"}\" ${slackUrl}"
                }
            }
        }
    }
}