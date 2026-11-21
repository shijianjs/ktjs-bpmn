package demo.e2e

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo page: the shell comes from the kit, the modeler renders the sample
 * diagram, its buttons go through the wrapper, and the language switch relabels the page.
 *
 * The shell lives in a shadow root; Playwright's CSS pierces shadow boundaries, `document.querySelector`
 * does not — so the waits use locators instead of `waitForFunction`.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders the shell, the logo and an overview`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("ktjs-bpmn"), "title: ${page.title()}")
        assertTrue(page.locator("img.layout__logo").isVisible(), "the shell shows the project logo")
        assertEquals("Overview", page.locator("bpmn-overview-page h1").textContent())
        assertTrue(
            page.locator("vaadin-side-nav-item[data-route='modeler']").isVisible(),
            "the sidebar lists the modeler route",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `the modeler renders the sample diagram`(page: Page) {
        openPage(page)
        goToModeler(page)
        waitForDiagram(page)

        assertTrue(page.locator("[data-canvas] .djs-element").count() >= 4, "the sample has two tasks and two events")
        val status = page.locator("[data-status]").textContent() ?: ""
        assertTrue(Regex("\\d+").containsMatchIn(status), "the counter reports a number: $status")
        assertTrue(page.locator("[data-canvas] .djs-palette").isVisible(), "the modeler shows its palette")
    }

    @RetryingTest(maxAttempts = 2)
    fun `export writes the document into the panel`(page: Page) {
        openPage(page)
        goToModeler(page)
        waitForDiagram(page)
        page.locator("[data-action=export]").click()

        val xml = page.locator("[data-xml]").textContent() ?: ""
        assertTrue(xml.contains("bpmn:definitions"), "the export is a BPMN document: ${xml.take(80)}")
        assertTrue(xml.contains("Task_1"), "the exported document keeps the sample task")
    }

    @RetryingTest(maxAttempts = 2)
    fun `new diagram clears the export panel`(page: Page) {
        openPage(page)
        goToModeler(page)
        waitForDiagram(page)

        val before = page.locator("[data-canvas] .djs-element").count()
        page.locator("[data-action=export]").click()
        page.locator("[data-action=new]").click()

        val xml = page.locator("[data-xml]").textContent() ?: ""
        assertTrue(!xml.contains("Task_1"), "a blank diagram has nothing to export: ${xml.take(80)}")
        assertTrue(
            page.locator("[data-canvas] .djs-element").count() < before,
            "the canvas no longer holds the sample ($before elements before)",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language switch relabels the toolbar and keeps the diagram`(page: Page) {
        openPage(page)
        goToModeler(page)
        waitForDiagram(page)

        page.locator("[data-menu='lang']").first().click()
        page.locator("[data-action='lang:zh-Hans']").click()

        assertEquals("zh-Hans", page.locator("html").getAttribute("lang"))
        assertEquals("建模器", page.locator("vaadin-side-nav-item[data-route='modeler']").textContent())
        assertEquals("新建流程图", page.locator("[data-action=new]").textContent())
        assertTrue(
            page.locator("[data-canvas] .djs-element").count() >= 4,
            "the diagram survives the switch",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message -> if (message.type() == "error") consoleErrors += message.text() }
        page.onRequestFailed { request -> failedRequests += "${request.url()} ${request.failure()}" }

        openPage(page)
        goToModeler(page)
        waitForDiagram(page)
        page.locator("[data-action=export]").click()

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun goToModeler(page: Page) {
        page.locator("vaadin-side-nav-item[data-route='modeler']").click()
        page.waitForSelector("[data-canvas]", Page.WaitForSelectorOptions().setTimeout(20_000.0))
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }

    /** The modeler draws asynchronously; wait for the first element to be on screen. */
    private fun waitForDiagram(page: Page) {
        page.locator("[data-canvas] .djs-element").first().waitFor(Locator.WaitForOptions().setTimeout(20_000.0))
        page.locator("[data-status][data-elements]").waitFor(Locator.WaitForOptions().setTimeout(20_000.0))
    }
}
