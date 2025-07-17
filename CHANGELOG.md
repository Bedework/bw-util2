# Release Notes

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Unreleased (6.1.0-SNAPSHOT)

## [6.0.1] - 2025-07-17
* Update commons-lang3 - CVE-2025-48924

## [6.0.0] - 2025-06-26
* First jakarta release

## [5.0.10] - 2025-02-06
* Update library versions.
* Move response classes and ToString into bw-base module.
* Pre-jakarta

## [5.0.9] - 2024-12-17
* Update library versions.
* Normalize the property list when converting from ical to xml. Unable to handle multivalued exdate and rdate so split them into a number of single valued properties.

## [5.0.8] - 2024-11-13
* Update library versions.
* Missing endCalendar call so no result.
* New index value

## [5.0.7] - 2024-09-18
* Update library versions.

## [5.0.6] - 2024-09-18
* Update library versions.
* New class to handle event participants.

## [5.0.5] - 2024-07-25
* Update library versions.
* Add processing for exdate and fix exrule

## [5.0.4] - 2024-06-01
* Update library versions.
* Update for new ical4j version

## [5.0.3] - 2024-05-15
* Update library versions.

## [5.0.2] - 2024-05-12
* Update library versions.
* Next stage in removing the bw-xml module.
    * icalendar schema moved into its own module
    * Many modules updated to refer to it
    * calws schema moved into caldav
    * feature pack update to deploy calws wsdls into bedework-content
* Fix handling of UNTIL element in recurrences when converting to/from xml
* Cosmetic and use instanceof patterns

## [5.0.1] - 2023-12-08
* Update library versions.
* Changes for vlocation
* Add EMAIL parameter to index
* Add a concept entity with a couple of methods and use it.
* Add a do not use flag to locations
* Add more information to message to identify property.

## [5.0.0] - 2022-02-12
* Update library versions.
* Use bedework-parent

## [4.0.12] - 2021-09-21
* Update library versions
* Fix WsXMLTranslator

## [4.0.11] - 2021-09-14
* Update library versions

## [4.0.10] - 2021-09-11
* Update library versions

## [4.0.9] - 2021-09-05
* Update library versions
* bw-util version and add ESTIMATED-DURATION support

## [4.0.8] - 2021-06-07
* Update library versions

## [4.0.7] - 2021-05-31
* Update library versions
* Move a bunch of vcard related code from carddav into common utility project
* Add methods to allow easier setting of properties.
* Handle CONCEPT
* Handle uid and fix geo
* Move some icalspecific code into util2.IcalendarUtil

## [4.0.6] - 2020-03-20
* Update javadoc plugin config
* Bump jackson version
* Updates for chnages to vpoll spec. Using participant not vvoter. Update to ical4j version and many related changes
* Use regular expressions to parse date time values in XcalUtil.getIcalFormatDateTime. Return null for bad values or null.
* Remove some references to log4j

## [4.0.5] - 2019-08-26
* Update library versions.

## [4.0.4] - 2019-04-14
* Update library versions.

## [4.0.3] - 2019-01-07
* Update library versions.

## [4.0.2] - 2018-12-13
* Update library versions.

## [4.0.1] - 2018-11-27
* Update library versions.
* Try to fix the comparison for properties. They are supposed to be in a fixed order by name - however the name of wrapped x-properties is a parameter. Use that parameter for the comparison.
* Add RDATE processing for ical to xcal

## [4.0.0] - 2018-04-08
* Move bw-util/bw-util-calendar and bw-util/bw-util-vcard into this project
