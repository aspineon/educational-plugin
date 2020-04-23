package com.jetbrains.edu.kotlin.hyperskill

import com.intellij.lang.Language
import com.jetbrains.edu.jvm.JdkProjectSettings
import com.jetbrains.edu.learning.stepik.hyperskill.HyperskillTaskDescriptionHighlightingTest
import org.jetbrains.kotlin.idea.KotlinLanguage

class KtHyperskillTaskDescriptionHighlightingTest : HyperskillTaskDescriptionHighlightingTest() {
  override val language: Language
    get() = KotlinLanguage.INSTANCE

  override val settings: Any
    get() = JdkProjectSettings.emptySettings()

  override val codeSample: String
    get() = """fun main() {}"""

  override val codeSampleWithHighlighting: String
    get() = """<span style="...">fun </span>main() {}"""
}