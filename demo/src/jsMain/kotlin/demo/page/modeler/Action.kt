package demo.page.modeler

import io.github.shijianjs.ktjs.dom.element.invoke

/** One toolbar button: what it does to the modeler, and what it does to the readouts. */
internal class Action(val id: String, val run: (ModelerPage, Modeler) -> Unit)
