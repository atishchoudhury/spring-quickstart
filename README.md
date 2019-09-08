# QUICKSTART SAMPLES
## TESTING
### Writing Unit Test

[sample project](/spring-quickstart/tree/master/spring-rbac)

* Enable H2 on Test scope if using JPA
  * pom.xml file entry https://github.com/atishchoudhury/spring-quickstart/blob/master/spring-rbac/pom.xml
* Use MochitoJunitRunner with Monchito and JUnit annotations and classes 
  * Implementation of Runner and Moching https://github.com/atishchoudhury/spring-quickstart/blob/master/spring-rbac/src/test/java/com/example/app/rbac/UserControllerTest.java
* User Spring to Test Repository
  * SpringRunner with TestEntityManager https://github.com/atishchoudhury/spring-quickstart/blob/master/spring-rbac/src/test/java/com/example/app/rbac/UserProfileRepoTest.java
