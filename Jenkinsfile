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
                    // On récupère l'URL et on retire les espaces invisibles avec .trim()
                    // On utilise env.getProperty pour éviter les erreurs de sécurité Sandbox
                    def slackUrl = env.getProperty('slack-token')?.trim()

                    if (slackUrl) {
                        echo "Envoi de la notification Slack..."
                        // Note : On utilise ${slackUrl} sans guillemets autour car le trim() a tout nettoyé
                        bat """
                        curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"✅ SUCCESS: %JOB_NAME% #%BUILD_NUMBER% - Le JAR est déployé !\\\"}" ${slackUrl}
                        """
                    } else {
                        echo "ERREUR : La variable slack-token n'est pas définie dans Jenkins."
                    }
                }
            }
        }
    }

    post {
        failure {
            script {
                // Envoi de l'email
                mail to: 'ma_ghesmoune@esi.dz',
                     subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                     body: "Le build a échoué. Vérifiez les logs sur Jenkins : ${env.BUILD_URL}"

                // Envoi Slack en cas d'échec
                def slackUrl = env.getProperty('slack-token')?.trim()
                if (slackUrl) {
                    bat """
                    curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"❌ FAILED: %JOB_NAME% #%BUILD_NUMBER% - Vérifiez les logs.\\\"}" ${slackUrl}
                    """
                }
            }
        }
    }
}