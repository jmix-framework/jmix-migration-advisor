<#-- Status pill badge. kind: compType | origin | uiType -->
<#macro badge value kind>
  <#if !value?? || value?length == 0><#return></#if>
  <#local cls = "neutral">
  <#switch kind>
    <#case "compType">
      <#switch value>
        <#case "MISSING"><#local cls = "bad"><#break>
        <#case "ADDON"><#case "TRANSLATION_ADDON"><#local cls = "info"><#break>
        <#default><#local cls = "neutral">
      </#switch>
      <#break>
    <#case "origin">
      <#switch value>
        <#case "EXTERNAL"><#local cls = "warn"><#break>
        <#case "FRAMEWORK"><#local cls = "info"><#break>
        <#default><#local cls = "neutral">
      </#switch>
      <#break>
    <#case "uiType">
      <#switch value>
        <#case "CHANGED"><#local cls = "ok"><#break>
        <#case "HAS_ALTERNATIVE"><#local cls = "info"><#break>
        <#case "HAS_WORKAROUND"><#local cls = "warn"><#break>
        <#case "ABSENT"><#local cls = "bad"><#break>
        <#default><#local cls = "neutral">
      </#switch>
      <#break>
  </#switch>
  <span class="badge ${cls}">${value?lower_case?replace("_", " ")?cap_first}</span>
</#macro>

<#-- Dependency of the primary replacement recipe (UiComponentIssue.requires) -->
<#macro requiresBadge req>
  <#local cls = "info">
  <#local label = "Add-on">
  <#switch req.kindName>
    <#case "COMMERCIAL_ADDON"><#local cls = "warn"><#local label = "Commercial add-on"><#break>
    <#case "THIRD_PARTY"><#local cls = "warn"><#local label = "3rd-party"><#break>
  </#switch>
  <span class="badge ${cls}" title="${req.subject}">${label}</span>
