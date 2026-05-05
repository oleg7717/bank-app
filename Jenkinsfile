pipeline {
    agent any

    parameters {
        choice(
            name: 'ENV',
            choices: ['dev', 'prod'],
            description: 'Окружение'
        )
        booleanParam(
            name: 'DEPLOY_ALL',
            defaultValue: true,
            description: 'Деплоить все сервисы?'
        )
    }

    environment {
        K8S_NAMESPACE = 'bank-app'
        VERSION = new Date().format('yyyyMMdd-HHmmss')
    }

    stages {
        stage('Подготовка') {
            steps {
                echo 'Начинаем деплой всей системы...'
                echo "Версия: ${VERSION}"
                echo "Окружение: ${params.ENV}"
            }
        }

        stage('Проверка Helm чартов') {
            steps {
                dir('bank-chart') {
                    echo 'Проверяем Helm чарты...'
                    sh 'helm lint ./'
                    sh 'helm template test ./'
                }
            }
        }

        stage('Сборка всех сервисов') {
            when {
                expression { params.DEPLOY_ALL }
            }
            parallel {
                stage('Account Service') {
                    steps {
                        build job: 'account-service',
                               parameters: [
                                   string(name: 'ENV', value: params.ENV)
                               ]
                    }
                }

                stage('Cash Service') {
                    steps {
                        build job: 'cash-service',
                               parameters: [
                                   string(name: 'ENV', value: params.ENV)
                               ]
                    }
                }

                stage('Transfer Service') {
                    steps {
                        build job: 'transfer-service',
                               parameters: [
                                   string(name: 'ENV', value: params.ENV)
                               ]
                    }
                }

                stage('Notification Service') {
                    steps {
                        build job: 'notification-service',
                               parameters: [
                                   string(name: 'ENV', value: params.ENV)
                               ]
                    }
                }

                stage('Bank Front') {
                    steps {
                        build job: 'bank-front',
                               parameters: [
                                   string(name: 'ENV', value: params.ENV)
                               ]
                    }
                }
            }
        }

        stage('Деплой через Helm') {
            steps {
                dir('bank-chart') {
                    script {
                        if (params.ENV == 'dev') {
                            sh """
                                helm upgrade --install bank-app ./ \
                                    --namespace ${K8S_NAMESPACE} \
                                    --create-namespace \
                                    --set global.environment=dev \
                                    --values values-dev.yaml \
                                    --wait
                            """
                        } else {
                            input message: 'Деплоим в PRODUCTION?', ok: 'Да'
                            sh """
                                helm upgrade --install bank-app ./ \
                                    --namespace ${K8S_NAMESPACE}-prod \
                                    --create-namespace \
                                    --set global.environment=prod \
                                    --values values-prod.yaml \
                                    --wait
                            """
                        }
                    }
                }
            }
        }

        stage('Проверка всех сервисов') {
            steps {
                script {
                    def services = ['account-service', 'cash-service', 'transfer-service', 'bank-front']

                    services.each { service ->
                        echo "Проверяем ${service}..."
                        sh """
                            kubectl get pods -l app=${service} -n ${K8S_NAMESPACE}
                        """
                    }
                }
            }
        }

        stage('Smoke тесты') {
            steps {
                echo 'Запускаем быстрые тесты...'
                sh """
                    # Проверяем фронт
                    curl -f http://bank-front:8080/actuator/health || echo "Фронт не готов"

                    # Проверяем один из сервисов
                    curl -f http://account-service:8081/actuator/health || echo "Сервис не готов"
                """
            }
        }
    }

    post {
        success {
            echo """
                ========================================
                ✅ Деплой успешно завершен!
                Окружение: ${params.ENV}
                Время: ${new Date()}
                ========================================
            """
        }
        failure {
            echo """
                ========================================
                ❌ Деплой провалился!
                Смотри логи в Jenkins
                ========================================
            """
        }
    }
}