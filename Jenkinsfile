pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        COMPOSE_PROJECT_NAME = 'flooring-finance'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Load environment') {
            steps {
                // .env holds real secrets (DB password, JWT secret) and is never committed.
                // Store it once on the Jenkins server as a "Secret file" credential
                // named "flooring-finance-env" and it will be copied into the workspace here.
                withCredentials([file(credentialsId: 'flooring-finance-env', variable: 'ENV_FILE')]) {
                    sh 'cp "$ENV_FILE" .env'
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh 'chmod +x mvnw'
                    sh './mvnw -q -B -DskipTests package'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm run build -- --configuration production'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh 'docker compose build --pull'
            }
        }

        stage('Stop Existing Containers') {
            steps {
                sh 'docker compose down --remove-orphans || true'
            }
        }

        stage('Docker Compose Up') {
            steps {
                sh 'docker compose up -d'
            }
        }

        stage('Health Check') {
            steps {
                script {
                    def healthy = false
                    for (int i = 0; i < 15 && !healthy; i++) {
                        sh 'sleep 5'
                        def status = sh(
                            script: 'curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || true',
                            returnStdout: true
                        ).trim()
                        healthy = (status == '200')
                    }
                    if (!healthy) {
                        sh 'docker compose logs --tail=100 backend'
                        error('Backend did not become healthy in time')
                    }
                }
                sh 'curl -sf http://localhost/ > /dev/null'
            }
        }

        stage('Cleanup') {
            steps {
                sh 'docker image prune -f'
            }
        }
    }

    post {
        failure {
            sh 'docker compose logs --tail=200 || true'
        }
        always {
            sh 'rm -f .env'
        }
    }
}
