# API Coverage

## 来源与范围

- 来源：`bpmn-js` 18.31.0（npm 发布包，含生成的 `lib/**/*.d.ts`）
- 源码克隆：`S:\workspace\github\bpmn-io\bpmn-js`（同版本）
- 核对日期：2026-11-16
- 范围：`bpmn-js` 的三个官方入口（`lib/Viewer`、`lib/NavigatedViewer`、`lib/Modeler`）、它们
  共享的 `lib/BaseViewer` 与 `lib/BaseModeler` 表面、`lib/util/ModelUtil`，以及入口文件导出的
  选项与结果类型。
- 不纳入统计：`lib/features/*` 内的特性模块（各自由 `additionalModules` 提供，属于服务层，
  不属于入口 API）、`lib/draw/*` 渲染实现、`bpmn-js-properties-panel` 等兄弟包。

## 统计

| 状态 | 数量 |
|---|---:|
| 总计 | 26 |
| 已完成 | 15 |
| 明确不做 | 5 |
| 待完成 | 6 |

已完成组（不逐项列名，实现见源码）：

1. `Viewer`、`NavigatedViewer`、`Modeler` 三个类与构造选项
2. `importXML`（含按 `ModdleElement` / id 指定 diagram 的两个孪生）
3. `importDefinitions`、`open`（含按 id 的孪生）
4. `saveXML`（含 `format` / `preamble` 选项）、`saveSVG`
5. `attachTo`、`detach`、`getDefinitions`、`getModules`
6. `get(service)` 与 `service<T>()` 转型助手
7. `clear`、`destroy`
8. `on` / `onGuarded` / `on(priority)` 与 `BpmnListener.remove`、`off(event)`
9. `Modeler.createDiagram`
10. `BaseViewerOptions`（`width`、`height`、`position`、`container`、`moddleExtensions`、
    `additionalModules`）与 `baseViewerOptions` 构造器
11. `ImportXMLResult`、`SaveXMLResult`、`SaveSVGResult` 三个结果类型
12. `BpmnEvents` 的 11 个生命周期事件名
13. `BpmnModdleElement`、`BpmnElement` 两个模型类型
14. `BpmnModelUtil` 的四个助手（`isType`、`isAnyType`、`businessObject`、`di`）
15. `BpmnLength` / `BpmnContainer` 两个联合类型与它们的工厂函数

## 明确不做

| 名称 | 排除理由 |
|---|---|
| `lib/features/*` 特性模块（`modeling`、`palette`、`context-pad`、`align-elements` 等 40+ 入口） | 这些是供 `additionalModules` 装配的服务定义，不是调用方直接使用的 API；逐个声明等于把 `diagram-js` 的注入体系搬过来，且多数没有独立的 `.d.ts` 签名 |
| `lib/draw/*`（`BpmnRenderer`、`TextRenderer`、`PathMap`） | 渲染实现，上游以内部实现为主，替换它们需要实现整套渲染契约 |
| `lib/import/*`（`BpmnImporter`、`BpmnTreeWalker`） | 导入流程的内部实现，由 `importXML` 驱动 |
| `lib/model/*`（`Types.d.ts` 除两个公开类型外） | `Moddle` / `ModdleExtension` 在上游本身就声明为 `any` / `{}`，没有可映射的类型 |
| `bpmn-js-properties-panel`、`bpmn-js-token-simulation` 等兄弟包 | 各自独立发布，属于其它 wrapper 仓库的范围 |

## 待完成

### 服务层

- [ ] `diagram-js` 服务类型的完整声明（`Canvas`、`ElementRegistry`、`CommandStack`、`Modeling`、
      `EventBus`、`Selection`、`Overlays`）
- [ ] `BaseViewerOptions` 里上游 `Record<string, any>` 覆盖的其余选项（`keyboard`、`canvas` 等）

### 模块入口

- [ ] `bpmn-js/lib/features/*` 的类型化入口（按需，一次一个）
- [ ] `Modeler` 的静态成员 `Modeler.Viewer` / `Modeler.NavigatedViewer`

### 事件负载

- [ ] `EventBus` 各事件的负载类型（`ElementClickEvent`、`SelectionEvent` 等）逐个建模
- [ ] 上游升级后的声明对齐

## 验证依据

| 命令 | 结果 |
|---|---|
| `./gradlew :ktjs-bpmn:compileKotlinJs` | 通过 |
| `./gradlew :ktjs-bpmn:jsBrowserTest` | 7 个用例全部通过（Chromium） |
| `./gradlew :demo:jsBrowserDistribution` | 通过 |
| `./gradlew :demo:e2eTest` | 6 个用例通过（Playwright：渲染、导出、新建、语言切换、无 console 报错） |
| `./gradlew :docs:docusaurusBuild` | en 与 zh-Hans 均成功 |
