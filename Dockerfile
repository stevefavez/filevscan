FROM openjdk:8-jdk
# File scanner container - use clamav to scan files
MAINTAINER Steve Favez - steve.favez@admin.vs.ch

#add spring boot application
VOLUME /tmp
EXPOSE 8080
ADD target/filevscan.jar filevscan.jar
ENTRYPOINT ["java"]
CMD ["-jar","/filevscan.jar"]