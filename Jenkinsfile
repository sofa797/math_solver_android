pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t math-backend .'
            }
        }

        stage('Run Backend') {
            steps {
                sh '''
                docker stop math-backend || true
                docker rm math-backend || true

                docker run -d \
                  --name math-backend \
                  -p 8000:8000 \
                  --env-file .env \
                  math-backend

                sleep 5
                curl http://localhost:8000/docs || exit 1
                '''
            }
        }
    }

    post {
        always {
            sh 'docker ps'
        }
    }
}
