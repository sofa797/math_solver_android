pipeline {
    agent any

    environment {
        DATABASE_URL = credentials('database-url')
        HF_API_KEY = credentials('hf-api-key')
        POSTGRES_PASSWORD = credentials('postgres-password')
    }

    stages {

        stage('Deploy') {
            steps {
                sh '''
                docker compose down
                docker compose build

                DATABASE_URL=$DATABASE_URL \
                HF_API_KEY=$HF_API_KEY \
                POSTGRES_PASSWORD=$POSTGRES_PASSWORD \
                docker compose up -d

                sleep 10
                curl http://localhost:8000/docs || exit 1
                '''
            }
        }
    }
}
