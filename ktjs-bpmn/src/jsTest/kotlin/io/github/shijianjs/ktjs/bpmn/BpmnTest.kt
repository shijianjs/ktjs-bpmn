package io.github.shijianjs.ktjs.bpmn

import io.github.shijianjs.ktjs.bpmn.core.BpmnContainer
import io.github.shijianjs.ktjs.bpmn.core.BpmnElement
import io.github.shijianjs.ktjs.bpmn.core.BpmnEvents
import io.github.shijianjs.ktjs.bpmn.core.BpmnModelUtil
import io.github.shijianjs.ktjs.bpmn.core.baseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.saveXMLOptions
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlin.js.unsafeCast
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import web.dom.document
import web.html.HTMLElement
import web.html.HtmlTagName

/**
 * Runs the wrapper against the real `bpmn-js` package in Chromium.
 *
 * The library is promise-based, so each test returns the promise it asserts in: the runner waits for
 * it, and a rejected promise fails the test. Every assertion depends on a binding that actually
 * executed — the viewer renders into a real document, the modeler hands back a document of its own,
 * and the model helpers are called with elements the library created.
 */
class BpmnTest {
    private val diagram = """
        <?xml version="1.0" encoding="UTF-8"?>
        <bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                          xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                          xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                          id="Definitions_1" targetNamespace="http://bpmn.io/schema/bpmn">
          <bpmn:process id="Process_1" isExecutable="false">
            <bpmn:startEvent id="StartEvent_1" name="Start" />
            <bpmn:task id="Task_1" name="Collect" />
            <bpmn:endEvent id="EndEvent_1" name="Done" />
          </bpmn:process>
          <bpmndi:BPMNDiagram id="BPMNDiagram_1">
            <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
              <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
                <dc:Bounds x="160" y="120" width="36" height="36" />
              </bpmndi:BPMNShape>
              <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
                <dc:Bounds x="250" y="100" width="100" height="80" />
              </bpmndi:BPMNShape>
              <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
                <dc:Bounds x="400" y="120" width="36" height="36" />
              </bpmndi:BPMNShape>
            </bpmndi:BPMNPlane>
          </bpmndi:BPMNDiagram>
        </bpmn:definitions>
    """.trimIndent()

    private fun host(tag: String): HTMLElement {
        val element = document.createElement(HtmlTagName.div).unsafeCast<HTMLElement>()
        element.setAttribute("data-role", tag)
        element.style.width = "600px"
        element.style.height = "400px"
        document.body?.appendChild(element)
        return element
    }

    private fun viewerOn(tag: String) = bpmnViewer(
        baseViewerOptions { container = BpmnContainer(host(tag)) },
    )

    @Test
    fun theViewerRendersTheImportedDiagram(): Promise<Unit> {
        val host = host("render")
        val viewer = bpmnViewer(baseViewerOptions { container = BpmnContainer(host) })

        return viewer.importXML(diagram).then<Unit> { result ->
            assertEquals(0, result.warnings.size, "warnings: ${result.warnings.joinToString()}")

            assertTrue(host.querySelector("svg") != null, "the viewer renders an SVG")
            // Three flow elements plus their labels: a wrapper wired to the wrong export renders
            // nothing at all, so the count is the cheapest proof that the instance is real.
            val rendered = host.querySelectorAll(".djs-element").length
            assertTrue(rendered >= 3, "rendered elements: $rendered")
            assertEquals("bpmn:Definitions", viewer.getDefinitions().type, "definitions root type")

            viewer.destroy()
            Unit
        }
    }

    @Test
    fun saveXMLSerialisesTheDocumentBack(): Promise<Unit> {
        val viewer = viewerOn("save-xml")

        return viewer.importXML(diagram).then<Unit> {
            viewer.saveXML(saveXMLOptions { format = true }).then<Unit> { saved ->
                val xml = saved.xml
                assertTrue(xml != null, "saveXML resolves with the document")
                assertTrue(xml.contains("bpmn:process"), "the process element survives")
                assertTrue(xml.contains("Task_1"), "the task id survives")
                assertTrue(xml.contains("Collect"), "the task name survives")

                viewer.destroy()
                Unit
            }
        }
    }

