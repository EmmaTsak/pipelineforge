pipeline {

    agent {
        label 'linux-agent-1'
    }

    options {
        skipDefaultCheckout(true)
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Environment') {
            steps {
                sh 'java -version'
                sh 'javac -version'
                sh 'git --version'
            }
        }

        stage('Compile') {
            steps {
                dir('services/pipelineforge-api') {
                    sh 'chmod +x gradlew'
                    sh './gradlew --no-daemon clean classes testClasses'
                }
            }
        }

        stage('Unit Test') {
            steps {
                dir('services/pipelineforge-api') {
                    sh './gradlew --no-daemon test'
                }
            }
        }

        post {
            always {
                junit testResults: 
                'services/pipelineforge-api/build/test-results/test/*.xml'
            }
        }

        stage('Package') {
            steps {
                dir('services/pipelineforge-api') {
                    sh './gradlew --no-daemon bootJar'
                }
            }
        }
    }

    post {

        success {
            archiveArtifacts(
                artifacts: 'services/pipelineforge-api/build/libs/*.jar',
                fingerprint: true
            )

            echo 'PipelineForge CI completed successfully!'
        }

        failure {
            echo 'PipelineForge failed. Check the console output.'
        }
    }
}