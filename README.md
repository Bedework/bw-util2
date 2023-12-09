# bw-util2 [![Build Status](https://travis-ci.org/Bedework/bw-util2.svg)](https://travis-ci.org/Bedework/bw-util2)

This project provides a number of utility classes and methods for
[Bedework](https://www.apereo.org/projects/bedework).

It was split off from the bw-util project to avoid some circular dependency issues

## Requirements

1. JDK 8
2. Maven 3

## Building Locally

> mvn clean install

## Releasing

Releases of this fork are published to Maven Central via Sonatype.

To create a release, you must have:

1. Permissions to publish to the `org.bedework` groupId.
2. `gpg` installed with a published key (release artifacts are signed).

To perform a new release:

> mvn -P bedework-dev release:clean release:prepare

When prompted, select the desired version; accept the defaults for scm tag and next development version.
When the build completes, and the changes are committed and pushed successfully, execute:

> mvn -P bedework-dev release:perform

For full details, see [Sonatype's documentation for using Maven to publish releases](http://central.sonatype.org/pages/apache-maven.html).

## Release Notes
### 4.0.0
  * Move bw-util/bw-util-calendar and bw-util/bw-util-vcard into this project

### 4.0.6
* Update javadoc plugin config
* Bump jackson version
* Updates for chnages to vpoll spec. Using participant not vvoter. Update to ical4j version and many related changes
* Use regular expressions to parse date time values in XcalUtil.getIcalFormatDateTime. Return null for bad values or null.
* Remove some references to log4j

#### 4.0.7
* Update library versions
* Move a bunch of vcard related code from carddav into common utility project
* Add methods to allow easier setting of properties.
* Handle CONCEPT
* Handle uid and fix geo
* Move some icalspecific code into util2.IcalendarUtil

#### 4.0.8
* Update library versions

#### 4.0.9
* Update library versions
* bw-util version and add ESTIMATED-DURATION support

#### 4.0.10
* Update library versions

#### 4.0.11
* Update library versions

#### 4.0.12
* Update library versions
* Fix WsXMLTranslator

#### 5.0.0
* Use bedework parent
* Update library versions

#### 5.0.1
* Update library versions
* Changes for vlocation
* Add EMAIL parameter to index
* Add a concept entity with a couple of methods and use it.
* Add a do not use flag to locations
* Add more information to message to identify property.
