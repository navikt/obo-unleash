FROM gcr.io/distroless/java21

COPY /target/obo-unleash.jar app.jar
CMD ["app.jar"]