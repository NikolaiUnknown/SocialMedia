FROM gradle:jdk23 AS build
WORKDIR /app
COPY ./build.gradle ./
COPY ./settings.gradle ./
RUN gradle build --dry-run || return 0
COPY . .
RUN mkdir /app/pictures
COPY ./src/main/resources/static/default-avatar.png /app/pictures
RUN gradle build

FROM openjdk:23-jdk AS final
WORKDIR /app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar /app/app.jar
COPY --from=build /app/init/elasticsearch/users.json /app/init/elasticsearch/users.json
COPY --from=build /app/pictures /app/pictures
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]