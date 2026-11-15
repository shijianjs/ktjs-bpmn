# Technical decisions

Decisions taken while wrapping `bpmn-js` 18.31.0. Several are forced by how the library interacts
with JavaScript; the listener one is a real bug the wrapper exists to prevent.

## 1. Entry points are sub-paths, and they must be fully specified

`bpmn-js` is published as ES modules, and webpack refuses a sub-path without the extension:

```text
Module not found: Error: Can't resolve 'bpmn-js/lib/Viewer'
The request 'bpmn-js/lib/Viewer' failed to resolve only because it was resolved as fully specified
```

So `@JsModule` targets `bpmn-js/lib/Viewer.js`, `…/NavigatedViewer.js`, `…/Modeler.js` and
`…/util/ModelUtil.js`. The root `bpmn-js` entry is only a re-export of `Viewer`; the sub-path is
used instead so each class has an unambiguous module.

## 2. A class-level `@JsModule` works here, unlike with `gridstack`

Each `lib/*.js` file has a **default** export, which is exactly what a Kotlin `@JsModule` class
compiles to. That is why the three classes are declared directly with `@JsModule` and a
`definedExternally` constructor, while `ModelUtil` — named function exports only — is an
`external object`.

## 3. Kotlin listeners would cancel every default action

`EventBus` treats *any* non-`undefined` return from a listener as “handle it here”: the event's
default action is skipped and later listeners never run. A Kotlin lambda typed `(BpmnEvent) -> Unit`
yields the `Unit` **object**, which is not `undefined`. Registering such a listener for
`import.parse.complete` therefore broke the import with

```text
Error: no diagram to display
```

because bpmn-js's own listener (the one that picks the diagram to render) never ran. The wrapper
cannot rely on the callback's return, so `on` wraps it in a JS shim that returns nothing:

```text
(cb) => (event) => { cb(event); }
```

`onGuarded` is the counterpart that forwards the answer (`false` cancels), built on
`(cb) => (event) => cb(event) === false ? false : undefined`, and the raw signatures stay available
as `onRaw` / `onRawWithPriority` for callers who want to control the return themselves.

## 4. `on` returns a listener handle, because `off(callback)` cannot match

The event bus unregisters by function identity, and the identity is the shim, not the Kotlin
callback. `on` therefore returns a `BpmnListener` holding the shim; `remove()` calls the raw `off`
with it. `off(event)` still removes every listener of an event, as upstream.

## 5. Element-or-selector is an opaque union with factories

`container` is `string | HTMLElement` upstream, and `importXML` / `open` accept
`ModdleElement | string`. Kotlin cannot overload on that union, so:

- `BpmnContainer(element)` / `BpmnContainer(selector)` build the opaque `BpmnContainer`;
- the viewer exposes `importXML` / `importXMLDiagram` / `importXMLDiagramById` and
  `openDiagram` / `openDiagramById`, all `@JsName`-ed to the upstream method.

## 6. Dimensions are `number | string`

`width` / `height` accept a pixel count or any CSS length. `BpmnLength` keeps both halves typed
(`BpmnLength(640.0)` / `BpmnLength("100%")`) instead of dropping to `JsAny`.

## 7. The service accessor is typed on demand

`get(name)` returns `JsAny?`, because the service map is generic over the whole `diagram-js` container.
`service<T>(name)` casts once, at the call site, where the caller already knows which service it
wants. A wrong name fails at the first call on the value, which is the earliest point the wrapper
can see.

## 8. Inventory of deliberately loose types

| Member | Kotlin type | Reason |
| --- | --- | --- |
| `BaseViewerOptions.moddleExtensions` | `JsAny?` | `Record<string, {name, package}>` |
| `BaseViewerOptions.additionalModules` | `Array<JsAny>` | `didi` module objects |
| `get(service)` | `JsAny?` | generic service map |
| `SaveXMLResult.error` | `JsAny?` | upstream types it as `Error` without a shape |
| `BpmnEvent` payloads | `JsAny` | one payload type per event name |
| `BaseViewerOptions` extra keys | not declared | upstream `Record<string, any>` tail, see coverage |

Every row is a real upstream shape, not a shortcut.

## 9. Options are `unsafeJso` builders

`baseViewerOptions { … }` and `saveXMLOptions { … }` write only the fields the caller set. bpmn-js
merges its defaults over the options object, and an explicit `null` is not the same as an absent
key for `container` and the dimension fields.

## 10. The tests are promise-returning, not `suspend`

`kotlin.test` rejects `suspend` test functions on the JS target:

```text
'suspend' functions annotated with '@kotlin.test.Test' are unsupported.
```

Each test therefore returns the `Promise` it asserts in, which the Karma runner awaits, and a
rejected promise fails the test. The one place this matters: an assertion inside a `then` callback
must not be swallowed, so every chain is returned all the way up.

## 11. The demo needs the package's stylesheets, not its own CSS

bpmn-js draws the diagram, the palette and the context pad from
`dist/assets/{diagram-js.css,bpmn-js.css,bpmn-font/}`. Those files are copied out of
`node_modules/bpmn-js` by a Gradle `Copy` that keeps the package's `assets/` layout — the font
paths inside the CSS are relative and break if the folder is flattened.

## 12. `saveXML` writes lower-case element names

The serialiser emits the BPMN namespace form (`bpmn:definitions`, `bpmn:process`), not the moddle
`$type` form (`bpmn:Definitions`). Test assertions use the serialised spelling; the model objects
report the `$type` spelling. Mixing the two produces a test that fails on the correct output.

## 13. The lock file is regenerated against the official registry

`kotlin-js-store/package-lock.json` is committed so CI, which has no mirror, resolves what a local
build resolves. Adding `bpmn-js` required `gradlew --stop` plus `kotlinUpgradePackageLock` with
`npm_config_registry=https://registry.npmjs.org`.

## 14. Browser tests, not Node

bpmn-js needs a DOM, a size and a real renderer, so `jsTest` runs in Chromium through
`browser { test { chromium() } }`. Assertions read the library's own bookkeeping
(`definitions.$type`, `elementRegistry`, the saved XML) instead of pixel geometry.

## 15. Localised documentation lives directly under `current/`

Docusaurus resolves a translated doc by doc id, so
`i18n/zh-Hans/docusaurus-plugin-content-docs/current/guide/api-overview.md` is the only correct
location for `guide/api-overview`; an extra `docs/` segment silently falls back to English.
