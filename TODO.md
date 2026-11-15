# TODO

## ktjs-bpmn 上游 API 对齐

把上游 `bpmn-js` 18.31.0-kt.1 的类型声明逐个落成 Kotlin/JS 声明，并让 demo、文档、
测试与之一致。

- 总计：26
- 已完成：15
- 明确不做：5
- 待完成：6

详细清单：[coverage.md](docs/development/coverage.md)
技术决策：[technical-decisions.md](docs/development/technical-decisions.md)

### 当前待办

- [ ] `diagram-js` 服务类型的完整声明（`Canvas`、`ElementRegistry`、`CommandStack`、`Modeling` 等）
- [ ] `Viewer` 之外的特性模块入口（`bpmn-js/lib/features/*`，如 `modeling`、`align-elements`）
- [ ] `BaseViewerOptions` 上游 `Record<string, any>` 里的其余选项
- [ ] `Modeler` 的静态成员（`Modeler.Viewer`、`Modeler.NavigatedViewer`）
- [ ] `EventBus` 事件的负载类型（`ElementClickEvent` 之类）逐个建模
- [ ] 上游升级后的声明对齐

### 注意事项

- `bpmn-js` 整体 MIT，没有需要排除的付费部分；`bpmn-js-properties-panel` 等兄弟包不在本仓库范围内。
- 页面必须加载 `bpmn-js/dist/assets` 下的三个样式/字体资源，否则图形无样式、调色板无图标。
- 监听器一旦返回值，bpmn-js 会取消默认行为：wrapper 的 `on` 保证不返回，`onGuarded` 才是显式表达「同意/取消」的入口。
- 包装后 `off(callback)` 无法按原回调匹配，因此 `on` 返回 `BpmnListener`，用 `remove()` 注销。
