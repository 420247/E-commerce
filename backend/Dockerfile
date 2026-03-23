# ─────────────────────────────────────────────────────────────────────────────
# Stage 1: BUILD
# We use the full JDK image here because we need Maven to compile the project.
# This stage will NOT be included in the final image — only the compiled jar
# will be copied over to Stage 2.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml BEFORE copying source code.
# Docker builds images in layers — each instruction is a cached layer.
# If pom.xml hasn't changed, Docker reuses the cached dependency layer
# and skips the slow "download all dependencies" step on the next build.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download all dependencies into the local Maven cache (~/.m2).
# -q flag suppresses verbose output so logs are easier to read.
RUN ./mvnw dependency:go-offline -q

# Now copy the actual source code.
# This layer changes on every commit, but the dependency layer above is
# still cached — so we don't re-download dependencies on every build.
COPY src ./src

# Compile, run tests, and package the application into a single .jar file.
# -DskipTests because tests run in a dedicated CI job with a real database.
RUN ./mvnw package -DskipTests -q

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2: RUNTIME
# We use JRE (Java Runtime Environment) instead of JDK (Java Development Kit).
# JRE is ~85 MB vs ~400 MB for JDK — we don't need a compiler at runtime.
# The final image contains ONLY the JRE + our compiled jar, nothing else.
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Security best practice: never run application processes as root.
# If an attacker exploits a vulnerability, they get limited OS permissions.
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy only the compiled jar from the build stage.
# The *.jar wildcard matches whatever Maven named the file (includes version).
COPY --from=builder /app/target/*.jar app.jar

# Document which port the application listens on.
# This does NOT actually publish the port — that happens in docker-compose.
EXPOSE 8080

# Spring Boot automatically reads environment variables and maps them to
# application.yml properties. For example:
#   SPRING_DATASOURCE_URL     → spring.datasource.url
#   SPRING_DATASOURCE_USERNAME → spring.datasource.username
# This means we can configure the app entirely through environment variables
# without modifying any code or config files.
ENTRYPOINT ["java", "-jar", "app.jar"]
