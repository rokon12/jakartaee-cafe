# jakartaee-cafe

## Open Liberty

* To generate a sample Jakarta EE project with Open Liberty, please execute the following (please ensure you have installed a [Java SE 8 implementation](https://adoptium.net/?variant=openjdk8) and [Maven 3+](https://maven.apache.org/download.cgi)). Please note that the generated application will only work with Java SE 8 for GlassFish 5.

  ```
  mvn archetype:generate -DarchetypeGroupId=org.eclipse -DarchetypeArtifactId=jakarta-starter -DarchetypeVersion=1.1.0-SNAPSHOT -Druntime=liberty
  ```

* To run the generated project with Open Liberty, please execute the following from the project directory - named `jakartaee-cafe` by default. Please ensure you have installed a [Java SE 8 implementation](https://adoptium.net/?variant=openjdk8) and [Maven 3+](https://maven.apache.org/download.cgi).

  ```
  mvn clean liberty:run -Pliberty
  ```

  Once GlassFish starts, you can access the project at http://localhost:8080/jakartaee-cafe.


* To run the integration tests (`**.it.*.java`) : 
  ```
  mvn clean verify -Parq-liberty-managed
  ```
