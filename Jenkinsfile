
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

        stage('Unit Tests') {
            steps {
                dir('services/pipelineforge-api') {
                    sh './gradlew --no-daemon test'
                }
            }

            post {
                always {
                    junit testResults:
                        'services/pipelineforge-api/build/test-results/test/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                dir('services/pipelineforge-api') {
                    sh './gradlew --no-daemon bootJar'
                }
            }
        }

        stage('Build Docker Image') {

            agent {
                label 'docker-builder'
            }

            steps {
                checkout scm

                sh 'docker info --format "{{.ServerVersion}}"'

                dir('services/pipelineforge-api') {
                    sh '''
                        set -eu

                         SHA=$(git rev-parse --short=12 HEAD)
                        TAG="pipelineforge-api:ci-${BUILD_NUMBER}-${SHA}"

                        echo "Building image: $TAG"

                        docker build -t "$TAG" .

                        docker image inspect "$TAG" \
                            --format '{{.Id}}' > ci-image-id.txt

                        printf '%s\\n' "$TAG" > ci-image-tag.txt
                    '''
                }

                archiveArtifacts(
                    artifacts: 'services/pipelineforge-api/ci-image-id.txt',
                    fingerprint: true
                )
            }
        }

        stage('Smoke Test Docker Image') {

            agent {
                label 'docker-builder'
            }

            steps {
                checkout scm

                sh '''
                    set -eu

                    SHA=$(git rev-parse --short=12 HEAD)
                    TAG="pipelineforge-api:ci-${BUILD_NUMBER}-${SHA}"

                    echo "Smoke testing image: $TAG"

                    bash scripts/smoke-test-image.sh "$TAG"
                '''
            }
        }
    }

    post {

         always {
            junit(
                testResults: 'services/pipelineforge-api/build/test-results/test/*.xml',
                allowEmptyResults: true
            )
        }

        success {
            archiveArtifacts(
                artifacts: 'services/pipelineforge-api/build/libs/*.jar',
                fingerprint: true
            )

            echo 'PipelineForge CI completed successfully!'
        }

        failure {
            echo 'Pipeline failed. Check the console output.'
        }
    }
}
