package demo

import io.github.shijianjs.ktjs.bpmn.bpmnModeler
import io.github.shijianjs.ktjs.bpmn.core.BpmnContainer
import io.github.shijianjs.ktjs.bpmn.core.baseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.saveXMLOptions
import kotlin.js.JsAny
import kotlin.js.unsafeCast
import web.cssom.ClassName
import web.dom.document
import web.events.EventHandler
import web.html.HTMLElement
import web.html.HtmlTagName

private const val PROJECT_NAME = "ktjs-bpmn"

private val texts = Texts(
    mapOf(
        "h1" to ("$PROJECT_NAME demo" to "$PROJECT_NAME 演示"),
        "intro" to (
            "A BPMN 2.0 modeler built with the real bpmn-js engine: drag elements from the palette on " +
                "the left, edit them with the context pad, then export the document as XML." to
                "用真实 bpmn-js 引擎搭的 BPMN 2.0 建模器：从左侧调色板拖入元素、用上下文菜单编辑，最后把" +
                "文档导出成 XML。"
        ),
        "new" to ("New diagram" to "新建流程图"),
        "export" to ("Export XML" to "导出 XML"),
        "status" to ("Elements in the diagram" to "图元数量"),
        "empty" to (
            "Export the document and its XML shows up here." to
                "导出后，XML 会显示在这里。"
        ),
        "hint" to (
            "Everything on this page goes through the wrapper: `bpmnModeler`, `importXML`, " +
                "`saveXML`, `createDiagram`, and the `service` accessor." to
                "页面上的操作全走 wrapper：`bpmnModeler`、`importXML`、`saveXML`、`createDiagram`，" +
                "以及 `service` 访问器。"
        ),
    ),
)

private val sampleDiagram = """
    <?xml version="1.0" encoding="UTF-8"?>
    <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                      xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                      xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                      id="Definitions_demo" targetNamespace="http://bpmn.io/schema/bpmn">
      <bpmn:process id="Process_demo" name="Order handling" isExecutable="false">
        <bpmn:startEvent id="StartEvent_1" name="Order received">
          <bpmn:outgoing>Flow_1</bpmn:outgoing>
        </bpmn:startEvent>
        <bpmn:task id="Task_1" name="Check stock">
          <bpmn:incoming>Flow_1</bpmn:incoming>
          <bpmn:outgoing>Flow_2</bpmn:outgoing>
        </bpmn:task>
        <bpmn:task id="Task_2" name="Ship order">
          <bpmn:incoming>Flow_2</bpmn:incoming>
          <bpmn:outgoing>Flow_3</bpmn:outgoing>
        </bpmn:task>
        <bpmn:endEvent id="EndEvent_1" name="Done">
          <bpmn:incoming>Flow_3</bpmn:incoming>
        </bpmn:endEvent>
        <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_1" />
        <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="Task_2" />
        <bpmn:sequenceFlow id="Flow_3" sourceRef="Task_2" targetRef="EndEvent_1" />
      </bpmn:process>
      <bpmndi:BPMNDiagram id="BPMNDiagram_demo">
        <bpmndi:BPMNPlane id="BPMNPlane_demo" bpmnElement="Process_demo">
          <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
            <dc:Bounds x="160" y="120" width="36" height="36" />
            <bpmndi:BPMNLabel>
              <dc:Bounds x="143" y="163" width="71" height="14" />
            </bpmndi:BPMNLabel>
          </bpmndi:BPMNShape>
          <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
            <dc:Bounds x="250" y="98" width="110" height="80" />
          </bpmndi:BPMNShape>
          <bpmndi:BPMNShape id="Task_2_di" bpmnElement="Task_2">
            <dc:Bounds x="420" y="98" width="110" height="80" />
          </bpmndi:BPMNShape>
          <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
            <dc:Bounds x="590" y="120" width="36" height="36" />
            <bpmndi:BPMNLabel>
              <dc:Bounds x="583" y="163" width="51" height="14" />
            </bpmndi:BPMNLabel>
          </bpmndi:BPMNShape>
          <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="196" y="138" />
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="250" y="138" />
          </bpmndi:BPMNEdge>
          <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="360" y="138" />
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="420" y="138" />
          </bpmndi:BPMNEdge>
          <bpmndi:BPMNEdge id="Flow_3_di" bpmnElement="Flow_3">
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="530" y="138" />
            <di:waypoint xmlns:di="http://www.omg.org/spec/BPMN/20100524/DI" x="590" y="138" />
          </bpmndi:BPMNEdge>
        </bpmndi:BPMNPlane>
      </bpmndi:BPMNDiagram>
    </bpmn:definitions>
""".trimIndent()

