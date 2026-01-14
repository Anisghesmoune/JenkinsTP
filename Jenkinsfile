pipeline {
    agent any

    stages {
        stage('Test') {
            steps {
                echo 'Phase 2.1: Lancement des tests...'
                // Utilisation de bat et gradlew.bat pour Windows
                bat 'gradlew.bat test'

                echo 'Archivage des résultats de tests...'
                junit '**/build/test-results/test/*.xml'
            }
        }

        stage('Code Analysis') {
            steps {
                echo 'Phase 2.2: Analyse SonarQube...'
                // 'sonar' doit être le nom configuré dans Administrer Jenkins -> System
                withSonarQubeEnv('sonar') {
                    bat 'gradlew.bat sonar'
                }
            }
        }

        stage('Code Quality') {
            steps {
                echo 'Phase 2.3: Vérification Quality Gate...'
                timeout(time: 1, unit: 'HOURS') {
                    // Attend que SonarQube renvoie le statut (nécessite le Webhook Sonar)
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Build') {
            steps {
                echo 'Phase 2.4: Génération du JAR et Javadoc...'
                bat 'gradlew.bat jar javadoc'

                echo 'Archivage des artefacts...'
                archiveArtifacts artifacts: 'build/libs/*.jar, build/docs/javadoc/**', allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                echo 'Phase 2.5: Déploiement sur MyMavenRepo...'
                // Utilise les variables MAVEN_USER/PASSWORD configurées dans Jenkins
                bat 'gradlew.bat publish'
            }
        }
    }

    post {
        success {
            echo 'Phase 2.6: Notification de succès...'
            mail to: 'h_mokeddem@esi.dz',
                 subject: "SUCCESS: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                 body: "Le pipeline s'est terminé avec succès. Le JAR est déployé."
        }
        failure {
            echo 'Phase 2.6: Notification d\'échec...'
            mail to: 'h_mokeddem@esi.dz',
                 subject: "FAILED: ${env.JOB_NAME} [#${env.BUILD_NUMBER}]",
                 body: "Le build a échoué. Vérifiez les logs sur Jenkins."
        }
    }
}