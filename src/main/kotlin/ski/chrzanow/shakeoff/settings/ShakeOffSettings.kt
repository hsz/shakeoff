package ski.chrzanow.shakeoff.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jdom.Element

@State(name = "ShakeOffSettings", storages = [Storage("shakeoff.xml")])
class ShakeOffSettings : PersistentStateComponent<Element?> {

    enum class KEY(private val key: String) {
        ROOT("IgnoreSettings"),
        TUTORIAL_SHOWN("tutorialShown"),
        TUTORIAL_SHOOK("tutorialShook");

        override fun toString() = key
    }

    var tutorialShown = false
    var tutorialShook = false

    override fun getState() = Element(KEY.ROOT.toString()).apply {
        setAttribute(KEY.TUTORIAL_SHOWN.toString(), tutorialShown.toString())
        setAttribute(KEY.TUTORIAL_SHOOK.toString(), tutorialShook.toString())
    }

    override fun loadState(element: Element) {
        element.apply {
            getAttributeValue(KEY.TUTORIAL_SHOWN.toString())?.let {
                tutorialShown = it.toBoolean()
            }
            getAttributeValue(KEY.TUTORIAL_SHOOK.toString())?.let {
                tutorialShook = it.toBoolean()
            }
        }
    }
}
