---
sidebar_position: 2
title: 快速开始
---

# 快速开始

## 依赖

```kotlin
kotlin {
    js {
        binaries.executable()
        browser()
    }
    sourceSets {
        jsMain.dependencies {
            implementation("io.github.shijianjs:ktjs-bpmn:18.31.0-kt.1")
            // `kotlin-browser` 的 `web.*` 类型出现在公开 API 里，所以消费方要能编译到它们。
            implementation("org.jetbrains.kotlin-wrappers:kotlin-browser:2026.11.1")
        }
    }
}
```

要求 Kotlin 2.4.21 或更新版本；wrapper 输出 ESM，`es2015` 目标起可用。

## 注册插件

没有插件注册表。`bpmn-js` 的 API 分散在 ES 模块里，且整包按 ES 模块发布，因此每个导入都必须写**完整路径**：

```kotlin
BpmnViewer      // bpmn-js/lib/Viewer.js
BpmnNavigatedViewer // bpmn-js/lib/NavigatedViewer.js
BpmnModeler     // bpmn-js/lib/Modeler.js
```

wrapper 已经替你写好。它同时导入 `bpmn-js/lib/util/ModelUtil.js` 提供模型助手，并随 npm 依赖携带库自带的样式（`bpmn-js/dist/assets/{diagram-js.css,bpmn-js.css,bpmn-font/}`）。

## 第一个调用

```kotlin
import io.github.shijianjs.ktjs.bpmn.bpmnModeler
import io.github.shijianjs.ktjs.bpmn.core.BpmnContainer
import io.github.shijianjs.ktjs.bpmn.core.BpmnEvents
import io.github.shijianjs.ktjs.bpmn.core.baseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.saveXMLOptions

fun mount(host: web.html.HTMLElement, xml: String) {
    val modeler = bpmnModeler(baseViewerOptions { container = BpmnContainer(host) })

    modeler.on(BpmnEvents.ImportDone) { println("已导入：${modeler.getDefinitions().type}") }

    modeler.importXML(xml).then {
        modeler.saveXML(saveXMLOptions { format = true }).then { saved ->
            println(saved.xml)
            Unit
        }
    }
}
```

页面必须加载库自带的样式，否则图形没有样式、调色板也没有图标。

## 构建与测试

```bash
./gradlew build                     # 编译、Detekt、浏览器测试
./gradlew :ktjs-bpmn:jsBrowserTest     # 只跑 wrapper 的浏览器测试
./gradlew :docs:docusaurusStart     # 文档开发服务器
./gradlew pagesSite                 # 文档在 /，演示在 /demo/，Dokka 在 /api/
```
