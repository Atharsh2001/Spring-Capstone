FROM openjdk:18
EXPOSE 8080
ADD target/Capstone-Spring.jar Capstone-Spring.jar
ENTRYPOINT ["java","-jar","/Capstone-Spring.jar"]

