package ski.chrzanow.shakeoff.startup

import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.ui.DialogEarthquakeShaker
import com.intellij.openapi.wm.WindowManager
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyEvent.*
import java.awt.event.MouseEvent
import java.awt.event.MouseEvent.MOUSE_MOVED
import kotlin.math.abs
import kotlin.math.sign

class ShakeOffStartupActivity : StartupActivity {

    private val lastDirections = IntArray(3)
    private val timeStamps = LongArray(3) { System.currentTimeMillis() }

    private var lastMousePoint = Point()
    private var lastDirection = 0
    private var directionChangeCounter = 0
    private var lastChangeTime = 0L
    private val shakeTimeThreshold = 1000 // in milliseconds
    private val shakeCountsThreshold = 5 // how many direction changes
    private val shakeDistanceThreshold = 20 // pixel distance

    override fun runActivity(project: Project) {
        IdeEventQueue.getInstance().addDispatcher({ event ->
            val time = System.currentTimeMillis()

            when (event) {
                is MouseEvent -> {
                    if (event.id == MOUSE_MOVED) {
                        val dx = event.point.x - lastMousePoint.x
                        if (abs(dx) > shakeDistanceThreshold) {
                            val currentDirection = sign(dx.toDouble()).toInt()
                            if (currentDirection != lastDirection) {
                                if (directionChangeCounter == 0 || System.currentTimeMillis() - lastChangeTime <= shakeTimeThreshold) {
                                    directionChangeCounter++
                                } else {
                                    directionChangeCounter = 1
                                }

                                lastChangeTime = System.currentTimeMillis()
                                lastDirection = currentDirection

                                if (directionChangeCounter >= shakeCountsThreshold) {
                                    shakeOff(project)
                                    clearData()
                                }
                            }
                            lastMousePoint = event.point
                        }
                    }
                }

                is KeyEvent -> {
                    if (event.id == KEY_PRESSED && (event.keyCode == VK_LEFT || event.keyCode == VK_RIGHT)) {
                        val direction = if (event.keyCode == VK_RIGHT) 1 else -1
                        updateDirectionAndTimeStamps(direction, time)
                        checkAndTriggerShakeOff(project)
                    }
                }
            }
            false
        }, ApplicationManager.getApplication())
    }

    private fun updateDirectionAndTimeStamps(direction: Int, time: Long) {
        lastDirections.copyInto(lastDirections, 0, 1, lastDirections.lastIndex + 1)
        timeStamps.copyInto(timeStamps, 0, 1, timeStamps.lastIndex + 1)
        lastDirections[lastDirections.lastIndex] = direction
        timeStamps[timeStamps.lastIndex] = time
    }

    private fun checkAndTriggerShakeOff(project: Project) {
        val timeFrame = timeStamps[timeStamps.lastIndex] - timeStamps[0]
        if (timeFrame <= 1000 && isSequenceValid()) {
            shakeOff(project)
            clearData()
        }
    }

    private fun isSequenceValid() =
        lastDirections
            .toList()
            .zipWithNext()
            .all { (a, b) -> a == -b }

    private fun clearData() {
        lastDirections.fill(0)
        timeStamps.fill(System.currentTimeMillis())
        directionChangeCounter = 0
    }

    private fun shakeOff(project: Project) = ApplicationManager.getApplication().invokeLater {
        WindowManager.getInstance()
            .suggestParentWindow(project)
            ?.let(DialogEarthquakeShaker::shake)

        with(ActionManager.getInstance()) {
            tryToExecute(getAction("ClearAllNotifications"), null, null, null, true)
        }
    }
}
