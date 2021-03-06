package com.jetbrains.edu.learning.taskDescription.ui.styleManagers

import com.google.gson.Gson
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.ui.UIUtil
import com.jetbrains.edu.learning.JavaUILibrary.Companion.isJavaFxOrJCEF
import com.jetbrains.edu.learning.courseFormat.tasks.choice.ChoiceTask
import com.jetbrains.edu.learning.courseGeneration.GeneratorUtils
import com.jetbrains.edu.learning.stepik.getStepikLink
import com.jetbrains.edu.learning.taskDescription.ui.MULTIPLE_CHOICE_LABEL
import com.jetbrains.edu.learning.taskDescription.ui.SINGLE_CHOICE_LABEL
import kotlinx.css.*
import kotlinx.css.properties.BoxShadow
import kotlinx.css.properties.deg
import kotlinx.css.properties.lh
import kotlinx.css.properties.rotate

class ChoiceTaskResourcesManager {
  private val CHOICE_TASK_TEMPLATE = "choiceTask.html"

  val choiceTaskResources = mapOf("choice_options_style" to choiceOptionsStylesheet())

  // we are providing choice task parameters here despite inserting task object in JavaFxToolWindow addTextLoadedListener()
  // because task object is inserted after html is loaded
  private fun getResources(task: ChoiceTask) = mapOf(
    "text" to task.getChoiceLabel(),
    "selected_variants" to task.selectedVariants,
    "choice_options" to Gson().toJson(task.choiceOptions.map { it.text }),
    "is_multiple_choice" to task.isMultipleChoice
  )

  private fun ChoiceTask.getChoiceLabel() = if (isMultipleChoice) {
    MULTIPLE_CHOICE_LABEL
  }
  else {
    SINGLE_CHOICE_LABEL
  }

  fun getText(task: ChoiceTask): String = if (task.choiceOptions.isNotEmpty()) {
    GeneratorUtils.getInternalTemplateText(CHOICE_TASK_TEMPLATE, getResources(task))
  }
  else {
    "View this step on <a href=" + getStepikLink(task, task.lesson) + ">Stepik</a>."
  }

  private fun choiceOptionsStylesheet(): String {
    val styleManager = StyleManager()
    return CSSBuilder().apply {
      "code" {
        fontFamily = styleManager.codeFont
        backgroundColor = styleManager.codeBackground
        padding = "4 4 4 4"
        borderRadius = 5.px
      }
      "#choiceOptions" {
        fontFamily = styleManager.bodyFont
        fontSize = if (isJavaFxOrJCEF()) styleManager.bodyFontSize.px else styleManager.bodyFontSize.pt
        lineHeight = (styleManager.bodyLineHeight * 1.1).px.lh
        color = styleManager.bodyColor
        textAlign = TextAlign.left
        paddingLeft = 8.px
      }
      "#choiceOptions .text"{
        marginBottom = 7.px
      }
      "#choiceOptions .checkbox, .radio"{
        marginTop = 2.px
        marginRight = 9.px
        verticalAlign = VerticalAlign("middle")
        position = Position.relative
        backgroundColor = getBackgroundColor()
        borderWidth = 0.7.px
        borderColor = getBorderColor()
        borderStyle = BorderStyle.solid
      }
      "#choiceOptions .checkbox" {
        borderRadius = 3.px
        //sets size of the element
        padding = "8px"
      }
      "#choiceOptions .radio" {
        borderRadius = 50.pct
        width = 16.px
        height = 16.px
      }
      "#choiceOptions .radio:checked" {
        padding = "3.2px"
        color = styleManager.bodyColor
        backgroundColor = getRadioButtonCheckedBackgroundColor()
        borderColor = getRadioButtonCheckedBorderColor()
        borderWidth = (5.9).px
      }
      "#choiceOptions .checkbox:checked" {
        backgroundColor = getRadioButtonCheckedBorderColor()
        borderColor = getRadioButtonCheckedBorderColor()
      }
      "#choiceOptions .checkbox:checked:after" {
        display = Display.block
      }
      "#choiceOptions .checkbox:after" {
        display = Display.none
        position = Position.absolute
        content = QuotedString("")
        left = 6.px
        top = 2.px
        width = 3.px
        height = 8.px
        backgroundColor = getRadioButtonCheckedBorderColor()
        border = "solid"
        borderColor = getRadioButtonCheckedBackgroundColor()
        borderTopWidth = 0.px
        borderBottomWidth = 2.px
        borderLeftWidth = 0.px
        borderRightWidth = 2.px
        transform.rotate(35.deg)
      }
      "#choiceOptions .radio:focus, .radio:before, .radio:hover, .checkbox:focus, .checkbox:before, .checkbox:hover" {
        boxShadow += BoxShadow(false, 0.px, 0.px, 2.px, 2.px, getFocusColor())
      }
    }.toString()
      .plus("#choiceOptions .checkbox, .radio { -webkit-appearance: none; }")
      .plus(getSystemSpecificCss())
  }

  private fun getBackgroundColor() = if (UIUtil.isUnderDarcula()) Color(
    TaskDescriptionBundle.message("darcula.choice.options.background.color"))
  else Color(TaskDescriptionBundle.message("choice.options.background.color"))

  private fun getBorderColor() = if (UIUtil.isUnderDarcula()) Color(
    TaskDescriptionBundle.message("darcula.choice.options.background.color"))
  else Color(TaskDescriptionBundle.message("choice.options.border.color"))

  private fun getRadioButtonCheckedBorderColor() = if (UIUtil.isUnderDarcula()) Color(
    TaskDescriptionBundle.message("darcula.choice.options.background.color"))
  else Color(TaskDescriptionBundle.message("choice.options.checked.border.color"))

  private fun getRadioButtonCheckedBackgroundColor() = if (UIUtil.isUnderDarcula()) Color(
    TaskDescriptionBundle.message("choice.options.border.color"))
  else Color(TaskDescriptionBundle.message("choice.options.background.color"))

  private fun getFocusColor() = if (UIUtil.isUnderDarcula()) Color(TaskDescriptionBundle.message("darcula.choice.options.focus.color"))
  else Color(TaskDescriptionBundle.message("choice.options.focus.color"))

  private fun getSystemSpecificCss(): String {
    if (SystemInfo.isWindows) {
      return CSSBuilder().apply {
        "#choiceOptions .radio:checked" {
          marginRight = 7.3.px
          left = (-1).px
        }
        "#choiceOptions .checkbox:checked" {
          borderStyle = BorderStyle.none
          padding = "8.7px"
        }
      }.toString()
    }
    return ""
  }

}