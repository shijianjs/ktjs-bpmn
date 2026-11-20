package demo.page.modeler

import io.github.shijianjs.ktjs.demo.i18n.texts
import io.github.shijianjs.ktjs.dom.element.invoke

/** Every string this page shows, in one language. */
internal class ModelerTexts(
    val title: String,
    val summary: String,
    val canvasHeading: String,
    val xmlHeading: String,
    val statusPrefix: String,
    val hint: String,
    val emptyXml: String,
    /** The button labels, in the order of [ACTIONS]. */
    val labels: List<String>,
    /** The sample diagram's own texts: start event, two tasks, end event. */
    val sampleTexts: List<String>,
)
