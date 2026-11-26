# Hibernate Hydrate #

[![Build Status](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml/badge.svg)](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate7-hydrate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate7-hydrate)

The primary goal of the [Hibernate Hydrate](https://github.com/arey/hibernate-hydrate) project is to populate a graph of persistent entities and thus avoid the famous [LazyInitializationException](http://docs.jboss.org/hibernate/orm/3.6/javadocs/org/hibernate/LazyInitializationException.html).

## Features ##

* Utility class to populate a lazy-initialized object graph
* Follows associations to entities, collections, maps, arrays, embeddables, `@Any` and `@ManyToAny`
  associations and associations inside composite identifiers (`@IdClass` and `@EmbeddedId`)
* Handles deep object graphs without overflowing the stack
* Supports bytecode enhanced entities including their lazy basic attributes
* Attributes can be excluded from the hydration
* Supports JPA with Hibernate as provider
* Supports Hibernate 3.x to 7.x (with different artefactId)

## Getting Help ##

This readme file as well as the [wiki](https://github.com/arey/hibernate-hydrate/wiki) are the best places to start learning about Hibernate Hydrate. 
There are also unit tests available to look at.

The [wiki](https://github.com/arey/hibernate-hydrate/wiki) contains links to basic project information such as source code, jenkins build, javadocs, issue tracking, etc.

A French article titled *Say goodbye to LazyInitializationException* : http://javaetmoi.com/2012/03/hibernate-dites-adieu-aux-lazy-initialization-exception/

## Quick Start ##

### Dependency ###

Download the jar though Maven:

```xml
<!-- Either Hibernate 7.0.0 and above support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate7-hydrate</artifactId>
  <version>7.0.1</version>
</dependency> 

<!-- or Hibernate 6.1.6 and above support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate6-hydrate</artifactId>
  <version>6.3.4</version>
</dependency> 

<!-- or Hibernate 5.2 and above support -->
<dependency>
  <groupId>com.javaetmoi.core</groupId>
  <artifactId>javaetmoi-hibernate5-hydrate</artifactId>
  <version>5.2.4</version>
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

Hibernate Hydrate artefacts are available from [Maven Central](https://repo1.maven.org/maven2/com/javaetmoi/core/javaetmoi-hibernate7-hydrate/)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate7-hydrate/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.javaetmoi.core/javaetmoi-hibernate7-hydrate)

### Usage ###

First get a `Hydrator` instance. There are four possible ways:
* `Hydrator.hydrator(entityManagerFactory)`
* `Hydrator.hydrator(entityManager)`
* `Hydrator.hydrator(sessionFactory)`
* `Hydrator.hydrator(session)`

To this `Hydrator` instance you can pass **attached** entities that need to be fully initialized.
Afterward you can detach these entities and access all of their transitive attributes 
without getting problems with lazy loading. That is, there will be no `LazyInitializationException`.

A simple example of a service method that returns a fully initialized entity 
except for its `mySubEntities` attribute:

```java
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import com.javaetmoi.core.persistence.hibernate.Hydrator;
import static com.javaetmoi.core.persistence.hibernate.Hydrator.hydrator;

public class MyEntityService {
  private final EntityManager entityManager;
  private final Hydrator hydrator;

  public MyEntityService(EntityManager entityManager) {
    this.entityManager = entityManager;
    this.hydrator = hydrator(entityManager).withExclude(MyEntity.class, "mySubEntities");
  }

  @Transactional
  public MyEntity myEntity(long id) {
    var myEntity = entityManager.find(MyEntity.class, id);
    var myHydratedEntity = hydrator.deepHydrate(myEntity);
    return myHydratedEntity;
  }
}
```

`withExclude` excludes associations, collections, embedded attributes and
lazy basic attributes (`@Basic(fetch = LAZY)`) from the hydration.
Attributes inside of embedded attributes and embedded identifiers are addressed by a path,
e.g. `"address.country"` or `"id.owner"`.
Attributes inside of collections of embeddables cannot be excluded.
Other basic attributes are never hydrated, so excluding them, like excluding unknown attributes,
throws an `IllegalArgumentException`.

Lazy basic attributes are lazy for bytecode enhanced entities only.
Hibernate loads all attributes of a lazy group (`@LazyGroup`) at once.
So to keep an excluded lazy attribute unloaded, all attributes of its lazy group have to be excluded.

### Performance ###

Hibernate Hydrate loads each uninitialized proxy and each uninitialized collection by a query of its own.
For big object graphs it is recommended to enable the batch fetching of Hibernate,
so that Hibernate loads several proxies or collections of the same kind by one query.
Either globally by the property `hibernate.default_batch_fetch_size`, e.g. in the `persistence.xml`:

```xml
<property name="hibernate.default_batch_fetch_size" value="100" />
```

Or by `@BatchSize` at single entities and collections.

## Contributing to Hibernate Hydrate ##

* GitHub is for social coding platform: if you want to write code, we encourage contributions through pull requests from [forks of this repository](http://help.github.com/forking/). 
  If you want to contribute code this way, please reference a GitHub ticket as well covering the specific issue you are addressing.
* Each major version of Hibernate has it own git branch: 
  * Hibernate 7 on the master
  * Hibernate 6.2 on the hibernate6 branch
  * Hibernate 5 on the hibernate5 branch
  * Hibernate 4 on the hibernate4 branch 
  * Hibernate 3 on the hibernate3 branch

### Development environment installation ###

Download the code with git:

``git clone git://github.com/arey/hibernate-hydrate.git``

Compile the code with maven:

``mvn clean install``

If you're using an IDE that supports Maven-based projects (IntelliJ Idea, Netbeans or m2Eclipse), you can import the project directly from its POM. 
Otherwise, generate IDE metadata with the related IDE maven plugin:

``mvn eclipse:clean eclipse:eclipse``

## Release ##

This project artefact is published to Maven Central using the Maven Release Plugin.
The [release.yml](https://github.com/arey/hibernate-hydrate/actions/workflows/release.yml) GitHub Actions workflow automates the process.

### Versioning scheme (`X.Y.Z`) ###

* **`X` (Major)**: Supported major Hibernate version (matches the Git branch and the `javaetmoi-hibernateX-hydrate` `artifactId`).
* **`Y` (Minor)**: Minimum required Hibernate minor version (e.g. `0` for Hibernate 7.0+, `2` for Hibernate 5.2+) or `Hydrator` API evolution. It is **not** incremented when adding support for a new backward-compatible Hibernate minor version.
* **`Z` (Patch)**: Bug fixes, dependency updates, and support for new backward-compatible Hibernate minor versions (verified against previous minor versions via Maven profiles in CI).

### Release checklist ###

1. Ensure all supported Hibernate minor profiles (`hibernate-X.Y`) are declared in [pom.xml](pom.xml) and tested in [build.yml](.github/workflows/build.yml).
2. Run the [Maven Release](https://github.com/arey/hibernate-hydrate/actions/workflows/release.yml) GitHub Actions workflow (`workflow_dispatch`) by specifying `releaseVersion` (e.g. `7.0.1`) and `developmentVersion` (e.g. `7.0.2-SNAPSHOT`).
3. Update the dependency version in the [Quick Start](#dependency) section of this `readme.md`.


## Credits ##

* Uses [Maven](http://maven.apache.org/) as a build tool
* Uses [GitHub Actions](https://github.com/features/actions) for continuous integration builds whenever code is pushed into GitHub
* [Izaak (John) Alpert](https://github.com/karlhungus) and [Marc Cobery](https://github.com/mcobery) and [Markus Heiden](https://github.com/markusheiden) for their pull requests
 

## Build Status ##

GitHub Actions: [![Java CI](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml/badge.svg)](https://github.com/arey/hibernate-hydrate/actions/workflows/build.yml)
