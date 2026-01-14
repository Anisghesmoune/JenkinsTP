pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                echo 'Phase 2.1: Lancement des tests...'
                bat 'gradlew.bat test'
                echo 'Archivage des résultats de tests...'
                junit '**/build/test-results/test/*.xml'
                  cucumber buildStatus: 'UNSTABLE',
                                reportTitle: 'My report',
                                fileIncludePattern: 'reports/example-report.json',
                                trendsLimit: 10
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

                    def slackUrl = env.'slack-token'

                    if (slackUrl) {
                        slackUrl = slackUrl.trim()

                        def message = "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est deploye !"

                        // Création du fichier JSON
                        def jsonContent = """{"text": "${message}"}"""
                        writeFile file: 'slack-payload.json', text: jsonContent, encoding: 'UTF-8'

                        // Utilisation de PowerShell pour l'envoi (plus fiable que cmd)
                        powershell """
                            \$webhook = '${slackUrl}'
                            \$json = Get-Content -Path 'slack-payload.json' -Raw
                            Invoke-RestMethod -Uri \$webhook -Method Post -Body \$json -ContentType 'application/json; charset=utf-8'
                        """

                        // Nettoyage
                        bat 'if exist slack-payload.json del slack-payload.json'
                    } else {
                        echo "ATTENTION : La variable 'slack-token' n'est pas definie."
                    }
                }
            }
        }
    }
    post {
        failure {
            script {
                echo "Le build a echoue. Envoi des notifications..."

                // Notification Email
                try {
                    mail to: 'ma_ghesmoune@esi.dz',
                         subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                         body: """Le build a echoue.

Job: ${env.JOB_NAME}
Build Number: ${env.BUILD_NUMBER}
Build URL: ${env.BUILD_URL}

Verifiez les logs sur Jenkins pour plus de details."""
                } catch (Exception e) {
                    echo "Erreur lors de l'envoi de l'email : ${e.message}"
                }

                // Notification Slack
                def slackUrl = env.'slack-token'
                if (slackUrl) {
                    slackUrl = slackUrl.trim()
                    def message = "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Verifiez les logs."
                    def jsonContent = """{"text": "${message}"}"""
                    writeFile file: 'slack-payload-failure.json', text: jsonContent, encoding: 'UTF-8'

                    try {
                        powershell """
                            \$webhook = '${slackUrl}'
                            \$json = Get-Content -Path 'slack-payload-failure.json' -Raw
                            Invoke-RestMethod -Uri \$webhook -Method Post -Body \$json -ContentType 'application/json; charset=utf-8'
                        """
                        bat 'if exist slack-payload-failure.json del slack-payload-failure.json'
                    } catch (Exception e) {
                        echo "Erreur lors de l'envoi de la notification Slack : ${e.message}"
                    }
                }
            }
        }
        success {
            echo "Build reussi avec succes !"
        }
        always {
            echo "Nettoyage post-build..."
            // Nettoyage des fichiers temporaires
            bat '''
                if exist slack-payload.json del slack-payload.json
                if exist slack-payload-failure.json del slack-payload-failure.json
            '''
        }
    }
}