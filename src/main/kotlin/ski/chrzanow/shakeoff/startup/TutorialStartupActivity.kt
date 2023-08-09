package ski.chrzanow.shakeoff.startup

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import ski.chrzanow.shakeoff.ShakeOffBundle.message
import ski.chrzanow.shakeoff.settings.ShakeOffSettings
import ski.chrzanow.shakeoff.utils.Notify

class TutorialStartupActivity : StartupActivity {

    private val settings = service<ShakeOffSettings>()

    override fun runActivity(project: Project) {
        if (settings.tutorialShown) {
            return
        }

        settings.tutorialShown = true

        Notify.show(
            project,
            message("notification.tutorial.title"),
            message("notification.tutorial.introduction")
                .replace(":left:", "←")
                .replace(":right:", "→"),
            NotificationType.INFORMATION,
        )
    }
}
