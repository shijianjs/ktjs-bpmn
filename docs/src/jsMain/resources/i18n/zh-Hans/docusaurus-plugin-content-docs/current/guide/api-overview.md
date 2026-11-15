---
sidebar_position: 3
title: API 概览
---

# API 概览

## 包结构

| 包 | 内容 |
| --- | --- |
| `io.github.shijianjs.ktjs.bpmn` | `BpmnViewer`、`BpmnNavigatedViewer`、`BpmnModeler`、`BpmnBaseViewer`、`BpmnBaseModeler`、`BpmnListener`、`bpmnViewer`、`bpmnNavigatedViewer`、`bpmnModeler`、`on`、`onGuarded`、`service` |
| `…bpmn.core` | `BaseViewerOptions`、`baseViewerOptions`、`BpmnLength`、`BpmnContainer`、`SaveXMLOptions`、`saveXMLOptions`、`ImportXMLResult`、`SaveXMLResult`、`SaveSVGResult`、`BpmnModdleElement`、`BpmnElement`、`BpmnEvent`、`BpmnEvents`、`BpmnModelUtil` |

## 本 wrapper 的约定

- **监听器不返回值。** bpmn-js 把任何非 `undefined` 的返回值读成「取消默认行为」，而 Kotlin 的
  `Unit` 返回 lambda 得到的是 `Unit` 对象、不是 `undefined`。注册在 `import.parse.complete` 上的
  监听器若漏出 `Unit`，每次导入都会以 `no diagram to display` 失败。wrapper 用 JS shim 把回调包一层，
  保证不返回任何值；`on` 返回 `BpmnListener`，可以按身份移除。
- **「元素或选择器」拆成两个方法。** `container` 是 `string | HTMLElement`，`importXML` / `open`
  接受 `ModdleElement | string`；两种都做成不透明联合类型 + 工厂（`BpmnContainer(element)` /
  `BpmnContainer(selector)`），查看器上再配 `…Diagram` / `…ById` 孪生。
- **`width` / `height` 是 `number | string`**，建模为 `BpmnLength`，用
  `BpmnLength(640.0)` / `BpmnLength("100%")` 构造。
- **事件名有类型，负载没有。** `BpmnEvents` 收好生命周期事件名，拼错即编译错误；每个事件的负载
  （图形、画布、definitions 对象）留在事件对象上，经 `JsAny` 取用。

## 有意放宽的类型

| 成员 | Kotlin 类型 | 原因 |
| --- | --- | --- |
| `BaseViewerOptions.moddleExtensions` | `JsAny?` | 上游是 `Record<string, {name, package}>`，按前缀索引的 moddle 包表 |
| `BaseViewerOptions.additionalModules` | `Array<JsAny>` | `didi` 模块对象：`{ __init__: […], service: ["type", Class] }` |
| `BpmnEvent` 的负载 | 经 `JsAny` | 每个事件的负载类型各不相同 |
| `get(service)` | `JsAny?` | 服务表是对 40+ 个服务的泛型；需要时用 `service<T>(name)` 转型 |

每一行都是上游真实形状，均记在 `docs/development/technical-decisions.md`。
