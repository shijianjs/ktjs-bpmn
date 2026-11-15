# ktjs-bpmn

bpmn-js 的 Kotlin/JS 绑定 —— BPMN 2.0 查看器与建模器

封装覆盖 `bpmn-js` 18：三个入口（`Viewer`、`NavigatedViewer`、`Modeler`）、它们共享的 `BaseViewer` 表面、`Modeler.createDiagram`、查看器选项、promise 结果类型、带类型的生命周期事件名，以及 `ModelUtil` 的四个助手。Kotlin 监听器被包了一层，`Unit` 返回值不会再误取消默认行为；「元素或选择器」参数做成不透明联合类型 + 工厂。

- 上游包：[`bpmn-js`](https://github.com/bpmn-io/bpmn-js) 18.31.0
- 文档：https://shijianjs.github.io/ktjs-bpmn/
- 演示：https://shijianjs.github.io/ktjs-bpmn/demo/
- API：https://shijianjs.github.io/ktjs-bpmn/api/
- 英文说明：[README.md](README.md)

## 用法

```kotlin
implementation("io.github.shijianjs:ktjs-bpmn:18.31.0-kt.1")
```

## 构建

```bash
./gradlew build
./gradlew :ktjs-bpmn:jsBrowserTest
./gradlew :demo:e2eTest
./gradlew pagesSite
```

## 许可证

wrapper 与上游 `bpmn-js` 同为 MIT；版权声明见文档的「许可证」页。
