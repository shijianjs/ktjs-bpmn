package demo.page.modeler

import io.github.shijianjs.ktjs.demo.page.DemoPage
import io.github.shijianjs.ktjs.bpmn.bpmnModeler
import io.github.shijianjs.ktjs.bpmn.core.BpmnContainer
import io.github.shijianjs.ktjs.bpmn.core.baseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.saveXMLOptions
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.page.Blocks.exampleBox
import io.github.shijianjs.ktjs.demo.page.Grid
import io.github.shijianjs.ktjs.demo.page.SectionBlock
import io.github.shijianjs.ktjs.dom.element.inlineStyle
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import io.github.shijianjs.ktjs.vaadin.button.ButtonElement
import io.github.shijianjs.ktjs.vaadin.button.createButton
import kotlin.js.JsAny
import web.animations.requestAnimationFrame
import web.cssom.FontFamily
import web.cssom.Margin
import web.cssom.Overflow
import web.cssom.WhiteSpace
import web.cssom.px
import web.dom.clickEvent
import web.events.addHandler
import web.html.HTMLElement
import web.html.HtmlTagName

internal typealias Modeler = io.github.shijianjs.ktjs.bpmn.BpmnModeler

/**
 * The modeler page: a toolbar, the canvas the palette and context pad live in, and the XML read back.
 *
 * bpmn-js ships three stylesheets (`diagram-js`, `bpmn-js` and the icon font). `index.html` links
 * them, but the canvas lives in this page's shadow tree and document styles do not cross that
 * boundary, so the page re-hosts all three through
 * [io.github.shijianjs.ktjs.demo.page.DemoPage.addLinkedSheet]; the icon font's `url()`s are
 * re-pointed at the original sheet while copying, otherwise every glyph would be missing.
 */
internal class ModelerPage : DemoPage() {

    private val texts = Binder(modelerTexts)

    /** The strings the readouts and the sample diagram use; kept in step with the switch. */
    private var current: ModelerTexts = modelerEnglish

    private var modeler: Modeler? = null

    private val canvas: HTMLElement = HtmlTagName.div {
        setAttribute("data-canvas", "")
        inlineStyle.apply {
            height = 460.px
        }
    }

    private val status: HTMLElement = HtmlTagName.p { setAttribute("data-status", "") }

    private val xml: HTMLElement = HtmlTagName.pre {
        setAttribute("data-xml", "")
        inlineStyle.apply {
            margin = Margin(0.px, 0.px)
            fontFamily = FontFamily.monospace
            fontSize = 12.px
            whiteSpace = WhiteSpace.preWrap
            maxHeight = 300.px
            overflow = Overflow.scroll
        }
    }

    private val buttons: List<ButtonElement> = ACTIONS.map { action ->
        createButton { setAttribute("data-action", action.id) }
    }

    init {
        addLinkedSheet("assets/diagram-js.css")
        addLinkedSheet("assets/bpmn-js.css")
        addLinkedSheet("assets/bpmn-font/css/bpmn.css")

        val section = SectionBlock.NAME { show() }
        texts.bind(section.heading, { it.canvasHeading })
        texts.bind(section.hint, { it.hint })
        section.body.append(
            Grid.NAME { show(150); add(*buttons.toTypedArray()) },
            status,
            exampleBox(null, canvas),
        )

        val output = SectionBlock.NAME { show() }
        texts.bind(output.heading, { it.xmlHeading })
        output.body.append(exampleBox(null, xml))

        body.append(section.root, output.root)
        xml.textContent = current.emptyXml

        buttons.forEachIndexed { index, button ->
            val action = ACTIONS[index]
            button.clickEvent.addHandler {
                val target = modeler ?: return@addHandler
                action.run(this, target)
            }
        }

        createOnceMounted()
    }

    /**
     * Creates the modeler as soon as the canvas is in the document *and* laid out, then imports the
     * sample.
     *
     * diagram-js sizes its SVG from the container's box when the modeler is constructed, and it only
     * follows later layouts through its own resize path. A modeler built while the page is still
     * being attached therefore keeps a 150-pixel viewport forever — every shape looks squashed, and
     * nothing reports an error. Waiting for a real box avoids that entirely.
     */
    private fun createOnceMounted() {
        if (modeler != null) return
        if (!canvas.isConnected || canvas.clientHeight < 400 || canvas.clientWidth < 100) {
            requestAnimationFrame { _ -> createOnceMounted() }
            return
        }
        val created = bpmnModeler(baseViewerOptions { container = BpmnContainer(canvas) })
        modeler = created
        created.importXML(sampleDiagram(current)).then<Unit> {
            refresh(created)
            Unit
        }
    }

    /** Re-reads the registry: how many elements the diagram holds. */
    fun refresh(modeler: Modeler) {
        val registry = modeler.get("elementRegistry")
        val count = if (registry == null) 0.0 else elementCount(registry)
        status.textContent = "${current.statusPrefix}: ${count.toInt()}"
        status.setAttribute("data-elements", "${count.toInt()}")
    }