/** How many elements the registry holds; the modeler's own bookkeeping, not the DOM's. */
private val elementCount: (JsAny) -> Double = js("(registry) => registry.getAll().length")

/** Entry point of the demo page: a live BPMN modeler with export and a bilingual UI. */
fun main() {
    val root = document.createElement(HtmlTagName.div).unsafeCast<HTMLElement>()
    root.className = ClassName("page")

    val logo = document.createElement(HtmlTagName.img).unsafeCast<HTMLElement>()
    logo.className = ClassName("logo")
    logo.setAttribute("src", "img/$PROJECT_NAME-logo.png")
    logo.setAttribute("alt", "$PROJECT_NAME logo")

    val heading = document.createElement(HtmlTagName.h1).unsafeCast<HTMLElement>()
    val intro = document.createElement(HtmlTagName.p).unsafeCast<HTMLElement>()
    intro.className = ClassName("hint")

    val header = document.createElement(HtmlTagName.header).unsafeCast<HTMLElement>()
    header.className = ClassName("header")
    header.append(logo, heading, languageSwitch(texts))

    val canvas = document.createElement(HtmlTagName.div).unsafeCast<HTMLElement>()
    canvas.className = ClassName("canvas")
    canvas.setAttribute("data-role", "canvas")

    val modeler = bpmnModeler(baseViewerOptions { container = BpmnContainer(canvas) })

    val status = document.createElement(HtmlTagName.p).unsafeCast<HTMLElement>()
    status.className = ClassName("view")

    val xml = document.createElement(HtmlTagName.pre).unsafeCast<HTMLElement>()
    xml.className = ClassName("xml")
    xml.setAttribute("data-role", "xml")

    val newButton = document.createElement(HtmlTagName.button).unsafeCast<HTMLElement>()
    val exportButton = document.createElement(HtmlTagName.button).unsafeCast<HTMLElement>()
    listOf(newButton to "new", exportButton to "export").forEach { (button, action) ->
        button.setAttribute("type", "button")
        button.setAttribute("data-action", action)
    }

    val row = document.createElement(HtmlTagName.div).unsafeCast<HTMLElement>()
    row.className = ClassName("row")
    row.append(newButton, exportButton)

    fun renderStatus() {
        val registry = modeler.get("elementRegistry")
        val count = if (registry == null) 0.0 else elementCount(registry)
        status.textContent = "${texts["status"]}: ${count.toInt()}"
    }

    // Re-import keeps the sample in place after the modeler starts empty.
    modeler.importXML(sampleDiagram).then<Unit> {
        renderStatus()
        Unit
    }

    newButton.onclick = EventHandler {
        modeler.createDiagram().then<Unit> {
            xml.textContent = ""
            renderStatus()
            Unit
        }
    }

    exportButton.onclick = EventHandler {
        modeler.saveXML(saveXMLOptions { format = true }).then<Unit> { saved ->
            xml.textContent = saved.xml ?: texts["empty"]
            Unit
        }
    }

    fun renderTexts() {
        heading.textContent = texts["h1"]
        intro.textContent = texts["intro"]
        newButton.textContent = texts["new"]
        exportButton.textContent = texts["export"]
        renderStatus()
        if (xml.textContent?.isEmpty() != false) {
            xml.textContent = texts["empty"]
        }
    }
    texts.onChange { renderTexts() }
    renderTexts()

    val hint = document.createElement(HtmlTagName.p).unsafeCast<HTMLElement>()
    hint.className = ClassName("hint small")
    texts.onChange { hint.textContent = texts["hint"] }
    hint.textContent = texts["hint"]

    val content = document.createElement(HtmlTagName.main).unsafeCast<HTMLElement>()
    content.className = ClassName("content")
    content.append(row, status, canvas, hint, xml)

    root.append(header, intro, content)
    document.body?.append(root)
}
