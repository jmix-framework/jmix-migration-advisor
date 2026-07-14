# Jmix Migration Advisor

## Overview

This tool helps in migrating projects to the current Jmix version by providing estimations and notes acquired via automatic project analysis. Two source platforms are supported:

* **CUBA Platform 7.2** projects: the `analyze-cuba` command;
* **Jmix 1.x (classic UI)** projects: the `analyze-jmix` command.

Both commands produce an HTML report with an effort estimation (in man-hours), an inventory of things that will change, and an explicit list of items that cannot be priced automatically and require a human decision.

> **IMPORTANT**: This is not a comprehensive estimation; certain aspects cannot be accurately estimated automatically and require manual analysis. Estimation weights are expert-set and NOT calibrated against completed migrations yet. Consider the numbers a rough lower-range estimate.

> **IMPORTANT**: Analysis of Kotlin classes is currently not supported. Kotlin files are counted and reported, but their content is not analyzed.

## Installation

Download the latest distribution (ZIP archive) from the [releases](https://github.com/jmix-framework/jmix-migration-advisor/releases) page.

Extract the archive, then open the `bin` subdirectory in terminal and use `migration-advisor` or `migration-advisor.bat` files for executing CLI commands.

## Usage

Both commands validate the project type first: `analyze-cuba` refuses Jmix projects and vice versa, so running the wrong command tells you which one to use instead. As a result, each command produces a `results_<timestamp>.html` report file in the current folder.

### Analyze CUBA Project

Analyzes a CUBA 7.2 project in the context of migration to the current Jmix version:

```
./migration-advisor analyze-cuba --project-dir "C:/Path/To/Project/Directory" --base-package "com.company.myproject"
```

Command options:
* `--project-dir` (required) - absolute path to the CUBA project root directory (the one containing `modules/global`, `modules/core`, `modules/web`);
* `--base-package` (required) - base package of the project, used to scope source scanning;
* `--estimation-data-file` (optional) - path to an external estimation data file overriding the default weights (see [Estimation Data](#estimation-data)).

### Analyze Jmix Project

Analyzes a Jmix 1.x (classic UI) project in the context of migration to the current Jmix version (Flow UI):

```
./migration-advisor analyze-jmix --project-dir "C:/Path/To/Project/Directory"
```

Command options:
* `--project-dir` (required) - absolute path to the Jmix project root directory (the one containing `build.gradle`);
* `--base-package` (optional) - base package of the project; when omitted, it is resolved automatically from the `@SpringBootApplication` class;
* `--jmix-version` (optional) - Jmix version override for builds where the version cannot be read from `build.gradle` (e.g. it is set via a placeholder variable);
* `--estimation-data-file` (optional) - path to an external estimation data file overriding the default weights.

## Reading the Report

The report leads with numbers, but the numbers are only half of the story. Read it as three answers:

1. **How much routine work is there?** The hours in *Overview* and *Estimations* cover work with a known recipe: rewriting screens, renaming dependencies, sweeping namespaces. This is a lower bound.
2. **What requires a decision?** Items marked *Requires decision* (screens with absent or custom components, add-ons with no equivalent) are NOT included in the hours: their cost is indeterminate until someone decides whether to drop, replace or re-implement the functionality.
3. **What requires manual estimation?** *Red flags* (custom Vaadin code, JavaScript components, custom themes) are inventoried but not priced; estimate them manually.

### Report Sections

**Overview** - KPI cards: total effort (accented), entities, screens, add-ons / app components, roles, red flags. Sub-lines show escalation counters (e.g. "1 absent · 1 without Jmix data").

**Estimations** - man-hours per activity category with a distribution bar:

* CUBA report: *Initial migration* (base migration steps from the [migration guide](https://docs.jmix.io/jmix/cuba/index.html)), *Base entities* (re-creation of built-in CUBA base entities), *Legacy listeners* (refactoring of legacy entity listeners), *Screens*.
* Jmix report: *Initial migration* (Gradle/Spring Boot/main and login views), *Jakarta namespace sweep* (javax to jakarta, mostly automated by the IDE or OpenRewrite), *Screens*, *Add-ons* (dependency-level work), *Security roles* (annotation and policy updates), *Configuration*, *Custom themes* (SCSS to CSS on top of Lumo or Aura).

**Screens complexity** - the breakdown behind the *Screens* category. Each screen gets a complexity score (see [docs/design.md](docs/design.md) for the model) and falls into a group (Trivial, Simple, Medium, Complex, Hard) with fixed hours per group. The *Requires decision* callout below the table lists screens containing components with no equivalent; such screens are still priced for their ordinary part, but the absent components are on top of that.

**UI components** - components used in the project that change during migration, with usage counts and per-screen extra complexity scores (`+N` chips). Status badges:

* *Changed* - direct analog with renames or minor differences; a mechanical XML/code edit;
* *Has alternative* - a different ready-made component achieves the same or similar result;
* *Has workaround* - achievable partially or with custom glue code following a known recipe;
* *Absent* - no recipe: drop the functionality, redesign, or build from scratch (escalated, not scored);
* *Custom* - a component from a non-standard namespace, i.e. a project custom component (escalated, not scored).

Dependency badges (*Add-on*, *Commercial add-on*, *3rd-party*) refer to the primary replacement recipe; simpler fallbacks, if any, are described in the notes. Components not listed have a direct equivalent in the target version.

**App components** (CUBA) / **Add-ons** (Jmix) - project dependencies with their status in the target version: *Available*, *Renamed* (with the replacement artifact), *Replaced*, *Merged*, *Absent*, *Unknown* (no registry data - check the marketplace). A *Commercial* badge marks add-ons requiring a commercial subscription. Add-ons with no equivalent are escalated in a *Requires decision* callout. The Jmix report also shows dependency-level cost hints in hours; screen/code rework driven by an add-on is estimated in the screens section instead.

**Red flags** (Jmix report) - artifacts that cannot be migrated automatically: direct Vaadin 8 API usage, JavaScript components, composite components, custom SCSS themes. Not included in the hours (except a flat per-theme re-creation cost in the *Custom themes* category).

**Renames** (Jmix report) - `application.properties` keys that are renamed or change meaning in the current Jmix.

**Data model**, **Misc notes** - inventory sections: entities per persistence unit, legacy listeners, configuration facts.

**Not analyzed** - files that could not be parsed (broken or unsupported syntax). The analysis continues past them, but their content is not reflected in the numbers.

## Estimation Data

Estimation weights are stored in per-platform profiles:

* [estimation-data-cuba.xml](src/main/resources/estimation/estimation-data-cuba.xml) for `analyze-cuba`;
* [estimation-data-jmix.xml](src/main/resources/estimation/estimation-data-jmix.xml) for `analyze-jmix`.

Each unit is described in the file itself. You can adjust the values by copying the profile, changing values and providing the new file via the `--estimation-data-file` command option. The override is partial: units missing from your file fall back to the profile defaults.

## Registries

Platform knowledge lives in XML registries under [src/main/resources/registries](src/main/resources/registries), not in code:

* `ui-component-issues.xml` - classic UI components changed, replaced or absent in Flow UI;
* `cuba-app-components.xml` - known CUBA app components and their availability in Jmix;
* `jmix-addons.xml` - Jmix 1.x add-on starters and their status in the current Jmix;
* `jmix-config-renames.xml` - renamed `application.properties` keys.

An unknown dependency or component is a valid state: the report shows it as "no data, check manually" rather than guessing. Registry formats are documented in the file headers; `RegistryConsistencyTest` guards the structural invariants.

## Design

The overall architecture, the estimation model and the principles behind the tool are described in [docs/design.md](docs/design.md).

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
