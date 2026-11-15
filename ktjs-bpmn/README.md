# ktjs-bpmn (module)

Kotlin/JS wrapper for [`bpmn-js`](https://www.npmjs.com/package/bpmn-js) 18.31.0 — the BPMN 2.0
viewer and modeler.

One npm package, one Gradle module, one Kotlin package (`io.github.shijianjs.ktjs.bpmn`) with a
`core` sub-package for the options, result types and model helpers.

## Installation

```kotlin
dependencies {
    implementation("io.github.shijianjs:ktjs-bpmn:18.31.0-kt.1")
}
```

The page also needs the stylesheets and icon font that ship inside the npm package
(`bpmn-js/dist/assets/{diagram-js.css,bpmn-js.css,bpmn-font/}`). The demo copies them out of
`node_modules` with a Gradle `Copy` task rather than loading them from a CDN.

## Usage

```kotlin
import io.github.shijianjs.ktjs.bpmn.bpmnModeler
import io.github.shijianjs.ktjs.bpmn.core.BpmnContainer
import io.github.shijianjs.ktjs.bpmn.core.BpmnEvents
import io.github.shijianjs.ktjs.bpmn.core.baseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.saveXMLOptions

val modeler = bpmnModeler(baseViewerOptions { container = BpmnContainer(host) })

val listener = modeler.on(BpmnEvents.ImportDone) { println(modeler.getDefinitions().type) }

modeler.importXML(xml).then {
    modeler.saveXML(saveXMLOptions { format = true }).then { saved -> println(saved.xml) }
    Unit
}
```

- A listener registered through `on` returns nothing to the library. bpmn-js reads any non-`undefined`
  return as “cancel the default action”, and a Kotlin `Unit` return would break the import (the
  viewer then reports `no diagram to display`). Use `onGuarded` when you want to answer back.
- `on` returns a `BpmnListener`; remove it with `listener.remove()`. `off(event)` drops every listener
  of that event, as upstream.
- `BpmnViewer` / `BpmnNavigatedViewer` / `BpmnModeler` share `BpmnBaseViewer`, which is where
  `importXML`, `saveXML`, `saveSVG`, `attachTo`, `get`, `clear` and `destroy` live.
- Services are reached by name: `modeler.service<JsAny>("canvas")`.

## Build and test

```bash
./gradlew :ktjs-bpmn:jsBrowserTest   # Chromium: renders real diagrams, round-trips XML
./gradlew :demo:e2eTest              # Playwright against the demo page
```

After adding or upgrading an npm dependency, refresh the shared lock file with the official registry
(the lock file is committed, and CI has no mirror):

```bash
./gradlew --stop
$env:npm_config_registry='https://registry.npmjs.org'
./gradlew kotlinUpgradePackageLock
```

## License

MIT, same as the upstream `bpmn-js` package (Copyright (c) 2014-present Camunda Services GmbH).
