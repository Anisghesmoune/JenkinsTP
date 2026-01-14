pipeline {
    agent any

    environment {
        // Récupère la variable globale Jenkins.
        // Note : Si le nom contient un tiret, on utilise la syntaxe ['name']
        SLACK_URL = "${env['slack-token']}"
    }

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
            script {
                // Envoi Email
                mail to: 'ma_ghesmoune@esi.dz',
                     subject: "SUCCESS: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                     body: "Le pipeline s'est terminé avec succès. Le JAR est déployé."

                // Envoi Slack via Curl (Syntaxe Windows bat avec échappement des guillemets JSON)
                bat """
                    curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Le JAR est déployé !\\\"}" ${SLACK_URL}
                """
            }
        }

        failure {
            script {
                // Envoi Email
                mail to: 'ma_ghesmoune@esi.dz',
                     subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                     body: "Le build a échoué. Vérifiez les logs sur Jenkins : ${env.BUILD_URL}"

                // Envoi Slack Failure
                bat """
                    curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"❌ FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER} - Vérifiez les logs : ${env.BUILD_URL}\\\"}" ${SLACK_URL}
                """
            }
        }
    }
}