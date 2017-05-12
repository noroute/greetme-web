# About

_GreetMe Web_ is a trivial web frontend for _GreetMe Server_ that allows a user to request a personalized greeting messages
in his preferred language.
 
It is merely a simple demo application that was used for winning insights of the build, deployment, and management possibilities
provided by PaaS solutions.

For more details, see the description of _GreetMe Server_.

# Building the project
As this project functionally depends on its backend, _GreetMe Server_ is started prior of integration testing.
Therefore, a specific [Docker Compose JUnit Rule](https://github.com/palantir/docker-compose-rule) is used.
The library containing this JUnit Rule is available via Bintray. So the according repository needs to be added to
your Maven _setttings.xml_ file: 
```
...
  </profiles>
    <profile>
      <id>bintray</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
          <id>bintray-palantir</id>
          <url>https://dl.bintray.com/palantir/releases</url>
        </repository>
      </repositories>
    </profile>
  </profiles>
...
```

# Running Selenium WebDriver tests
To run these integration tests, you need to have Docker, Firefox 52+, and [geckodriver](https://github.com/mozilla/geckodriver/releases)
installed.

Also, you need to have built a Docker image for _GreetMe Server_. Therefore, run `mvn clean install && docker build -t greetme-server .` in
that other project.
          
Finally, you need to set the path to the _geckodriver_ in this project's _pom.xml_ - look for line:
```
<argLine>-Dwebdriver.gecko.driver=/path/to/geckodriver</argLine>
```

You can then run the tests by using the Maven profile _selenium_:
```
mvn verify -Pselenium
```