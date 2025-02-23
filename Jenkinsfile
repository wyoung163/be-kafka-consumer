pipeline {
    agent any

    environment {
        REPO_URL = 'https://github.com/snowducks/BE-kafka-consumer.git'
        GIT_CREDENTIALS = 'github-credential'
        AWS_REGION = 'ap-northeast-2'
        AWS_ACCOUNT_ID = '796973504685'
        ECR_REPO_NAME = 'server/kafka-consumer'
        ECR_CREDENTIALS = 'aws-ecr-credential'
        SQ_CREDENTIALS = 'sonarqube-credential'
        SQ_PROJECT_KEY = 'sq-kafka-consumer-project-key'
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    git branch: 'dev', credentialsId: GIT_CREDENTIALS, url: REPO_URL
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool 'sonarqube-scanner';
                    withSonarQubeEnv(credentialsId: SQ_CREDENTIALS, installationName: 'sonarqube') {
                        withCredentials([string(credentialsId: SQ_PROJECT_KEY, variable: 'PROJECT_KEY')]) {
                        sh 'chmod +x ./gradlew'
                        sh './gradlew build'
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=${PROJECT_KEY} \
                            -Dsonar.projectName=${PROJECT_KEY} \
                            -Dsonar.sources=src \
                            -Dsonar.java.binaries=build/classes/java/main \
                            -Dsonar.sourceEncoding=UTF-8
                        """
                        }
                    }
                }
            }
        }

        stage('Login to AWS ECR') {
            steps {
                script {
                    withAWS(credentials: ECR_CREDENTIALS, region: AWS_REGION) {
                        sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def ecrImage = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:latest"
                    sh "docker build --cache-from=${ecrImage} -t ${ecrImage} ."
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    def ecrImage = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO_NAME}:latest"
                    sh "docker push ${ecrImage}"
                }
            }
        }
    }

    post {
        always {
            sh 'docker logout ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com'
        }
    }
}