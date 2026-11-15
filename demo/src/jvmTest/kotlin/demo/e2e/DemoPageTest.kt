package demo.e2e

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import com.microsoft.playwright.options.WaitUntilState
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junitpioneer.jupiter.RetryingTest

/**
 * Smoke checks for the demo page: the bundle loads, the modeler renders the sample diagram, its
 * buttons go through the wrapper, and the language switch swaps every visible string.
 */
@UsePlaywright(DemoOptions::class)
class DemoPageTest {
    @RetryingTest(maxAttempts = 2)
    fun `page renders heading, logo and styles`(page: Page) {
        openPage(page)

        assertTrue(page.title().contains("ktjs-bpmn"), "title: ${page.title()}")
        assertEquals("ktjs-bpmn demo", page.locator("h1").textContent())
        assertTrue(page.locator("img.logo").isVisible(), "the demo serves the project logo")

        val height = (
            page.locator(".canvas").evaluate("(node) => node.getBoundingClientRect().height") as Number
            ).toDouble()
        assertTrue(height > 300, "the stylesheet gives the canvas a height ($height)")
    }

    @RetryingTest(maxAttempts = 2)
    fun `the modeler renders the sample diagram`(page: Page) {
        openPage(page)

        waitForDiagram(page)
        assertTrue(
            page.locator(".djs-element").count() >= 4,
            "the sample has three tasks and two events",
        )
        val status = page.locator(".view").textContent()
        assertTrue(Regex("\\d+").containsMatchIn(status), "the counter reports a number: $status")
        assertTrue(page.locator(".djs-palette").isVisible(), "the modeler shows its palette")
    }

    @RetryingTest(maxAttempts = 2)
    fun `export writes the document into the panel`(page: Page) {
        openPage(page)

        waitForDiagram(page)
        page.locator("[data-action=export]").click()

        val xml = page.locator("[data-role=xml]").textContent()
        assertTrue(
            xml.contains("bpmn:definitions"),
            "the export is a BPMN document: ${xml.take(80)}",
        )
        assertTrue(xml.contains("Task_1"), "the exported document keeps the sample task")
    }

    @RetryingTest(maxAttempts = 2)
    fun `new diagram clears the export panel`(page: Page) {
        openPage(page)

        waitForDiagram(page)
        val before = page.locator(".djs-element").count()
        page.locator("[data-action=export]").click()
        page.locator("[data-action=new]").click()

        val xml = page.locator("[data-role=xml]").textContent()
        assertTrue(!xml.contains("Task_1"), "a blank diagram has nothing to export: ${xml.take(80)}")
        assertTrue(
            page.locator(".djs-element").count() < before,
            "the canvas no longer holds the sample ($before elements before)",
        )
    }

    @RetryingTest(maxAttempts = 2)
    fun `the language switch swaps every visible string`(page: Page) {
        openPage(page)

        page.locator("button.lang").click()

        assertEquals("ktjs-bpmn 演示", page.locator("h1").textContent())
        assertEquals("zh-Hans", page.locator("html").getAttribute("lang"))
        assertEquals("新建流程图", page.locator(".content .row button").first().textContent())

        page.locator("button.lang").click()

        assertEquals("ktjs-bpmn demo", page.locator("h1").textContent())
        assertEquals("en", page.locator("html").getAttribute("lang"))
    }

    @RetryingTest(maxAttempts = 2)
    fun `page logs no console errors or failed requests`(page: Page) {
        val consoleErrors = mutableListOf<String>()
        val failedRequests = mutableListOf<String>()
        page.onConsoleMessage { message -> if (message.type() == "error") consoleErrors += message.text() }
        page.onRequestFailed { request -> failedRequests += "${request.url()} ${request.failure()}" }

        openPage(page)
        waitForDiagram(page)
        page.locator("[data-action=export]").click()

        assertTrue(consoleErrors.isEmpty(), "console errors: $consoleErrors")
        assertTrue(failedRequests.isEmpty(), "failed requests: $failedRequests")
    }

    private fun openPage(page: Page) {
        page.navigate("/", Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE))
    }

    /** The modeler draws asynchronously; wait for the first element to be on screen. */
    private fun waitForDiagram(page: Page) {
        page.waitForFunction(
            "() => { const shape = document.querySelector('.djs-element');" +
                " return shape !== null && shape.getBoundingClientRect().width > 0; }",
        )
    }
}
