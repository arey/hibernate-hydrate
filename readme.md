# Hibernate Hydrate #

[![Build Status](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml/badge.svg)](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate6-hydrate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate6-hydrate)

The primary goal of the [Hibernate Hydrate](https://github.com/arey/hibernate-hydrate) project is to populate a graph of persistent entities and thus avoid the famous [LazyInitializationException](http://docs.jboss.org/hibernate/orm/3.6/javadocs/org/hibernate/LazyInitializationException.html).

## Features ##

* Utility class to populate a lazy-initialized object graph by recursivity
* Supports JPA with Hibernate as provider
* Supports Hibernate 3.x to 6.x (with different artefactId)

## Getting Help ##

This readme file as well as the [wiki](https://github.com/arey/hibernate-hydrate/wiki) are the best places to start learning about Hibernate Hydrate. 
There are also unit tests available to look at.

The [wiki](https://github.com/arey/hibernate-hydrate/wiki) contains links to basic project information such as source code, jenkins build, javadocs, issue tracking, etc.

A French article titled *Say goodbye to LazyInitializationException* : http://javaetmoi.com/2012/03/hibernate-dites-adieu-aux-lazy-initialization-exception/

## Quick Start ##

Download the jar though Maven:

```xml
<!-- Either Hibernate 6.1.6 and above support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate6-hydrate</artifactId>
  <version>6.2.3</version>
</dependency> 

<!-- or Hibernate 5.2 and above support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate5-hydrate</artifactId>
  <version>5.2.3</version>
</dependency> 

<!-- or Hibernate 5.0 and 5.1 support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate5-hydrate</artifactId>
  <version>2.3</version>
</dependency> 

<!-- or Hibernate 4 support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate4-hydrate</artifactId>
  <version>2.2</version>
</dependency> 

<!-- or Hibernate 3 support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate3-hydrate</artifactId>
  <version>2.2</version>
</dependency> 
```

Please note that we are not able to support Hibernate versions 6.0 up to 6.1.5 due to bugs in them.

Hibernate Hydrate artefacts are available from [Maven Central](https://repo1.maven.org/maven2/com/javaetmoi/core/javaetmoi-hibernate6-hydrate/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate6-hydrate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate6-hydrate)


## Contributing to Hibernate Hydrate ##

* GitHub is for social coding platform: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). 
  If you want to contribute code this way, please reference a GitHub ticket as well covering the specific issue you are addressing.
* Each major version of Hibernate has it own git branch: Hibernate 6.2 on the master, Hibernate 4 on the hibernate4 branch and Hibernate 3 on the hibernate3 branch

### Development environment installation ###

Download the code with git:

``git clone git://github.com/arey/hibernate-hydrate.git``

Compile the code with maven:

``mvn clean install``

If you're using an IDE that supports Maven-based projects (IntelliJ Idea, Netbeans or m2Eclipse), you can import the project directly from its POM. 
Otherwise, generate IDE metadata with the related IDE maven plugin:

``mvn eclipse:clean eclipse:eclipse``

## Release

This project artefact is published to Maven Central.
The Maven Release Plugin is used to release the project with Maven.
The [release.yml](https://github.com/arey/hibernate-hydrate/actions/workflows/release.yml) GitHub Actions workflow automates the process.


## Credits ##

* Uses [Maven](http://maven.apache.org/) as a build tool
* Uses [GitHub Actions](https://github.com/features/actions) for continuous integration builds whenever code is pushed into GitHub
* [Izaak (John) Alpert](https://github.com/karlhungus) and [Marc Cobery](https://github.com/mcobery) and [Markus Heiden](https://github.com/markusheiden) for their pull requests
 

## Build Status ##

GitHub Actions: [![Java CI](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml/badge.svg)](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml)
