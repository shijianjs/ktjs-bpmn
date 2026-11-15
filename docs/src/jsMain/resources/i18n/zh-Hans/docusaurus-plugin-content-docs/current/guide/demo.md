---
sidebar_position: 4
title: 演示页
---

# 演示页

演示页：[https://shijianjs.github.io/ktjs-bpmn/demo/](https://shijianjs.github.io/ktjs-bpmn/demo/)

演示页在一个「订单处理」样例流程上挂建模器：左侧调色板可添加元素、上下文菜单可编辑，图形由库自己
渲染。两个按钮都走 wrapper：**导出 XML** 调用 `saveXML` 并把序列化后的文档打印到下方面板，
**新建流程图** 调用 `createDiagram` 并把它清空。状态行通过 `elementRegistry` 服务读出图元数量，
语言开关会改写每个标签。

页面右上角的语言按钮在英文与简体中文之间切换，切换的是页面上全部可见文案（不只是页面标题）。

API 文档在 [https://shijianjs.github.io/ktjs-bpmn/api/](https://shijianjs.github.io/ktjs-bpmn/api/)，由 Dokka 从已发布模块生成。
