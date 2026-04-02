pipeline {
    agent any

    environment {
        DATABASE_URL = credentials('database-url')
        HF_API_KEY = credentials('hf-api-key')
    }

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
                  -e DATABASE_URL=$DATABASE_URL \
                  -e HF_API_KEY=$HF_API_KEY \
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
