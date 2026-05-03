# bank-app
Приложение «Банк» с использованием Spring Boot, интеграций Spring Cloud и паттернов микросервисной архитектуры.

Для запуска необходимо выполнить:
1. Сборку приложения (выполнять из корня проекта)

   $ mvnw clean package

2. Настроить клиентов и их роли в keycloak для межсервисного взаимодейсвтия

   2.1. bank-front-service

      2.1.1 account_viewer

      2.1.2 account_editor
	  
	  2.1.3 cash_deposit_or_withdraw

      2.1.4 transfer_cash

   2.2. client-service

	  2.1.1 notification

   2.3. cash-service

	  2.1.1 cash_deposit_or_withdraw

	  2.1.2 notification

   2.4. transfer-service

	  2.1.1 transfer_cash

	  2.1.2 notification

# Установка чарта
helm install bank-app ./bank-helm -f ./bank-helm/values.yaml

# Обновление
helm upgrade bank-app ./bank-helm -f ./bank-helm/values.yaml

# Удаление
helm uninstall bank-app

# Проверка статуса
helm status bank-app

# Просмотр шаблонов без установки
helm template bank-app ./bank-helm

# Запустить сборку отдельного сервиса
build account-service -p OPERATION=full -p ENVIRONMENT=dev

# Запустить зонтичный деплой
build bank-umbrella -p ENVIRONMENT=staging -p DEPLOYMENT_STRATEGY=rolling