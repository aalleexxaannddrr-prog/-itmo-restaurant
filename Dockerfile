# Используем базовый образ Ubuntu
FROM ubuntu:latest AS build

# Обновляем пакеты и устанавливаем необходимые инструменты
RUN apt-get update && \
    apt-get install -y wget maven curl

# Устанавливаем OpenJDK 22 (Oracle OpenJDK 22.0.1)
RUN wget https://download.oracle.com/java/22/latest/jdk-22_linux-x64_bin.deb && \
    apt install ./jdk-22_linux-x64_bin.deb && \
    rm ./jdk-22_linux-x64_bin.deb

# Устанавливаем переменные среды для использования JDK
ENV JAVA_HOME=/usr/lib/jvm/jdk-22
ENV PATH=$JAVA_HOME/bin:$PATH

# Копируем все файлы проекта в контейнер
COPY . .

# Собираем проект с помощью Maven
RUN mvn clean package

# Второй этап: создаём минимальный образ для выполнения Java-приложения
FROM ubuntu:latest

# Устанавливаем OpenJDK 22 для выполнения приложения
RUN wget https://download.oracle.com/java/22/latest/jdk-22_linux-x64_bin.deb && \
    apt install ./jdk-22_linux-x64_bin.deb && \
    rm ./jdk-22_linux-x64_bin.deb

# Устанавливаем переменные среды для выполнения JDK
ENV JAVA_HOME=/usr/lib/jvm/jdk-22
ENV PATH=$JAVA_HOME/bin:$PATH

# Копируем сгенерированный JAR файл из этапа сборки
COPY --from=build /target/your-app.jar /app/your-app.jar

# Указываем команду для выполнения JAR файла
CMD ["java", "-jar", "/app/your-app.jar"]
