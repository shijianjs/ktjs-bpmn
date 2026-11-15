package io.github.shijianjs.ktjs.bpmn.core

import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsName

/**
 * The model helpers of `bpmn-js/lib/util/ModelUtil`: the four functions every bpmn-js extension
 * starts with.
 *
 * ```kotlin
 * if (BpmnModelUtil.isType(element, "bpmn:Task")) { … }
 * ```
 */
@JsModule("bpmn-js/lib/util/ModelUtil.js")
external object BpmnModelUtil {
    /**
     * `is`: whether the element is of the given BPMN type.
     *
     * The check goes through `businessObject.$instanceOf`, so subtypes match their supertypes
     * (`bpmn:UserTask` is a `bpmn:Task`).
     *
     * @param element a rendered [BpmnElement] or a [BpmnModdleElement].
     */
    @JsName("is")
    fun isType(element: JsAny, type: String): Boolean

    /** `isAny`: whether the element is of any of the given types. */
    @JsName("isAny")
    fun isAnyType(element: JsAny, types: Array<String>): Boolean

    /** `getBusinessObject`: the BPMN 2.0 object behind an element, or the argument itself. */
    @JsName("getBusinessObject")
    fun businessObject(element: JsAny): BpmnModdleElement?

    /** `getDi`: the diagram interchange object of an element. */
    @JsName("getDi")
    fun di(element: JsAny): BpmnModdleElement?
}
