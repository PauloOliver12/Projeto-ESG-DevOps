# ---------- Etapa 1: build ----------
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Camada separada para cache das dependencias (so e refeita quando o pom.xml muda)
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
# Os testes rodam na etapa "Build e testes" do pipeline, antes da imagem ser gerada
RUN mvn -q -B -DskipTests clean package

# ---------- Etapa 2: runtime ----------
FROM eclipse-temurin:21-jre-alpine

ARG APP_VERSION=local
LABEL org.opencontainers.image.title="esg-compliance-api" \
      org.opencontainers.image.description="API REST de governanca e compliance ambiental (ESG)" \
      org.opencontainers.image.version="${APP_VERSION}"

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /app/target/esg-compliance-api-1.0.0.jar app.jar
RUN chown app:app app.jar
USER app

ENV APP_VERSION=${APP_VERSION} \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
