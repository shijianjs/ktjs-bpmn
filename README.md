# ktjs-bpmn

Kotlin/JS bindings for bpmn-js — BPMN 2.0 viewer and modeler

The wrapper covers `bpmn-js` 18: the three entry points (`Viewer`, `NavigatedViewer`, `Modeler`), the `BaseViewer` surface they share, `Modeler.createDiagram`, viewer options, the result types of the promises, typed lifecycle event names, and the four `ModelUtil` helpers. Kotlin listeners are wrapped so their `Unit` return cannot cancel a default action, and the element-or-selector arguments become opaque union types with factories.

- Upstream package: [`bpmn-js`](https://github.com/bpmn-io/bpmn-js) 18.31.0
- Docs: https://shijianjs.github.io/ktjs-bpmn/
- Demo: https://shijianjs.github.io/ktjs-bpmn/demo/
- API: https://shijianjs.github.io/ktjs-bpmn/api/
- 中文说明：[README.zh-CN.md](README.zh-CN.md)

## Usage

```kotlin
implementation("io.github.shijianjs:ktjs-bpmn:18.31.0-kt.1")
```

## Build

```bash
./gradlew build
./gradlew :ktjs-bpmn:jsBrowserTest
./gradlew :demo:e2eTest
./gradlew pagesSite
```

## License

The wrapper is MIT, same as the upstream `bpmn-js` — see the "License" page of the docs.
