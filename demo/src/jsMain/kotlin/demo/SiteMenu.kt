package demo

import demo.page.modeler.ModelerPage
import demo.page.overview.OverviewPage
import io.github.shijianjs.ktjs.demo.nav.DemoMenu
import io.github.shijianjs.ktjs.demo.nav.Entry
import io.github.shijianjs.ktjs.demo.nav.Group
import io.github.shijianjs.ktjs.dom.element.invoke

/** The bpmn-js version this demo is built against; the navbar shows it under the site name. */
const val SITE_VERSION: String = "18.31.1"

/** The documentation site. */
const val DOCS_URL: String = "https://shijianjs.github.io/ktjs-bpmn/"

/** Dokka reference assembled by `pagesSite` next to the demo. */
const val API_URL: String = "https://shijianjs.github.io/ktjs-bpmn/api/"

/** Source repository, linked from the navbar's link menu. */
const val SOURCE_URL: String = "https://github.com/shijianjs/ktjs-bpmn"

internal val START: Group = Group("start", "lucide:sparkles")

internal val EXAMPLES: Group = Group("examples", "lucide:play")

/** The site: one entry per page. */
fun siteMenu(): DemoMenu = DemoMenu(
    listOf(
        Entry("overview", START, "lucide:sparkles") { OverviewPage.NAME() },
        Entry("modeler", EXAMPLES, "lucide:workflow") { ModelerPage.NAME() },
    ),
)