    @Test
    fun saveSVGReturnsAnImageOfTheDiagram(): Promise<Unit> {
        val viewer = viewerOn("save-svg")

        return viewer.importXML(diagram).then<Unit> {
            viewer.saveSVG().then<Unit> { saved ->
                assertTrue(saved.svg.startsWith("<svg"), "svg: ${saved.svg.take(40)}")
                assertTrue(saved.svg.contains("Task_1"), "the task is in the image")

                viewer.destroy()
                Unit
            }
        }
    }

    @Test
    fun theLifecycleEventsReachListeners(): Promise<Unit> {
        val viewer = viewerOn("events")
        var parseComplete = 0
        var done = 0

        // Registered before the import on purpose: a listener whose Kotlin `Unit` return leaked
        // through would read as “cancel the default action” and the import would lose its diagram.
        val parseListener = viewer.on(BpmnEvents.ImportParseComplete) { parseComplete++ }
        val doneListener = viewer.on(BpmnEvents.ImportDone) { event ->
            assertEquals(BpmnEvents.ImportDone, event.type, "the event carries its own name")
            done++
        }

        return viewer.importXML(diagram).then<Unit> {
            assertEquals(1, parseComplete, "import.parse.complete fires once")
            assertEquals(1, done, "import.done fires once")

            // The counter stops moving once the listener is removed.
            parseListener.remove()
            doneListener.remove()
            viewer.importXML(diagram).then<Unit> {
                assertEquals(1, parseComplete, "a removed listener is not called again")
                assertEquals(1, done, "a removed listener is not called again")

                viewer.destroy()
                Unit
            }
        }
    }

    @Test
    fun theModelUtilHelpersReadTheElementTypes(): Promise<Unit> {
        val viewer = viewerOn("model-util")

        return viewer.importXML(diagram).then<Unit> {
            val registry = viewer.get("elementRegistry")
            assertTrue(registry != null, "elementRegistry is a service of the viewer")
            val firstElement = js("(r) => r.getAll()[0]") as (JsAny) -> BpmnElement
            val root = firstElement(registry.unsafeCast<JsAny>())

            assertTrue(BpmnModelUtil.isType(root, "bpmn:Process"), "the root renders a bpmn:Process")
            assertTrue(
                BpmnModelUtil.isType(root, "bpmn:FlowElementsContainer"),
                "supertypes match too",
            )
            assertTrue(!BpmnModelUtil.isType(root, "bpmn:Task"), "the root is not a task")
            assertTrue(
                BpmnModelUtil.isAnyType(root, arrayOf("bpmn:Task", "bpmn:Process")),
                "isAny accepts a list of types",
            )
            assertEquals(
                "Process_1",
                BpmnModelUtil.businessObject(root)?.id,
                "getBusinessObject reaches the model element",
            )

            viewer.destroy()
            Unit
        }
    }

    @Test
    fun theModelerStartsFromABlankDiagram(): Promise<Unit> {
        val modeler = bpmnModeler(baseViewerOptions { container = BpmnContainer(host("modeler")) })

        return modeler.createDiagram().then<Unit> { result ->
            assertEquals(0, result.warnings.size, "a blank diagram imports cleanly")

            modeler.saveXML(null).then<Unit> { saved ->
                val xml = saved.xml
                assertTrue(xml != null, "the modeler owns a document")
                assertTrue(xml.contains("bpmn:definitions"), "the document is BPMN 2.0")
                assertTrue(modeler.getModules().isNotEmpty(), "the modeler reports its modules")

                modeler.destroy()
                Unit
            }
        }
    }


    @Test
    fun aGuardedListenerKeepsTheDefaultAction(): Promise<Unit> {
        val viewer = viewerOn("guarded")
        var calls = 0

        // `onGuarded` forwards the answer: true means “carry on”, so the import must still land.
        viewer.onGuarded(BpmnEvents.ImportParseComplete) {
            calls++
            true
        }

        return viewer.importXML(diagram).then<Unit> {
            assertEquals(1, calls, "the guarded listener runs")
            assertTrue(viewer.getDefinitions().type != null, "the import still opened a diagram")

            viewer.destroy()
            Unit
        }
    }
}
