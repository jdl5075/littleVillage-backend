spring-mvc
==========

Basic Spring 3 MVC template for building server apps.

Built using:

* Gradle
* Spring MVC
* Jackson
* Shiro - for authentication
* Tomcat 7
* Java 7

##How To Build##

1. Clone this project to your machine.
2. Install a postgres instance on your machine
3. copy your db credentials into db.properties
3. Make sure [Gradle](http://www.gradle.org/) is installed
4. Install Tomcat 7+ and ensure that it starts up
5. Run ``` gradle build ``` and a ``` build ``` directory will be built with the war in the lib directory
6. Push the war into the webapps directory of your tomcat
