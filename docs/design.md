# Jmix Migration Advisor: Design

This document describes the internal structure of the tool and the principles behind it.
For usage and report reading, see the [README](../README.md).

## 1. Goals and Scope

The tool automates the first step of a migration project: producing a defensible lower-bound
effort estimate together with an inventory of everything that will change. It supports two
source platforms migrating to the current (Flow UI based) Jmix version:

* CUBA Platform 7.2 (`analyze-cuba`);
* Jmix 1.x with classic UI (`analyze-jmix`).

Non-goals: the tool does not migrate code, does not produce a fixed-price quote, and does not
try to guess the cost of work it cannot see (custom client-side code, business decisions).

## 2. Principles

**Hours vs decisions.** Only work with a known recipe is priced in man-hours: rewriting a
screen, renaming a dependency, sweeping a namespace. Anything whose cost is indeterminate is
NOT silently priced with a made-up number; it is escalated in the report as *Requires
decision* (or as a *Red flag*) and excluded from the hours. Three escalation sources share
this mechanism: UI components with `absent` status, components from custom namespaces, and
add-ons with no equivalent in the target version. The rationale: the cost of an absent
feature ranges from zero (dropped) to a re-implementation project; averaging that into hours
would only hide the risk.

**Registries as data.** Platform knowledge (which component maps to what, which add-on is
available) lives in XML resources under `src/main/resources/registries`, not in code. This
keeps the knowledge reviewable, diff-able and maintainable by non-developers. An unknown
artifact is a valid state: the report says "no data, check manually" instead of guessing.
Structural invariants of the registries are enforced twice: at load time (fail-fast parsing)
and by `RegistryConsistencyTest` (key entries present, forbidden combinations absent,
suspicious shrinks detected) - the registries are hand-edited, and the test protects against
accidental data loss.

**Degrade gracefully.** A real project always contains something unparsable. Analyzers skip
broken files with a warning and report them in the *Not analyzed* section; a single broken
file must never abort the analysis or silently disappear.

**Determinism.** Collections in the report are sorted, timestamps are isolated, and the HTML
output is stable. This makes golden-file snapshot testing possible (see § 8).

**Shared core, thin platform layers.** CUBA 7.2 UI and Jmix 1.x classic UI are almost the
same technology (Vaadin 8 based, same descriptor structure, same component set). The parsing
and estimation logic for it is written once and parameterized by platform profile.

## 3. Architecture

Package map (`io.jmix.migration.*`):

```
cli         JCommander commands: analyze-cuba, analyze-jmix
core        platform-agnostic building blocks:
  estimation  two-stage estimation engine (metrics, scores, thresholds, data provider)
  incident    UI component issue model + registry (taxonomy, factories, prefix matching)
  model       shared enums and values: License, Origin, TargetStatus, UnparsedFileEntry
  project     build fingerprinting: GradleBuildParser, JmixProjectDescriptor,
              ProjectType, ProjectTypeDetector, JmixModule
  report      report model: ReportModel + typed sections, HtmlReportWriter (FreeMarker)
  scan        parsing utilities: XML (XXE-hardened), JavaParser base, properties,
              unparsed-files collector
classicui   the shared classic-UI layer used by BOTH platforms:
  model       UiUnitInfo, Layout, ScreenData, controller details
  parser      descriptor/layout/data/facets/controller parsers, ScreensCollector,
              ScreenClassProfile (cuba() / jmixClassic() base-class FQNs)
  estimation  ScreenEstimator + per-metric scoring rules
cuba        CUBA-specific: module analyzers (global/core/web), web-screens.xml,
            persistence.xml, app components registry, report generator
jmix        Jmix 1.x specific: screen/entity/sources/config analyzers, add-ons
            registry, JmixEstimator, report generator, BasePackageResolver
```

The dependency rule: `core` imports nothing platform-specific; `classicui` depends only on
`core`; `cuba` and `jmix` depend on both but not on each other.

The classic-UI layer is parameterized in two points: `ScreenClassProfile` carries the
platform base-class names for controller recognition (CUBA legacy and Screens API classes vs
`io.jmix.ui.screen.*`), and the descriptor parser checks the expected XML namespace
(`http://jmix.io/schema/ui/window` for Jmix 1.x). CUBA-only branches (legacy screens
registered in `web-screens.xml`, `dsContext` data sources) simply never fire on Jmix input.

