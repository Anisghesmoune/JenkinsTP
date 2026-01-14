pipeline {
    agent any

    // On a supprimé le bloc environment vide qui causait l'erreur

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
                echo "Envoi de la notification Slack..."
                // Utilisation de guillemets autour de %slack-token% pour éviter l'erreur d'URL malformée
                // Tout sur une seule ligne pour éviter les espaces invisibles
                bat """
                curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"✅ SUCCESS: %JOB_NAME% #%BUILD_NUMBER% - Le JAR est déployé !\\\"}" "%slack-token%"
                """
            }
        }
    }

    post {
        failure {
            echo "Le pipeline a échoué. Envoi mail et Slack..."
            mail to: 'ma_ghesmoune@esi.dz',
                 subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                 body: "Le build a échoué. Vérifiez les logs sur Jenkins."

            bat """
            curl -X POST -H "Content-type: application/json" --data "{\\\"text\\\": \\\"❌ FAILED: %JOB_NAME% #%BUILD_NUMBER% - Vérifiez les logs.\\\"}" "%slack-token%"
            """
        }
    }
}