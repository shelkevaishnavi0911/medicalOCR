FROM maven:3.9.9-eclipse-temurin-17

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN echo "===== JAR FILES =====" && find /app/target -maxdepth 1 -type f -name "*.jar" -ls

EXPOSE 10000

CMD ["sh", "-c", "JAR=$(find /app/target -maxdepth 1 -type f -name '*.jar' | head -n 1) && echo \"Starting: $JAR\" && java -jar \"$JAR\""]