## 4. Command Pipelines

Both commands follow the same shape:

```
fingerprint -> validate type -> analyze -> estimate -> build report model -> render HTML
```

1. **Fingerprint**: `GradleBuildParser` extracts facts from the build scripts (plugin and BOM
   versions, dependencies, module layout) tolerantly - regex-based, no Gradle execution.
   `ProjectTypeDetector` classifies the project (CUBA / JMIX_CLASSIC / JMIX_FLOW / UNKNOWN)
   using build signals and, for CUBA, the `modules/{global,web}` directory layout.
2. **Validate**: the validation is symmetric - each command refuses projects of the other
   type with a message naming the right command.
3. **Analyze**: platform analyzers walk the sources and resources and produce facts:
   * CUBA: global/core/web module analyzers (entities, listeners, screens via
     `web-screens.xml` + descriptors + controllers, app components from `web.xml`);
   * Jmix: screens via `@UiController`/`@UiDescriptor` annotations and descriptor namespace,
     entities via JavaParser AST (`@JmixEntity`, `@Entity`, `EnumClass`, listeners), a
     lightweight text scan for red flags / security artifacts / Kotlin files, configuration
     analysis (property renames, menu, Liquibase), add-on resolution against the registry.
4. **Estimate**: see § 5.
5. **Report**: the generator builds a `ReportModel` from typed sections; `HtmlReportWriter`
   renders it with a FreeMarker template (escaped output, `Locale.US`, `0.##` numbers).

## 5. Estimation Model

Screen estimation is two-stage:

```
screen facts -> numeric metrics -> complexity score -> complexity group -> hours
```

**Stage 1: score.** For every screen, `ScreenEstimator` collects numeric metrics and
converts them to a score using per-metric rules:

* per-component extra scores from `ui-component-issues.xml` (a screen using `groupTable`
  gets +3, `popupView` +5, etc.);
* structural metrics: nested data containers, `uiComponents.create()` calls in the
  controller, descriptor inheritance (`extends`);
* controller size: the amount of method calls, mapped through thresholds (L1..L5) to a score.

Components with `absent` status and custom-namespace components contribute NO score; they
put the screen on the *Requires decision* list instead.

