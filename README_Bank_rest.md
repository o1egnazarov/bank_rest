# Система управления банковскими картами

---

## Пошаговая инструкция запуска проекта

### Установка и запуск

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/o1egnazarov/bank_rest.git
   cd Bank_restt
   ```

2. **Соберите проект и создайте Docker-образ:**
   ```bash
   make build
   ```

3. **Запустите контейнеры:**
   ```bash
   make up
   ```

4. **Проверьте доступность Swagger UI:**
   [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

5. **Для остановки контейнеров:**
   ```bash
   make down
   ```

6. **Для остановки и удаления томов:**
   ```bash
   make down VOLUMES=true
   ```

7. **Для пересборки образа и перезапуска приложения:**
   ```bash
   make rebuild
   ```

---

##  Проверка работоспособности

Swagger доступен по адресу:

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

---

## Команды Makefile

| Команда              | Описание                                              |
|----------------------|-------------------------------------------------------|
| `make build`         | Собирает приложение и создаёт Docker-образ           |
| `make up`            | Запускает контейнеры через docker-compose            |
| `make down`          | Останавливает контейнеры и удаляет их                |
| `make down VOLUMES=true` | Останавливает контейнеры и удаляет связанные тома  |
| `make rebuild`       | Пересобирает образ, перезапускает контейнеры         |