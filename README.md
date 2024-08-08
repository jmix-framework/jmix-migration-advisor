# jmix-migration-advisor
## Overview
This tool helps to make project migration by providing some estimations and notes acquired via automatical project analysis.
> **IMPORTANT**: this is not a comprehensive estimation - some aspects can't be automatically estimated properly and required manual analysis.
> Consider this data as rough low-range estimate.

## Installation
Download or build distibution (zip archive).

After extracting the archive use bin/migration-advisor or bin/migration-advisor.bat files for executing CLI commands.

Distribution directories structure:
```
migration-advisor
  -  bin (contains CLI executables)
  -  lib (required Java libraries)
```

## Usage
### Analyze CUBA Project
This command analyzes CUBA project and makes some estimations & notes in context of migration to the actual Jmix version.
```
./migration-advisor analyze-cuba --project-dir "C:/Path/To/Project/Directory" --base-package "com.company.myproject"
```
Command options:
* --project-dir (required) - path to target CUBA project root directory
* --base-package (required) - base package of your project
* --estimation-data-file (optional) - path to some external estimation data file. Default one will be used if paratemer is not specified. 
As a result command produces a report HTML file.

### Result Report Structure
#### General
Contains general information
Project directory
Amount entities
Amount of legacy entity listeners
Amount of screens
#### App Components
Contains information about application components (add-ons) present in project witn the notes about their availability in Jmix
> **NOTE**: For now there is no information on all add-ons currently available in marketplace.

#### Estimations
Contains rough estimation (in hours) of some activity categories
* Initial migration: basically related to actions within migration process (https://docs.jmix.io/jmix/cuba/index.html) which are not explicitly mentioned as separate category.
* Base entities: restoration of entities structure/creation of new base entities due to Jmix doesn't have any OOB base entities.
* Legacy listeners: refactoring of legacy entity listeners to use the actual approach
* Screens: Recreation of GUI screens due to CUBA screens are incompatible with Jmix 2+ and should be created from scratch. Going to be the most time-consuming activity on project with big amount of screens.

#### Screens Complexity
Contains more details about Screens estimation - all screens are divided into complexity groups with their own estimation.

#### UI Components Notes
Contains notes about UI components present in project which are absent or changed in Jmix.

#### Misc
Contains misc notes about some CUBA project functionality what should be taken into account

## Estimation Data
TODO

## Building the Distribution
To build the distribution locally run the following command to build the migration-advisor distribution:
```
./gradlew zipDist
```
or pass the "version" parameter if you want to build the distribution with the specific version:
```
./gradlew zipDist -Pversion=1.2.3
```

The task produces the `build/distributions/migration-advisor-<version>.zip` archive.
