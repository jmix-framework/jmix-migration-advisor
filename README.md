# Jmix Migration Advisor

## Overview

This tool helps in migrating projects from CUBA Platform to Jmix by providing some estimations and notes acquired via automatic project analysis.

> **IMPORTANT**: This is not a comprehensive estimation; certain aspects cannot be accurately estimated automatically and require manual analysis. Consider this data as a rough lower-range estimate.

> **IMPORTANT**: Analysis of Kotlin classes is currently not supported.

## Installation

Download the latest distribution (ZIP archive) from the [releases](https://github.com/jmix-framework/jmix-migration-advisor/releases) page.

Extract the archive, then open the `bin` subdirectory in terminal and use `migration-advisor` or `migration-advisor.bat` files for executing CLI commands.

## Usage

### Analyze CUBA Project

This command analyzes a CUBA project and makes some estimations & notes in context of migration to the actual Jmix version.

```
./migration-advisor analyze-cuba --project-dir "C:/Path/To/Project/Directory" --base-package "com.company.myproject"
```

Command options:
* `--project-dir` (required) - absolute path to target CUBA project root directory
* `--base-package` (required) - base package of your project
* `--estimation-data-file` (optional) - path to an external estimation data file. Default one will be used if the parameter is not specified. 

As a result, the command produces a report HTML file in the current folder.

### Result Report Structure

#### General

Contains general information:
- Project directory
- Number of entities
- Amount of legacy entity listeners
- Amount of screens

#### App Components

Contains information about application components (add-ons) present in project with the notes about their availability in Jmix.

> **NOTE**: The information on add-ons currently available in marketplace is incomplete.

#### Estimations

Contains rough estimation (in man-hours) of some activity categories:

* Initial migration: basically related to actions within migration process (https://docs.jmix.io/jmix/cuba/index.html) which are not explicitly mentioned as a separate category.
* Base entities: restoration of entities structure and creation of new base entities replicating the built-in CUBA base entities.
* Legacy listeners: refactoring of legacy entity listeners to use the modern approach.
* Screens: creation of UI screens. CUBA screens are incompatible with Jmix 2+ and should be created from scratch. This is going to be the most time-consuming activity on project with big amount of screens.

#### Screens Complexity

Contains more details about Screens estimation - all screens are divided into complexity groups with their own estimation.

#### UI Components Notes

Contains notes about UI components present in project which are absent or significantly changed in Jmix.

#### Misc

Contains miscellaneous notes about CUBA project functionality that should be taken into account.

## Estimation Data

Estimation weights for different metrics are stored in [estimation-data.xml](src/main/resources/estimation/estimation-data.xml). Description of metrics can be found in the file.

You can adjust the values by copying the file, changing values and providing new file via `--estimation-data-file` command option.

## Building Locally

To build the distribution locally, clone the repository and run the following command in the project root:

```
./gradlew zipDist
```

Pass the "version" parameter if you want to build the distribution with a specific version:

```
./gradlew zipDist -Pversion=1.2.3
```

The task produces the `build/distributions/migration-advisor-<version>.zip` archive.
