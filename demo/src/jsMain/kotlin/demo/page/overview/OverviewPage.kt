package demo.page.overview

import io.github.shijianjs.ktjs.demo.page.DemoPage
import demo.API_URL
import demo.DOCS_URL
import demo.SOURCE_URL
import io.github.shijianjs.ktjs.demo.i18n.Binder
import io.github.shijianjs.ktjs.demo.i18n.Lang
import io.github.shijianjs.ktjs.demo.i18n.Langs
import io.github.shijianjs.ktjs.demo.i18n.Translations
import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.demo.page.Blocks.anchor
import io.github.shijianjs.ktjs.demo.page.Blocks.codeText
import io.github.shijianjs.ktjs.demo.page.Blocks.paragraph
import io.github.shijianjs.ktjs.demo.page.Blocks.row
import io.github.shijianjs.ktjs.demo.page.SectionBlock
import io.github.shijianjs.ktjs.dom.element.invoke
import io.github.shijianjs.ktjs.dom.element.unaryPlus
import io.github.shijianjs.ktjs.dom.webcomponent.CustomElementReferenceAdapter
import web.html.HtmlTagName

/** Every string this page shows, in one language. */
private class OverviewTexts(
    val title: String,
    val summary: String,
    val docsLink: String,
    val sourceLink: String,
    val apiLink: String,
    val purposeHeading: String,
    val body: String,
    val bullets: List<String>,
    val xmlHeading: String,
    val xmlBody: String,
    val nextHeading: String,
    val nextBody: String,
)

private val overviewEnglish: OverviewTexts = OverviewTexts(
    title = "Overview",
    summary = "A Kotlin/JS wrapper for bpmn-js: a BPMN 2.0 modeler with typed options, typed results " +
        "and a checked service accessor.",
    docsLink = "Documentation",
    sourceLink = "Source repository",
    apiLink = "API reference",
    purposeHeading = "What it wraps",
    body = "bpmn-js is a modeler built on diagram-js: it owns the process, draws it, and hands you the " +
        "document as XML. In JavaScript the options object, the save options and the results are " +
        "untyped literals; the wrapper declares them, so `baseViewerOptions { container = … }` is " +
        "checked, `saveXMLOptions { format = true }` returns `xml: String?`, and `modeler.get(\"…\")` " +
        "is the only place a name is still a string.",
    bullets = listOf(
        "bpmnModeler(baseViewerOptions { container = BpmnContainer(host) }) — one modeler over one host.",
        "modeler.importXML(xml) / saveXML(saveXMLOptions { format = true }) — the document in and out.",
        "modeler.createDiagram() — an empty diagram, ready to edit.",
    ),
    xmlHeading = "The document is XML",
    xmlBody = "The model is the file: everything the modeler does ends up in the BPMN 2.0 XML, so " +
        "storing or diffing a diagram is a matter of keeping that text. The demo exports it into a " +
        "panel under the canvas.",
    nextHeading = "Reading order",
    nextBody = "The overview, then the modeler: drag elements from the palette, edit them with the " +
        "context pad, and export the document.",
)

private val overviewChinese: OverviewTexts = OverviewTexts(
    title = "概览",
    summary = "bpmn-js 的 Kotlin/JS 封装：BPMN 2.0 建模器，选项有类型、结果有类型，服务访问器也被检查。",
    docsLink = "使用文档",
    sourceLink = "源码仓库",
    apiLink = "API 文档",
    purposeHeading = "它封装了什么",
    body = "bpmn-js 是架在 diagram-js 上的建模器：它持有流程、把流程画出来，并交给你一份 XML 文档。在 " +
        "JavaScript 里选项对象、保存选项和结果都是无类型字面量；封装把它们声明出来：" +
        "`baseViewerOptions { container = … }` 会被检查，`saveXMLOptions { format = true }` 返回 " +
        "`xml: String?`，`modeler.get(\"…\")` 是唯一还按字符串取服务的地方。",
    bullets = listOf(
        "bpmnModeler(baseViewerOptions { container = BpmnContainer(host) }) —— 一个宿主上的一个建模器。",
        "modeler.importXML(xml) / saveXML(saveXMLOptions { format = true }) —— 文档进、文档出。",
        "modeler.createDiagram() —— 一张空白图，可以直接编辑。",
    ),
    xmlHeading = "文档就是 XML",
    xmlBody = "模型就是文件：建模器做的一切最终都落在 BPMN 2.0 的 XML 里，所以存盘或比对一张图，就是留住那段文本。" +
        "demo 把它导出到画布下面的面板里。",
    nextHeading = "建议顺序",
    nextBody = "先看概览，再看建模器：从调色板拖入元素、用上下文菜单编辑，然后导出文档。",
)

private val overviewTexts: Translations<OverviewTexts> = texts(
    Langs.EN to overviewEnglish,
    Langs.ZH_HANS to overviewChinese,
)

/**
 * Landing page: what the library wraps, how it is consumed, where the reference lives.
 */
internal class OverviewPage : DemoPage() {

    private val texts = Binder(overviewTexts)


    init {
        addLink(texts.bind(anchor("", DOCS_URL, external = true), { it.docsLink }))
        addLink(texts.bind(anchor("", API_URL, external = true), { it.apiLink }))
        addLink(texts.bind(anchor("", SOURCE_URL, external = true), { it.sourceLink }))

        val purpose = SectionBlock.NAME { show() }
        texts.bind(purpose.heading, { it.purposeHeading })
        val bullets = HtmlTagName.ul {
            List(3) { index -> texts.bind(+HtmlTagName.li()) { it.bullets.getOrNull(index).orEmpty() } }
        }
        purpose.body.append(texts.bind(paragraph(), { it.body }), bullets)

        val xml = SectionBlock.NAME { show() }
        texts.bind(xml.heading, { it.xmlHeading })
        xml.body.append(
            texts.bind(paragraph(), { it.xmlBody }),
            row(codeText("modeler.saveXML(saveXMLOptions { format = true })")),
        )

        val next = SectionBlock.NAME { show() }
        texts.bind(next.heading, { it.nextHeading })
        next.body.append(texts.bind(paragraph(), { it.nextBody }))

        body.append(purpose.root, xml.root, next.root)
    }

    override fun applyTranslation(lang: Lang) {
        val current = overviewTexts[lang]
        setTitle(current.title)
        setSummary(current.summary)
        texts.apply(lang)
    }

    companion object : CustomElementReferenceAdapter<OverviewPage>(OverviewPage::class.js, "bpmn-overview-page")
}
