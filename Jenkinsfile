pipeline {
    agent any

    environment {
        DATABASE_URL = credentials('database-url')
        HF_API_KEY = credentials('hf-api-key')
        POSTGRES_PASSWORD = credentials('postgres-password')
    }

    stages {
        stage('Setup') {
            steps {
                sh'''
                    python -m venv .venv
                    . .venv/bin/activate
                    pip install -r requirements.txt
                    pip install pytest-cov flake8
                '''
            }
        }

        stage('Lint') {
            steps {
                sh'''
                    . .venv/bin/activate
                    flake8 backend
                '''
            }
        }

        stage('Test') {
            steps {
                sh'''
                . .venv/bin/activate
                export DATABASE_URL=sqlite:///./test.db
                pytest \
                    --junitxml=report.xml \
                    --cov=backend \
                    --cov-report=xml \
                    --cov-report=term \
                    --cov-fail-under=80
                '''
            }
            post {
                always {
                    junit 'report.xml'
                    publishCoverage adapters: [coberturaAdapter('coverage.xml')]
                }
            }
        }

        stage('Build') {
            steps {
                sh'''
                docker compose build
                '''
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                docker compose down

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

        post {
            success {
                echo "success"
            }
            failure {
                echo "failed"
            }
            unstable {
                echo "unstable"
            }
            always {
                echo "finished"
            }
    }
}
