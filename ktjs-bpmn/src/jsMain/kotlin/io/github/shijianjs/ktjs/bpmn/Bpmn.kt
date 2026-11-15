/**
 * Kotlin/JS bindings for [bpmn-js](https://github.com/bpmn-io/bpmn-js), the BPMN 2.0 viewer and
 * modeler.
 *
 * The entry points are `BpmnViewer`, `BpmnNavigatedViewer` and `BpmnModeler`; the types they use
 * live in [io.github.shijianjs.ktjs.bpmn.core].
 */
package io.github.shijianjs.ktjs.bpmn

import io.github.shijianjs.ktjs.bpmn.core.BaseViewerOptions
import io.github.shijianjs.ktjs.bpmn.core.BpmnEvent
import io.github.shijianjs.ktjs.bpmn.core.BpmnModdleElement
import io.github.shijianjs.ktjs.bpmn.core.ImportXMLResult
import io.github.shijianjs.ktjs.bpmn.core.SaveSVGResult
import io.github.shijianjs.ktjs.bpmn.core.SaveXMLOptions
import io.github.shijianjs.ktjs.bpmn.core.SaveXMLResult
import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.Promise
import kotlin.js.definedExternally
import kotlin.js.unsafeCast
import web.html.HTMLElement

/**
 * A BPMN 2.0 viewer.
 *
 * Upstream `bpmn-js/lib/Viewer`, which `bpmn-js` also re-exports as its default export.
 *
 * ```kotlin
 * val viewer = BpmnViewer(bpmnViewerOptions { container = bpmnContainer(host) })
 * viewer.importXML(xml).then { result -> println(result.warnings.size) }
 * ```
 */
@JsModule("bpmn-js/lib/Viewer.js")
external open class BpmnViewer(options: BaseViewerOptions? = definedExternally) : BpmnBaseViewer

/**
 * A viewer with mouse and keyboard navigation: panning, zooming and scrolling are enabled.
 *
 * Upstream `bpmn-js/lib/NavigatedViewer`.
 */
@JsModule("bpmn-js/lib/NavigatedViewer.js")
external class BpmnNavigatedViewer(options: BaseViewerOptions? = definedExternally) : BpmnViewer

/**
 * A BPMN 2.0 modeler: everything a viewer does, plus the palette, the context pad, undo/redo and
 * the modeling commands.
 *
 * Upstream `bpmn-js/lib/Modeler`.
 *
 * ```kotlin
 * val modeler = BpmnModeler(bpmnViewerOptions { container = bpmnContainer(host) })
 * modeler.createDiagram() // a blank diagram, ready to edit
 * ```
 */
@JsModule("bpmn-js/lib/Modeler.js")
external class BpmnModeler(options: BaseViewerOptions? = definedExternally) : BpmnBaseModeler {
    /** Creates a blank diagram to start modeling, resolving with the import warnings. */
    fun createDiagram(): Promise<ImportXMLResult>
}

/**
 * The shared base of [BpmnViewer] and [BpmnModeler].
 *
 * Upstream `bpmn-js/lib/BaseViewer`, which itself extends `diagram-js`'s `Diagram`; the service
 * accessors of that base class are declared here, so a caller never needs `diagram-js`.
 */
external open class BpmnBaseViewer {
    /**
     * Parses and renders a BPMN 2.0 document.
     *
     * The promise resolves with the warnings the import produced; a fatal problem rejects it.
     * Life-cycle events (`import.parse.start` … `import.done`) fire along the way.
     */
    fun importXML(xml: String): Promise<ImportXMLResult>

    /** [importXML], opening the given `bpmn:Definitions`' diagram instead of the first one. */
    @JsName("importXML")
    fun importXMLDiagram(xml: String, diagram: BpmnModdleElement): Promise<ImportXMLResult>

    /** [importXML], opening the diagram with the given id. */
    @JsName("importXML")
    fun importXMLDiagramById(xml: String, diagramId: String): Promise<ImportXMLResult>

    /** Renders already parsed definitions, the second half of an import. */
    fun importDefinitions(definitions: BpmnModdleElement): Promise<ImportXMLResult>

    /** Opens another diagram of the currently imported document. */
    @JsName("open")
    fun openDiagram(diagram: BpmnModdleElement): Promise<ImportXMLResult>

    /** [openDiagram], by diagram id. */
    @JsName("open")
    fun openDiagramById(diagramId: String): Promise<ImportXMLResult>

    /** Serialises the current document back to BPMN 2.0 XML. */
    fun saveXML(options: SaveXMLOptions? = definedExternally): Promise<SaveXMLResult>

    /** Renders the current document as an SVG image. */
    fun saveSVG(): Promise<SaveSVGResult>

    /** The modules the viewer was bootstrapped with, its own defaults included. */
    fun getModules(): Array<JsAny>

    /** The `bpmn:Definitions` root of the imported document. */
    fun getDefinitions(): BpmnModdleElement

    /** Attaches the viewer to a host element, which is what the `container` option does. */
    fun attachTo(parentNode: HTMLElement)

    /** Detaches the viewer and removes its DOM, keeping the imported document. */
    fun detach()

    /**
     * Subscribes to an event of the viewer's event bus, use [on][BpmnBaseViewer.on] rather than this.
     *
     * The raw signature is kept so the listener's return value is the caller's own: bpmn-js reads a
     * non-`undefined` return as “cancel the default action”, so the Kotlin `on` extension wraps
     * this and discards it. Call this directly only when you want that behaviour.
     */
    @JsName("on")
    fun onRaw(event: String, callback: (BpmnEvent) -> Any?)

    /** [onRaw], with an explicit listener priority (higher runs first, the default is 1000). */
    @JsName("on")
    fun onRawWithPriority(event: String, priority: Double, callback: (BpmnEvent) -> Any?)

    /** Removes every listener of the given event. */
    @JsName("off")
    fun off(event: String)

    /** Removes [BpmnListener.handler] from the given event, what [BpmnListener.remove] calls. */
    @JsName("off")
    fun offRaw(event: String, callback: (BpmnEvent) -> Any?)

    /** Looks a service up by name, the diagram-js dependency injection entry point. */
    fun get(service: String): JsAny?

    /** Removes the rendered diagram; the viewer stays usable for another import. */
    fun clear()

    /** Releases every listener and service the viewer created. */
    fun destroy()
}

