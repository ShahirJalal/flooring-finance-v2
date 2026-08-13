pipeline {
    agent { label 'ubuntu' }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                // .env lives outside the workspace (~/flooring-finance.env) on
                // purpose - this job's checkout wipes untracked files, so a
                // .env sitting inside the workspace doesn't survive a rebuild.
                sh 'cp ~/flooring-finance.env .env'
            }
        }

        stage('Stop Existing Stack') {
            steps {
                sh 'docker compose down || true'
            }
        }

        stage('Build & Deploy') {
            steps {
                sh 'docker compose up -d --build --remove-orphans'
            }
        }

        stage('Verify Deployment') {
            steps {
                // The backend takes a while to actually come up (Flyway + JPA
                // startup), so give it a few tries rather than a single curl.
                sh '''
                    for i in $(seq 1 15); do
                        if curl --fail --silent --show-error http://localhost:4204/ > /dev/null; then
                            exit 0
                        fi
                        sleep 5
                    done
                    echo "Frontend did not become healthy in time"
                    exit 1
                '''
            }
        }
    }

    post {
        success {
            echo 'Flooring Finance deployed successfully!'
        }

        failure {
            echo 'Deployment failed.'
        }

        always {
            sh 'docker image prune -f || true'
        }
    }
}