**Stage 2: hours.** The score maps through threshold bands to a complexity group (Trivial,
Simple, Medium, Complex, Hard), and each group has a fixed hour value. Coarse bands are a
feature: they absorb the noise of the score model and make the result explainable ("this
screen is Medium because of X and Y").

**Project-level categories.** On top of screens, each platform adds flat or counted
categories: CUBA - initial migration, base entities, legacy listeners; Jmix - initial
migration, jakarta sweep (thresholds by affected file count), add-on cost hints, security
roles, configuration, custom themes. Red flags are deliberately NOT converted to hours.

**Profiles.** All weights live in `estimation/estimation-data-{cuba,jmix}.xml`. The
`EstimationDataProvider` resolves units lazily by name, so each profile carries only the
units its platform uses. The Jmix profile weights are lower than CUBA ones (rewriting a
Screens-API screen to Flow is cheaper than rewriting a legacy CUBA screen). A user-provided
file (`--estimation-data-file`) partially overrides the profile: missing units fall back.

**Calibration status.** The weights are expert-set and not yet calibrated against completed
migrations. Treat all hour values as a lower-range estimate; the calibration track (compare
predicted vs actual hours on finished projects, adjust profiles) is future work.

## 6. Registries

Four registries, all with a documented header comment and load-time validation.

**`ui-component-issues.xml`** - classic UI components with migration issues. The taxonomy:

| Status | Meaning | Scored |
|---|---|---|
| *(no entry)* | direct equivalent in Flow UI | no |
| `changed` | rename or minor differences, mechanical edit | yes |
| `has-alternative` | a different ready-made component covers it | yes |
| `has-workaround` | custom glue code following a known recipe | yes |
| `absent` | no recipe; cost indeterminate | no - escalated |

Entries are created through closed factories (`UiComponentIssue.createChanged/...`), which
makes illegal states unrepresentable: an `absent` entry cannot carry a score or a
`requires` dependency, and the loader rejects such XML with a message pointing at the entry.
`requires` (0..n, a conjunction) declares what the primary recipe depends on
(add-on / commercial add-on / third-party); it is non-strict - a simpler fallback may exist
and is described in the notes. Namespaced add-on families are covered by prefix entries
(`match="chart:*"`). The prefix written in a descriptor is a per-file convention, so
families declare their namespace URIs (`uri` elements) and the layout parser canonicalizes
component names by the element URI: `charts:serialChart` and `ch:pieChart` both become
`chart:...` when the URI belongs to the charts family. A component written with a family
prefix over a foreign URI is not that family: it gets a Clark-notation name
(`{uri}localName`) and is escalated as custom. A prefixed component with no registry match
is treated as a project custom component and escalated with the synthetic `CUSTOM` status;
families without declared URIs keep conventional prefix matching.

**`jmix-addons.xml`** and **`cuba-app-components.xml`** - dependencies of the source project
(Gradle starters for Jmix, app components from `web.xml` for CUBA). The formats mirror each
other with three orthogonal axes plus a status:

* `category` - structural role (core / data / ui / addon / translation; CUBA adds base-app
  and theme). The UI part of an add-on is just `addon`: the axis does not encode licensing;
* `license` - open-source / commercial (rendered as a *Commercial* badge);
* `origin` - framework / community;
* status (`flow-status` / `status`) - the shared `TargetStatus` enum: available, renamed
  (Jmix only, with the replacement artifact), replaced, merged, absent. Absent entries are
  escalated in the report.

Add-on `cost-hint` covers only dependency-level work (0 = rename); screen and code rework
driven by an add-on is estimated through the component weights instead, so nothing is
counted twice.

A note on coordinates: artifact groups are stable across Jmix versions (`io.jmix.charts`),
while the Java base packages of flow modules may differ (`io.jmix.chartsflowui`). Registry
`flow-artifact` values are Maven coordinates only; packages appear only in notes about code.

**`jmix-config-renames.xml`** - `application.properties` keys renamed or changed in meaning.

## 7. Report Model

The report is a `ReportModel` with an ordered list of typed sections (`OverviewSection`,
`EstimationSummarySection`, `ComplexityGroupsSection`, `UiComponentsSection`,
`AddonsSection`, `AppComponentsSection`, `RedFlagsSection`, ...). The FreeMarker template is
a skeleton that dispatches on `section.type` to a macro per section type; the table of
contents is generated from the model. Sections shared by both platforms are built by shared
factories (`UiComponentsSection.fromComponentCounters`,
`ComplexityGroupsSection.fromScreensPerComplexity`), so the two reports cannot drift apart
in the shared parts. Generators only assemble data; all HTML lives in the template.

## 8. Testing Strategy

Three complementary layers:

1. **Golden snapshots** (`ReportSnapshotTest`): four fixture projects
   (`src/test/resources/projects`: `cuba-features`, `cuba-robustness`, `jmix17-minimal`,
   `jmix17-features`) are analyzed end-to-end and the full HTML is compared against goldens
   (`src/test/resources/goldens`) after normalization (timestamp, absolute paths, path
   separators, line endings). Regenerate with `./gradlew test -Dsnapshot.update=true`.
   Goldens pin *everything the tool currently produces*, including badges and escalations.
2. **Expectation ledgers** (`EstimationExpectationsTest`, `JmixProjectExpectationsTest`):
   hand-derived numbers for the fixtures (per-screen score arithmetic in the javadoc,
   group membership, totals). Unlike goldens, they encode *intent*: when a golden
   regeneration changes a number, the ledger explains which expectation broke. Fixtures are
   built with values at threshold boundaries to catch off-by-one drift.
3. **Registry consistency** (`RegistryConsistencyTest`): see § 2.

Reference projects for manual smoke runs (not in CI): the CUBA `sampler` (306 screens, the
component universe of CUBA), `jmix-v1-ui-samples` (335 screens, the component universe of
classic Jmix), and `jmix-ui-samples` (the current-Jmix sampler - never parsed, used as the
target-side reference to verify that recommended replacements actually exist).

## 9. Known Limitations

* Estimation weights are not calibrated against real migrations (see § 5).
* Kotlin sources are counted but not analyzed.
* Only Groovy `build.gradle` is parsed; Kotlin DSL (`build.gradle.kts`) is not supported yet.
* Controller analysis counts method calls without symbol resolution; call-site precision
  (which API is actually used) would require the JavaParser symbol solver.
* Jmix 2.x sources are not supported as a migration source.
