---
sidebar_position: 1
title: Introduction
---

# Introduction

`ktjs-bpmn` is a Kotlin/JS wrapper for [`bpmn-js`](https://github.com/bpmn-io/bpmn-js) from [bpmn-js](https://github.com/bpmn-io/bpmn-js) 18.31.0: every type and member of
the upstream `.d.ts` files becomes an `external` declaration the compiler checks, with no
`dynamic` fallback.

The wrapper covers the three documented entry points of `bpmn-js` 18: `Viewer`, `NavigatedViewer` and `Modeler`, the `BaseViewer` surface they share (`importXML` / `importDefinitions` / `open`, `saveXML`, `saveSVG`, `attachTo`, `detach`, `getDefinitions`, `getModules`, the `get` service accessor, `clear`, `destroy`, event subscriptions), `Modeler.createDiagram`, the viewer options (`width`, `height`, `position`, `container`, `moddleExtensions`, `additionalModules`), the result types of the promises, the lifecycle event names, and the four model helpers of `bpmn-js/lib/util/ModelUtil` (`is`, `isAny`, `getBusinessObject`, `getDi`).

## What is wrapped

| Module | Gradle artifact | Upstream |
| --- | --- | --- |
| `ktjs-bpmn` | `io.github.shijianjs:ktjs-bpmn` | `bpmn-js` 18.31.0 |

One npm package, one Gradle module, one Kotlin package (`io.github.shijianjs.ktjs.bpmn`) with a `core` sub-package for the options, the result types and the model helpers.

## License

The wrapper itself is MIT. The upstream `bpmn-js` license differs from it — read the
[license notes](guide/license.md) before shipping it commercially.

## Where to go next

- [Getting started](guide/getting-started.md) — add the dependency, make the first call, run the build.
- [API overview](guide/api-overview.md) — package layout, type mapping conventions, common patterns.
- [Demo](guide/demo.md) — see it running in the browser.