    /** Replaces the diagram with an empty one. */
    fun createDiagram(modeler: Modeler) {
        modeler.createDiagram().then<Unit> {
            xml.textContent = current.emptyXml
            refresh(modeler)
            Unit
        }
    }

    /** Writes the whole document into the XML panel. */
    fun exportXml(modeler: Modeler) {
        modeler.saveXML(saveXMLOptions { format = true }).then<Unit> { saved ->
            xml.textContent = saved.xml ?: current.emptyXml
            Unit
        }
    }

    override fun applyTranslation(lang: Lang) {
        current = modelerTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
        buttons.forEachIndexed { index, button ->
            button.textContent = current.labels.getOrElse(index) { "" }
        }
        xml.textContent = current.emptyXml
        modeler?.let { refresh(it) }
    }

    companion object : CustomElementReferenceAdapter<ModelerPage>(ModelerPage::class.js, "bpmn-modeler-page") {
        private val modelerEnglish: ModelerTexts = ModelerTexts(
            title = "Modeler",
            summary = "The real bpmn-js modeler: drag elements from the palette on the left, edit them with " +
                "the context pad, then export the document as XML.",
            canvasHeading = "Canvas",
            xmlHeading = "The document as XML",
            statusPrefix = "Elements in the diagram",
            hint = "Everything on this page goes through the wrapper: `bpmnModeler`, `importXML`, `saveXML`, " +
                "`createDiagram`, and the `service` accessor.",
            emptyXml = "Export the document and its XML shows up here.",
            labels = listOf("New diagram", "Export XML"),
            sampleTexts = listOf("Order received", "Check stock", "Ship order", "Done"),
        )
        private val modelerChinese: ModelerTexts = ModelerTexts(
            title = "建模器",
            summary = "真实 bpmn-js 建模器：从左侧调色板拖入元素、用上下文菜单编辑，最后把文档导出成 XML。",
            canvasHeading = "画布",
            xmlHeading = "文档的 XML",
            statusPrefix = "图元数量",
            hint = "页面上的操作全走封装：`bpmnModeler`、`importXML`、`saveXML`、`createDiagram`，以及 " +
                "`service` 访问器。",
            emptyXml = "导出后，XML 会显示在这里。",
            labels = listOf("新建流程图", "导出 XML"),
            sampleTexts = listOf("收到订单", "检查库存", "发货", "完成"),
        )
        private val modelerTexts: Translations<ModelerTexts> = texts(
            Langs.EN to modelerEnglish,
            Langs.ZH_HANS to modelerChinese,
        )
        private val ACTIONS: List<Action> = listOf(
            Action("new") { page, modeler -> page.createDiagram(modeler) },
            Action("export") { page, modeler -> page.exportXml(modeler) },
        )
        /** How many elements the registry holds; the modeler's own bookkeeping, not the DOM's. */
        private val elementCount: (JsAny) -> Double = js("(registry) => registry.getAll().length")
        /**
         * The sample diagram: a start event, two tasks and an end event, labelled in the page's language.
         *
         * The XML is a string because that is what `importXML` takes — the wrapper does not pretend to
         * model BPMN itself.
         */
        private fun sampleDiagram(texts: ModelerTexts): String = """
            <?xml version="1.0" encoding="UTF-8"?>
            <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                              xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                              xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                              xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                              id="Definitions_demo" targetNamespace="http://bpmn.io/schema/bpmn">
              <bpmn:process id="Process_demo" name="Order handling" isExecutable="false">
                <bpmn:startEvent id="StartEvent_1" name="${texts.sampleTexts[0]}">
                  <bpmn:outgoing>Flow_1</bpmn:outgoing>
                </bpmn:startEvent>
                <bpmn:task id="Task_1" name="${texts.sampleTexts[1]}">
                  <bpmn:incoming>Flow_1</bpmn:incoming>
                  <bpmn:outgoing>Flow_2</bpmn:outgoing>
                </bpmn:task>
                <bpmn:task id="Task_2" name="${texts.sampleTexts[2]}">
                  <bpmn:incoming>Flow_2</bpmn:incoming>
                  <bpmn:outgoing>Flow_3</bpmn:outgoing>
                </bpmn:task>
                <bpmn:endEvent id="EndEvent_1" name="${texts.sampleTexts[3]}">
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
                    <di:waypoint x="196" y="138" />
                    <di:waypoint x="250" y="138" />
                  </bpmndi:BPMNEdge>
                  <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
                    <di:waypoint x="360" y="138" />
                    <di:waypoint x="420" y="138" />
                  </bpmndi:BPMNEdge>
                  <bpmndi:BPMNEdge id="Flow_3_di" bpmnElement="Flow_3">
                    <di:waypoint x="530" y="138" />
                    <di:waypoint x="590" y="138" />
                  </bpmndi:BPMNEdge>
                </bpmndi:BPMNPlane>
              </bpmndi:BPMNDiagram>
            </bpmn:definitions>
        """.trimIndent()
    }
}
