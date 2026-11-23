FROM europe-north1-docker.pkg.dev/cgr-nav/pull-through/nav.no/jre:openjdk-21

COPY /target/obo-unleash.jar app.jar
ENV TZ="Europe/Oslo"
CMD ["-jar", "app.jar"]