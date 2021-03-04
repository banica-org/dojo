# Banica
![Build and Publish](https://github.com/stef2georg/banica/workflows/Build%20and%20Publish/badge.svg)
[![codecov](https://codecov.io/gh/stef2georg/banica/branch/master/graph/badge.svg)](https://codecov.io/gh/stef2georg/banica)
[![Total alerts](https://img.shields.io/lgtm/alerts/g/stef2georg/banica.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/stef2georg/banica/alerts/)
[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/stef2georg/banica.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/stef2georg/banica/context:java)

https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages

```<!-- Maven setting file. It should be located in ~/.m2. -->
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
	<repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
        </repository>
	<repository>
          <id>github</id>
          <name>GitHub OWNER Apache Maven Packages</name>
          <url>https://maven.pkg.github.com/stef2georg/banica</url>
          <snapshots><enabled>true</enabled></snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>stef2georg</username>
      <password>Personal access token generated at https://github.com/settings/tokens for permission read:packages</password>
    </server>
  </servers>
</settings>
```
