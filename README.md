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


3. Создать файл .env с настройками для docker-compose

   POSTGRES_DB: <db_name>

   POSTGRES_USER: <user_name>

   POSTGRES_PASSWORD: <password> 

   KEYCLOAK_ADMIN: <user_name>

   KEYCLOAK_ADMIN_PASSWORD: <password>

   ACCOUNT_CLIENT_SECRET: <cient_secret>

   CASH_CLIENT_SECRET: <cient_secret>

   TRANSFER_CLIENT_SECRET: <cient_secret>

4. Выполнить команду сборки и запуска docker контейнеров 

   $ docker compose up --build -d


ToDo:

Обязательно:
1. Написать тесты

Доработки

1. Проверка введенных данных
2. Возвращать единый формат dto для операций с кодом и текстом ответа
3. Отображать информацию об успешности операции на странице
4. Передавать с UI dto без логина и добавлять на стороне transfer и cash
5. Проверять токен пользователя в cash и transfer сервисе, добавить в эти сервисы аннотации методной защиты
6. Вынести настройки bank-front и notification в config-server
7. Убрать из списка пользователей пользователя, который отправляет запрос
8. Разобраться почему возникает ошибка при использовании аннотации @LoadBalanced
9. Возвращать на страницу /client после операций с информациой об ошибке или успехе
10. Если сумма больше суммы на счёте отправления, то должна появляться ошибка)
11. Отправка запросов из bank front в gateway с использованием Eureka 