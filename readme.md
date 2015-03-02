# Hibernate Hydrate #

The primary goal of the [Hibernate Hydrate](https://github.com/arey/hibernate-hydrate) project is to populate a graph of persistent entities and thus avoid the famous [LazyInitializationException](http://docs.jboss.org/hibernate/orm/3.6/javadocs/org/hibernate/LazyInitializationException.html).

## Features ##

* Utility class to populate a lazy-initialized object graph by recursivity
* Supports JPA with Hibernate as provider
* Supports both Hibernate 3.x and Hibernate 4.x

## Getting Help ##

This readme file as well as the [wiki](https://github.com/arey/hibernate-hydrate/wiki) are the best places to start learning about Hibernate Hydrate. 
There are also unit tests available to look at.

The [wiki](https://github.com/arey/hibernate-hydrate/wiki) contains links to basic project information such as source code, jenkins build, javadocs, issue tracking, etc.

A french article titled *Say goodbye to LazyInitializationException* : http://javaetmoi.com/2012/03/hibernate-dites-adieu-aux-lazy-initialization-exception/

## Quick Start ##

Download the jar though Maven:

```xml
<!-- Either Hibernate 4 support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate4-hydrate</artifactId>
  <version>2.1</version>
</dependency> 

<!-- Or Hibernate 3 support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate3-hydrate</artifactId>
  <version>1.3</version>
</dependency> 
```

Hybernate Hydrate artefacts are available from [Maven Central](http://repo1.maven.org/maven2/com/javaetmoi/core/javaetmoi-hibernate4-hydrate/)

## Contributing to Hibernate Hydrate ##

* Github is for social coding platform: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). If you want to contribute code this way, please reference a GitHub ticket as well covering the specific issue you are addressing.

### Development environment installation ###

Download the code with git:

``git clone git://github.com/arey/hibernate-hydrate.git``

Compile the code with maven:

``mvn clean install``

If you're using an IDE that supports Maven-based projects (InteliJ Idea, Netbeans or m2Eclipse), you can import the project directly from its POM. 
Otherwise, generate IDE metadata with the related IDE maven plugin:

``mvn eclipse:clean eclipse:eclipse``


## Credits ##

* Uses [Maven](http://maven.apache.org/) as a build tool
* Uses [Cloudbees](http://www.cloudbees.com/foss) and [Travis CI](http://www.travis-ci.org) for continuous integration builds whenever code is pushed into GitHub
* [Izaak (John) Alpert](https://github.com/karlhungus) and [Marc Cobery] (https://github.com/mcobery) for their pull requests
 

## Build Status ##

Travis: [![Build
Status](https://travis-ci.org/arey/hibernate-hydrate.png?branch=master)](https://travis-ci.org/arey/hibernate-hydrate)

Cloudbees Jenkins: [![Build
Status](https://javaetmoi.ci.cloudbees.com/job/hibernate-hydrate/badge/icon)](https://javaetmoi.ci.cloudbees.com/job/hibernate-hydrate/)

Bitdeli: [![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/arey/hibernate-hydrate/trend.png)](https://bitdeli.com/free "Bitdeli Badge")
