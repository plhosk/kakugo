package org.kaqui_plhosk.testactivities

import android.view.View
import org.kaqui_plhosk.TestEngine
import org.kaqui_plhosk.model.Certainty
import org.kaqui_plhosk.model.Item
import org.kaqui_plhosk.model.TestType

interface TestFragmentHolder {
    val testEngine: TestEngine
    val testType: TestType
        get() = testEngine.testType

    fun onAnswer(button: View?, certainty: Certainty, wrong: Item?)
    fun nextQuestion()
}
