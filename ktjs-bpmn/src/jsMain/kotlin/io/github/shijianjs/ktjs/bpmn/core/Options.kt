package io.github.shijianjs.ktjs.bpmn.core

import kotlin.js.JsAny
import kotlin.js.JsName
import kotlin.js.unsafeCast
import js.objects.unsafeJso
import web.html.HTMLElement

/**
 * The options a viewer or modeler accepts, upstream `BaseViewerOptions`.
 *
 * Built with [baseViewerOptions]; what a builder does not set stays absent, which is how bpmn-js
 * distinguishes “use the default” from “use this value”.
 */
external interface BaseViewerOptions {
    /** Diagram width: a pixel count or any CSS length. */
    var width: BpmnLength?

    /** Diagram height: a pixel count or any CSS length. */
    var height: BpmnLength?

    /** Position of the diagram inside its container, a CSS `position`. */
    var position: String?

    /** The host element, or a selector for it; `attachTo` does the same thing later. */
    var container: BpmnContainer?

    /**
     * Extra moddle packages, for XML dialects that extend BPMN 2.0 (element templates, custom
     * namespaces). Keyed by namespace prefix.
     */
    var moddleExtensions: JsAny?

    /** Extension modules: services the viewer should additionally instantiate (a `didi` module). */
    var additionalModules: Array<JsAny>?
}

/** Builds [BaseViewerOptions], setting only the fields the block assigns. */
inline fun baseViewerOptions(block: BaseViewerOptions.() -> Unit): BaseViewerOptions =
    unsafeJso(block)

/** A diagram dimension: pixels when given a number, a CSS length when given a string. */
external interface BpmnLength

/** A width or height in pixels. */
@Suppress("FunctionName")
fun BpmnLength(pixels: Double): BpmnLength = pixels.unsafeCast<BpmnLength>()

/** A width or height as a CSS length, for example `"100%"`. */
@Suppress("FunctionName")
fun BpmnLength(length: String): BpmnLength = length.unsafeCast<BpmnLength>()

/** Where a viewer mounts: an element, or a selector that resolves to one. */
external interface BpmnContainer

/** Mounts the viewer on the given element. */
@Suppress("FunctionName")
fun BpmnContainer(element: HTMLElement): BpmnContainer = element.unsafeCast<BpmnContainer>()

/** Mounts the viewer on the first element that matches the selector. */
@Suppress("FunctionName")
fun BpmnContainer(selector: String): BpmnContainer = selector.unsafeCast<BpmnContainer>()

/** Options for [io.github.shijianjs.ktjs.bpmn.BpmnBaseViewer.saveXML]. */
external interface SaveXMLOptions {
    /** Pretty-print the document instead of emitting one long line. */
    var format: Boolean?

    /** Emit the `<?xml …?>` declaration in front of the document. */
    var preamble: Boolean?
}

/** Builds [SaveXMLOptions]. */
inline fun saveXMLOptions(block: SaveXMLOptions.() -> Unit): SaveXMLOptions =
    unsafeJso(block)

/** What an import resolves with: the warnings that did not stop it. */
external interface ImportXMLResult {
    /** Human-readable warnings, empty when the document imported cleanly. */
    val warnings: Array<String>
}

/** What [io.github.shijianjs.ktjs.bpmn.BpmnBaseViewer.saveXML] resolves with. */
external interface SaveXMLResult {
    /** The BPMN 2.0 document, absent when serialisation failed. */
    val xml: String?

    /** The serialisation failure, absent when it succeeded. */
    val error: JsAny?
}

/** What [io.github.shijianjs.ktjs.bpmn.BpmnBaseViewer.saveSVG] resolves with. */
external interface SaveSVGResult {
    /** The rendered diagram as an SVG document. */
    val svg: String
}

/** An element of the BPMN 2.0 object model (a `moddle` element). */
external interface BpmnModdleElement {
    /** The element id, when the document gives it one. */
    var id: String?

    /** The type in `prefix:Name` form, for example `bpmn:Process`. */
    @JsName("\$type")
    var type: String?
}

/**
 * A rendered diagram element, the BPMN-typed counterpart of diagram-js's `Element`.
 *
 * Shape, connection and label share this surface; the geometry lives in diagram-js and is not
 * redeclared here.
 */
external interface BpmnElement {
    /** The element id. */
    var id: String?

    /** The BPMN 2.0 model object this element renders. */
    var businessObject: BpmnModdleElement?

    /** The diagram interchange object that holds the element's geometry. */
    var di: BpmnModdleElement?

    /** The diagram-js element type, for example `bpmn:Task`. */
    var type: String?
}

/**
 * The lifecycle and bus events a viewer fires, as named constants.
 *
 * Pass one of these to `on` / `off` instead of typing the string, so a typo is a compile error.
 */
object BpmnEvents {
    /** Import is about to read the document. */
    inline val ImportParseStart: String get() = "import.parse.start"

    /** The document is parsed; the event carries `definitions`, `elementsById` and `warnings`. */
    inline val ImportParseComplete: String get() = "import.parse.complete"

    /** The graphical import starts. */
    inline val ImportRenderStart: String get() = "import.render.start"

    /** The graphical import finished. */
    inline val ImportRenderComplete: String get() = "import.render.complete"

    /** The whole import is done, successfully or not: `import.done` carries `error` and `warnings`. */
    inline val ImportDone: String get() = "import.done"

    /** `saveXML` is about to serialise. */
    inline val SaveXMLStart: String get() = "saveXML.start"

    /** The XML string exists. */
    inline val SaveXMLSerialized: String get() = "saveXML.serialized"

    /** `saveXML` finished. */
    inline val SaveXMLDone: String get() = "saveXML.done"

    /** `saveSVG` is about to serialise. */
    inline val SaveSVGStart: String get() = "saveSVG.start"

    /** `saveSVG` finished. */
    inline val SaveSVGDone: String get() = "saveSVG.done"

    /** The viewer detached from its host. */
    inline val Detach: String get() = "detach"
}

/**
 * The event object a listener receives.
 *
 * Only the parts every listener may touch are declared; the payload of each event (a shape, a
 * canvas, a definitions object) is on the specific event and reached through [JsAny].
 */
external interface BpmnEvent {
    /** The event name, for example `element.click`. */
    val type: String

    /** Stops the event from reaching later listeners. */
    fun stopPropagation()

    /** Cancels the event's default action. */
    fun preventDefault()
}