</#macro>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Migration report — ${projectName}</title>
<style>
  :root {
    color-scheme: light dark;
    --font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
    --font-mono: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
    --radius: 10px;
    --shadow: 0 1px 2px rgba(15,23,42,.06), 0 4px 12px rgba(15,23,42,.06);
    /* light theme (default — works in every browser with CSS custom properties) */
    --bg:#f6f7f9; --surface:#ffffff; --surface-2:#f1f3f5; --zebra:#f7f8fa; --border:#e3e6ea;
    --ink:#1f2430; --ink-soft:#5b6472;
    --accent:#4f46e5; --accent-2:#0ea5e9;
    --ok:#15803d;   --ok-bg:#e7f6ec;
    --warn:#b45309; --warn-bg:#fbf0dd;
    --bad:#b91c1c;  --bad-bg:#fbe6e6;
    --info:#0369a1; --info-bg:#e3f1fb;
    --neutral: var(--ink-soft); --neutral-bg: var(--surface-2);
  }
  @media (prefers-color-scheme: dark) {
    :root {
      --bg:#0f1115; --surface:#1a1d24; --surface-2:#232730; --zebra:#1f232b; --border:#2c313b;
      --ink:#e7e9ee; --ink-soft:#a3acba;
      --accent:#818cf8; --accent-2:#38bdf8;
      --ok:#4ade80;   --ok-bg:#14321f;
      --warn:#fbbf24; --warn-bg:#3a2c0c;
      --bad:#f87171;  --bad-bg:#3a1414;
      --info:#38bdf8; --info-bg:#0c2a3a;
    }
  }
  * { box-sizing: border-box; }
  body { margin: 0; font-family: var(--font-sans); color: var(--ink); background: var(--bg);
         line-height: 1.55; -webkit-font-smoothing: antialiased; }
  a { color: var(--accent); text-decoration: none; }
  a:hover { text-decoration: underline; }
  code, .mono { font-family: var(--font-mono); font-size: .86em; }

  /* hero */
  header.hero { background: linear-gradient(135deg, var(--accent), var(--accent-2));
                color: #fff; padding: 34px 24px 30px; }
  header.hero .inner { max-width: 1100px; margin: 0 auto; }
  header.hero .eyebrow { text-transform: uppercase; letter-spacing: .12em; font-size: .72rem; opacity: .85; }
  header.hero h1 { margin: 6px 0 4px; font-size: 1.9rem; font-weight: 700; }
  header.hero .meta { opacity: .9; font-size: .9rem; }

  /* layout + sticky toc */
  .layout { max-width: 1100px; margin: 0 auto; padding: 28px 24px 72px;
            display: grid; gap: 40px; grid-template-columns: 196px minmax(0,1fr); align-items: start; }
  nav.toc { position: sticky; top: 16px; font-size: .9rem; display: grid; gap: 2px; }
  nav.toc .toc-title { color: var(--ink-soft); text-transform: uppercase; letter-spacing: .06em;
                       font-size: .72rem; margin-bottom: 6px; }
  nav.toc a { color: var(--ink-soft); padding: 5px 10px; border-radius: 7px; border-left: 2px solid transparent; }
  nav.toc a:hover { background: var(--surface-2); color: var(--ink); text-decoration: none; }
  main { min-width: 0; display: grid; gap: 34px; }
  section { scroll-margin-top: 16px; }
  h2 { font-size: 1.2rem; margin: 0 0 14px; display: flex; align-items: baseline; gap: 10px; }
  h2 .count { font-size: .8rem; color: var(--ink-soft); font-weight: 500; }

  /* callout */
  .callout { background: var(--info-bg); border: 1px solid var(--border); border-left: 3px solid var(--info);
             color: var(--ink); padding: 12px 16px; border-radius: var(--radius); font-size: .9rem; }
  .callout + .callout { margin-top: 10px; }

  /* kpi cards */
  .cards { display: grid; gap: 16px; grid-template-columns: repeat(auto-fit, minmax(168px,1fr)); }
  .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius);
          padding: 16px 18px; box-shadow: var(--shadow); }
  .card .label { color: var(--ink-soft); font-size: .74rem; text-transform: uppercase; letter-spacing: .04em; }
  .card .num { font-size: 2rem; font-weight: 700; margin-top: 4px; font-variant-numeric: tabular-nums; }
  .card .sub { color: var(--ink-soft); font-size: .8rem; margin-top: 2px; }
  .card.accent { border-top: 3px solid var(--accent); }

  /* tables */
  .table-wrap { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius);
                box-shadow: var(--shadow); overflow: hidden; }
  table { width: 100%; border-collapse: collapse; font-size: .92rem; }
  thead th { background: var(--surface-2); color: var(--ink-soft);
             text-align: left; font-weight: 600; font-size: .76rem; text-transform: uppercase;
             letter-spacing: .03em; padding: 10px 14px; border-bottom: 1px solid var(--border); }
  tbody td { padding: 10px 14px; border-bottom: 1px solid var(--border); vertical-align: top; }
  tbody tr:last-child td { border-bottom: 0; }
  tbody tr:nth-child(even) { background: var(--zebra); }
  td.num, th.num { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; }
  tfoot td, tfoot th { padding: 10px 14px; border-top: 2px solid var(--border); font-weight: 700; background: var(--surface-2); }
  .empty { padding: 18px 14px; color: var(--ink-soft); font-style: italic; }

  /* badges & chips */
  .badge { display: inline-flex; align-items: center; gap: 6px; padding: 2px 10px; border-radius: 999px;
           font-size: .78rem; font-weight: 600; line-height: 1.7; white-space: nowrap; }
  .badge::before { content: ""; width: 7px; height: 7px; border-radius: 50%; background: currentColor; }
  .badge.ok{color:var(--ok);background:var(--ok-bg)}   .badge.warn{color:var(--warn);background:var(--warn-bg)}
  .badge.bad{color:var(--bad);background:var(--bad-bg)} .badge.info{color:var(--info);background:var(--info-bg)}
  .badge.neutral{color:var(--neutral);background:var(--neutral-bg)}
  .chip { display: inline-block; padding: 1px 8px; border-radius: 6px; background: var(--surface-2);
          border: 1px solid var(--border); font-family: var(--font-mono); font-size: .76rem; color: var(--ink-soft); }
  .legend { margin-top: 10px; display: flex; flex-wrap: wrap; gap: 8px 18px;
            font-size: .82rem; color: var(--ink-soft); }
  .legend > span { display: inline-flex; align-items: center; gap: 6px; }
  .legend > .note { flex-basis: 100%; display: block; }

  /* stacked effort bar */
  .stack { display: flex; height: 30px; border-radius: 8px; overflow: hidden;
           border: 1px solid var(--border); background: var(--surface-2); margin-bottom: 14px; }
  .stack > span { min-width: 3px; }
  .seg-1{background:var(--accent)} .seg-2{background:var(--accent-2)}
  .seg-3{background:var(--warn)}   .seg-4{background:var(--ok)}
  .swatch { display:inline-block; width:11px; height:11px; border-radius:3px; margin-right:7px; vertical-align:middle; }

  /* per-row mini bar */
  .minibar { position: relative; height: 8px; border-radius: 4px; background: var(--surface-2);
             min-width: 80px; overflow: hidden; }
  .minibar > span { position:absolute; inset:0 auto 0 0; background: var(--accent); border-radius: 4px; }

  /* drill-down */
  details { margin-top: 6px; }
  details > summary { cursor: pointer; color: var(--accent); font-size: .82rem; list-style: none; }
  details > summary::-webkit-details-marker { display: none; }
  details > summary::before { content: "▸ "; }
  details[open] > summary::before { content: "▾ "; }
  details .drill { margin: 8px 0 2px; padding-left: 4px; display: flex; flex-wrap: wrap; gap: 6px; }
  details .drill .mono { background: var(--surface-2); border: 1px solid var(--border); border-radius: 6px; padding: 2px 7px; }

  .toolbar { display: flex; justify-content: flex-end; max-width: 1100px; margin: 0 auto; padding: 12px 24px 0; }
  .toolbar button { font: inherit; font-size: .82rem; color: var(--ink-soft); background: var(--surface);
                    border: 1px solid var(--border); border-radius: 7px; padding: 5px 12px; cursor: pointer; }
  .toolbar button:hover { color: var(--ink); }
  footer { max-width: 1100px; margin: 0 auto; padding: 0 24px 48px; color: var(--ink-soft); font-size: .8rem; }

  @media (max-width: 820px) {
    .layout { grid-template-columns: 1fr; gap: 26px; }
    nav.toc { position: static; display: none; }
  }
  @media print {
    :root { color-scheme: light; }
    body { background: #fff; }
    .toolbar, nav.toc { display: none !important; }
    .layout { display: block; padding: 0 8px; max-width: none; }
    main { gap: 22px; }
    header.hero { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    .card, .table-wrap, .stack, .badge, .minibar > span, .seg-1,.seg-2,.seg-3,.seg-4 {
      -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    thead { display: table-header-group; }
    tr, .card, details { break-inside: avoid; }
    section { break-inside: avoid-page; }
    details { open: open; }
    details > summary { display: none; }
    details .drill { display: flex !important; }
  }
</style>
</head>
<body>

<header class="hero">
  <div class="inner">
    <div class="eyebrow">CUBA → Jmix migration report</div>
    <h1>${projectName}</h1>
    <div class="meta">Generated ${generatedAt} · rough lower-bound estimate</div>
  </div>
</header>

<div class="toolbar"><button type="button" id="toggle-all">Expand all details</button></div>

<div class="layout">
  <nav class="toc">
    <div class="toc-title">Sections</div>
    <a href="#overview">Overview</a>
    <a href="#estimations">Estimations</a>
    <a href="#screens">Screens complexity</a>
    <a href="#ui">UI components</a>
    <a href="#app-components">App components</a>
    <a href="#data-model">Data model</a>
    <a href="#misc">Misc notes</a>
    <#if unparsedFiles?has_content><a href="#unparsed">Not analyzed</a></#if>
  </nav>

  <main>
    <section id="overview">
      <h2>Overview</h2>
      <div class="cards">
        <div class="card accent">
          <div class="label">Total effort</div>
          <div class="num">${totalEstimation}</div>
          <div class="sub">man-hours (lower bound)</div>
        </div>
        <div class="card">
          <div class="label">Entities</div>
          <div class="num">${entitiesAmount}</div>
        </div>
        <div class="card">
          <div class="label">Screens</div>
          <div class="num">${screensTotalAmount}</div>
          <div class="sub">${screensTotalHours} man-hours</div>
        </div>
        <div class="card">
          <div class="label">App components</div>
          <div class="num">${appComponentsAmount}</div>
          <div class="sub"><#if (missingAppComponentsAmount > 0)>${missingAppComponentsAmount} without Jmix data<#else>all recognized</#if></div>
        </div>
        <div class="card">
          <div class="label">Legacy listeners</div>
          <div class="num">${legacyListenersAmount}</div>
        </div>
      </div>
      <div class="callout" style="margin-top:18px">
        This is not a comprehensive estimation. Some aspects cannot be evaluated automatically and need manual analysis — treat the numbers as a rough lower-range estimate. Analysis of Kotlin classes is not supported.
      </div>
    </section>

    <section id="estimations">
      <h2>Estimations <span class="count">${totalEstimation} man-hours total</span></h2>
      <div class="stack" role="img" aria-label="Effort distribution by category">
        <#list estimationItems as e>
          <span class="seg-${e?index % 4 + 1}" style="flex-grow:${e.estimation?c}" title="${e.category}: ${e.estimation} h"></span>
        </#list>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Category</th><th class="num">Man-hours</th><th class="num">Share</th></tr></thead>
          <tbody>
            <#list estimationItems as e>
              <tr>
                <td><span class="swatch seg-${e?index % 4 + 1}"></span>${e.category}</td>
                <td class="num">${e.estimation}</td>
                <td class="num"><#if (totalEstimation > 0)>${(e.estimation * 100 / totalEstimation)?string("0")}%<#else>—</#if></td>
              </tr>
            </#list>
          </tbody>
          <tfoot><tr><th>Total</th><td class="num">${totalEstimation}</td><td class="num">100%</td></tr></tfoot>
        </table>
      </div>
    </section>

    <section id="screens">
      <h2>Screens complexity <span class="count">${screensTotalAmount} screens · ${screensTotalHours} man-hours</span></h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Group</th><th class="num">Screens</th><th class="num">Cost, h</th><th class="num">Total, h</th><th>Weight</th></tr></thead>
          <tbody>
            <#list screenComplexityGroups as group>
              <tr>
                <td>
                  ${group.name}
                  <#if group.screens?has_content>
                    <details>
                      <summary>${group.screens?size} screen<#if group.screens?size != 1>s</#if></summary>
                      <div class="drill"><#list group.screens as s><span class="mono">${s}</span></#list></div>
                    </details>
                  </#if>
                </td>
                <td class="num">${group.amount}</td>
                <td class="num">${group.cost}</td>
                <td class="num">${group.total}</td>
                <td>
                  <#assign wpct = 0>
                  <#if (screensMaxGroupTotal > 0)><#assign wpct = (group.total * 100 / screensMaxGroupTotal)></#if>
                  <div class="minibar" title="${group.total} h"><span style="width:${wpct?string("0.#")}%"></span></div>
                </td>
              </tr>
            </#list>
          </tbody>
          <tfoot><tr><th>Total</th><td class="num">${screensTotalAmount}</td><td></td><td class="num">${screensTotalHours}</td><td></td></tr></tfoot>
        </table>
      </div>
      <#if screensRequireDecision?has_content>
      <div class="callout" style="margin-top:14px">
        <strong>Requires decision:</strong> ${screensRequireDecision?size} screen<#if screensRequireDecision?size != 1>s</#if>
        contain<#if screensRequireDecision?size == 1>s</#if> components with no Jmix equivalent (marked "Absent").
        The replacement cost of such components is not included in the estimations above: decide per screen whether
        to drop the functionality, redesign it, or build a custom component.
        <details>
          <summary>Show screens</summary>
          <div class="drill">
            <#list screensRequireDecision as screenName, components>
              <span class="mono">${screenName}: ${components?join(", ")}</span>
            </#list>
          </div>
        </details>
      </div>
      </#if>
    </section>

    <section id="ui">
      <h2>UI components <span class="count">${uiComponentNotes?size} noted</span></h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Component</th><th class="num">Used</th><th>Status</th><th>Notes</th></tr></thead>
          <tbody>
            <#if uiComponentNotes?has_content>
              <#list uiComponentNotes as note>
                <tr>
                  <td><code>${note.name}</code><#if (note.extraComplexityScore > 0)> <span class="chip" title="extra complexity score">+${note.extraComplexityScore}</span></#if></td>
                  <td class="num">${note.amount}</td>
                  <td>
                    <#if note.type??><@badge note.type "uiType"/></#if>
                    <#list note.requires as req> <@requiresBadge req/></#list>
                  </td>
                  <td>${note.notes}</td>
                </tr>
              </#list>
            <#else>
              <tr><td colspan="4" class="empty">No noteworthy UI components found.</td></tr>
            </#if>
          </tbody>
        </table>
      </div>
      <div class="legend">
        <span><span class="badge ok">Changed</span> direct analog with renames or minor differences; mechanical XML/code edit</span>
        <span><span class="badge info">Has alternative</span> a different ready-made component achieves the same or similar result</span>
        <span><span class="badge warn">Has workaround</span> achievable partially or with custom glue code following a known recipe</span>
        <span><span class="badge bad">Absent</span> no recipe: drop the functionality, redesign, or build from scratch (not included in complexity scores)</span>
        <span><span class="chip">+N</span> extra complexity score added to each screen using the component</span>
        <span class="note">Components not listed here have a direct Jmix equivalent. Dependency badges (Add-on, Commercial add-on, 3rd-party) refer to the primary replacement recipe; simpler fallbacks, if any, are described in the notes.</span>
      </div>
    </section>

    <section id="app-components">
      <h2>App components <span class="count">${appComponentsAmount} found</span></h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Name</th><th>Type</th><th>Origin</th><th>Notes</th></tr></thead>
          <tbody>
            <#if appComponents?has_content>
              <#list appComponents as appComponent>
                <tr>
                  <td>${appComponent.name}<#if appComponent.appComponentPackage?? && appComponent.appComponentPackage != appComponent.name><br><span class="chip">${appComponent.appComponentPackage}</span></#if></td>
                  <td><@badge appComponent.appComponentTypeName "compType"/></td>
                  <td><@badge appComponent.originName "origin"/></td>
                  <td>${appComponent.notes}</td>
                </tr>
              </#list>
            <#else>
              <tr><td colspan="4" class="empty">No application components detected.</td></tr>
            </#if>
          </tbody>
        </table>
      </div>
    </section>

    <section id="data-model">
      <h2>Data model <span class="count">${entitiesAmount} entities</span></h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Persistence unit</th><th class="num">Entities</th></tr></thead>
          <tbody>
            <#if entitiesPerPersistenceUnit?? && entitiesPerPersistenceUnit?size gt 0>
              <#list entitiesPerPersistenceUnit as unit, entities>
                <tr>
                  <td>
                    <code>${unit}</code>
                    <#if entities?has_content>
                      <details>
                        <summary>${entities?size} entit<#if entities?size != 1>ies<#else>y</#if></summary>
                        <div class="drill"><#list entities as ent><span class="mono">${ent}</span></#list></div>
                      </details>
                    </#if>
                  </td>
                  <td class="num">${entities?size}</td>
                </tr>
              </#list>
            <#else>
              <tr><td colspan="2" class="empty">No persistence units detected.</td></tr>
            </#if>
          </tbody>
        </table>
      </div>
      <#if legacyListeners?has_content>
        <details style="margin-top:14px">
          <summary>${legacyListenersAmount} legacy entity listener<#if legacyListenersAmount != 1>s</#if></summary>
          <div class="drill"><#list legacyListeners as l><span class="mono">${l}</span></#list></div>
        </details>
      </#if>
    </section>

    <section id="misc">
      <h2>Misc notes <span class="count">${miscNotes?size}</span></h2>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Item</th><th>Notes</th></tr></thead>
          <tbody>
            <#if miscNotes?has_content>
              <#list miscNotes as row>
                <tr>
                  <td>${row.name}<#if row.code?? && row.code?length gt 0><br><span class="chip">${row.code}</span></#if></td>
                  <td>${row.notes}</td>
                </tr>
              </#list>
            <#else>
              <tr><td colspan="2" class="empty">Nothing to report.</td></tr>
            </#if>
          </tbody>
        </table>
      </div>
    </section>

    <#if unparsedFiles?has_content>
    <section id="unparsed">
      <h2>Not analyzed <span class="count">${unparsedFiles?size} file<#if unparsedFiles?size != 1>s</#if></span></h2>
      <div class="callout" style="margin-bottom:14px">
        These files could not be parsed and are excluded from all metrics and estimations,
        so the numbers above are underestimated. Review the files manually.
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>File</th><th>Reason</th></tr></thead>
          <tbody>
            <#list unparsedFiles as unparsedFile>
              <tr>
                <td><span class="mono">${unparsedFile.path}</span></td>
                <td>${unparsedFile.reason}</td>
              </tr>
            </#list>
          </tbody>
        </table>
      </div>
    </section>
    </#if>
  </main>
</div>

<footer>Generated by Jmix Migration Advisor on ${generatedAt}.</footer>

<script>
  (function () {
    var btn = document.getElementById("toggle-all");
    if (!btn) return;
    btn.addEventListener("click", function () {
      var items = document.querySelectorAll("details");
      var anyClosed = false;
      items.forEach(function (d) { if (!d.open) anyClosed = true; });
      items.forEach(function (d) { d.open = anyClosed; });
      btn.textContent = anyClosed ? "Collapse all details" : "Expand all details";
    });
  })();
</script>
</body>
</html>