/**
 * A registered listener.
 *
 * Remove it with [remove], which is the only way to unregister exactly this listener: the wrapper
 * hands bpmn-js a function of its own, so the callback you passed in is not the identity the event
 * bus knows.
 */
class BpmnListener internal constructor(
    private val viewer: BpmnBaseViewer,
    private val event: String,
    internal val handler: (BpmnEvent) -> Any?,
) {
    /** Unregisters this listener; calling it twice is harmless. */
    fun remove() {
        viewer.offRaw(event, handler)
    }
}

/**
 * Turns a Kotlin `Unit` listener into one bpmn-js reads as “no opinion”.
 *
 * A Kotlin/JS `Unit`-returning lambda yields the `Unit` object, not `undefined`, and the event bus
 * treats any non-`undefined` return as “stop here”: registering such a listener for
 * `import.parse.complete` makes the import lose its diagram. The shim returns nothing.
 */
private val listenerShim: ((BpmnEvent) -> Unit) -> (BpmnEvent) -> Any? =
    js("(cb) => (event) => { cb(event); }")

/** Likewise, but `false` survives: it is the documented way to cancel a default action. */
private val guardedListenerShim: ((BpmnEvent) -> Boolean) -> (BpmnEvent) -> Any? =
    js("(cb) => (event) => cb(event) === false ? false : undefined")

/**
 * Subscribes to an event of the viewer's event bus.
 *
 * ```kotlin
 * val listener = viewer.on(BpmnEvents.ImportDone) { println(it.type) }
 * listener.remove()
 * ```
 *
 * The listener's Kotlin return value is discarded, so it can neither cancel a default action nor
 * stop propagation by accident; use [onGuarded] when you mean to, and
 * [BpmnEvent.stopPropagation] to stop propagation.
 */
fun BpmnBaseViewer.on(event: String, callback: (BpmnEvent) -> Unit): BpmnListener {
    val handler = listenerShim(callback)
    onRaw(event, handler)
    return BpmnListener(this, event, handler)
}

/** [on] with an explicit listener priority: higher runs first, the default is 1000. */
fun BpmnBaseViewer.on(event: String, priority: Double, callback: (BpmnEvent) -> Unit): BpmnListener {
    val handler = listenerShim(callback)
    onRawWithPriority(event, priority, handler)
    return BpmnListener(this, event, handler)
}

/**
 * [on] for listeners that answer back: returning `false` cancels the event's default action, `true`
 * lets it happen.
 */
fun BpmnBaseViewer.onGuarded(event: String, callback: (BpmnEvent) -> Boolean): BpmnListener {
    val handler = guardedListenerShim(callback)
    onRaw(event, handler)
    return BpmnListener(this, event, handler)
}

/** The base of [BpmnModeler], upstream `bpmn-js/lib/BaseModeler`. */
external open class BpmnBaseModeler : BpmnBaseViewer

/**
 * Looks a service up and casts it, for the services a caller uses directly:
 *
 * ```kotlin
 * val canvas = modeler.service<JsAny>("canvas")!!
 * ```
 *
 * The cast is unchecked: a wrong service name fails at the first call on the value, not here.
 */
inline fun <reified T : JsAny> BpmnBaseViewer.service(name: String): T? = this.get(name)?.unsafeCast<T>()

/** Creates a [BpmnViewer] — the same as calling its constructor. */
fun bpmnViewer(options: BaseViewerOptions? = null): BpmnViewer = BpmnViewer(options)

/** Creates a [BpmnNavigatedViewer]. */
fun bpmnNavigatedViewer(options: BaseViewerOptions? = null): BpmnNavigatedViewer =
    BpmnNavigatedViewer(options)

/** Creates a [BpmnModeler]. */
fun bpmnModeler(options: BaseViewerOptions? = null): BpmnModeler = BpmnModeler(options)
