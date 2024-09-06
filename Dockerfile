# Используем базовый образ OpenJDK 17
FROM openjdk:17-jdk-slim AS build

# Устанавливаем Maven
RUN apt-get update && \
    apt-get install -y maven

# Создаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта в контейнер
COPY pom.xml ./
COPY src ./src

# Собираем проект с помощью Maven
RUN mvn clean package

# Используем базовый образ OpenJDK 17 для выполнения
FROM openjdk:17-jdk-slim

# Создаем рабочую директорию
WORKDIR /app

# Копируем скомпилированное приложение из этапа сборки
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar /app/demo.jar

# Открываем порт
EXPOSE 8080

# Определяем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "/app/demo.jar"]
