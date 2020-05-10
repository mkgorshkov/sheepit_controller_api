# Sheepit Controller API
If you're been looking for a way to remotely ensure that sheepit is running as well as have the ability to perform simple commands
such as restart and kill this is for you.

The prerequisite for this project would be to have sheepit already running in textui mode on the server in question. This controller API
can then be launched as a jar on the server:

1. Compile the project with `mvn clean package` and it will create a jar in the `/target` directory
2. Upload the jar to the server in question
3. Ensure that sheepit is launched with the full path such as `java -jar ~/path/to/sheepit-client-6.1712.0.jar -login [username] -password [password] -ui text`
3. `java -jar sheepit-controller-api-0.0.1-SNAPSHOT.jar`
4. Hit the API, usually through `localhost:8080/healthcheck` if you're on the server/tunneled or `[SOME IP]:8080/healthcheck`

## API Endpoints
We only really want to use the `/healthcheck/` endpoint because we can do all of the operations from there.

## How does it work?
Read my writeup here on [DevopsDebug](https://devopsdebug.com/sheepit-remote-controller)

### Reference Documentation

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.6.RELEASE/maven-plugin/)
* [Sheepit Render Farm](https://sheepit-renderfarm.com)
* [DevopsDebug](https://devopsdebug.com)