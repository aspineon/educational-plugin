package com.jetbrains.edu.learning.taskDescription

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.application.ApplicationNamesInfo
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.keymap.KeymapManager
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.openapi.keymap.impl.KeymapManagerImpl
import com.intellij.openapi.util.io.FileUtil
import com.jetbrains.edu.learning.EduTestCase
import com.jetbrains.edu.learning.EduUtils
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.DescriptionFormat
import com.jetbrains.edu.learning.courseFormat.EduCourse
import com.jetbrains.edu.learning.stepik.hyperskill.courseFormat.HyperskillCourse
import com.jetbrains.edu.learning.taskDescription.ui.BrowserWindow
import com.jetbrains.edu.learning.taskDescription.ui.EduFileEditorManagerListener
import com.jetbrains.edu.learning.taskDescription.ui.TaskDescriptionView
import org.jsoup.Jsoup
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import java.io.File
import javax.swing.KeyStroke

class TaskDescriptionTest : EduTestCase() {
  companion object {
    private val overrideMethodShortcut: String = getKeystrokeText(KeyEvent.VK_O, InputEvent.CTRL_MASK)
    private val goToActionShortcut: String = getKeystrokeText(KeyEvent.VK_A, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK)

    private fun getKeystrokeText(keyChar: Int, modifiers: Int) = KeymapUtil.getKeystrokeText(KeyStroke.getKeyStroke(keyChar, modifiers))
  }

  fun testHyperskillTagsRemoved() {
    createCourseWithHyperskillTags(courseProducer = ::HyperskillCourse)

    val taskDescription = EduUtils.getTaskTextFromTask(project, findTask(0, 0))

    val expectedTextWithoutTags = """
      text danger hint pre meta
    """.trimIndent()
    assertEquals(expectedTextWithoutTags, taskDescription)
  }

  fun testHyperskillTagsNotRemoved() {
    val expectedTextWithTags = createCourseWithHyperskillTags(courseProducer = ::EduCourse)

    val taskDescription = EduUtils.getTaskTextFromTask(project, findTask(0, 0))

    assertEquals(expectedTextWithTags, taskDescription)
  }

  private fun createCourseWithHyperskillTags(courseProducer: () -> Course): String {
    val taskText = "text [ALERT-danger]danger[/ALERT] [HINT]hint[/HINT] [PRE]pre[/PRE] [META]meta[/META]"

    courseWithFiles(courseProducer = courseProducer) {
      lesson {
        eduTask(taskDescription = taskText) {
          taskFile("taskFile1.txt")
        }
      }
    }

    return taskText
  }

  fun testIDEName() {
    createCourseWithDescription("This is %IDE_NAME%")
    val task = findTask(0, 0)
    val taskDescription = EduUtils.getTaskTextFromTask(project, task)
    assertEquals("This is ${ApplicationNamesInfo.getInstance().fullProductName}", taskDescription!!.getBody())
  }

  fun testSimpleImg() {
    doTestImage()
  }

  fun testImgInsideOtherTags() {
    doTestImage()
  }

  fun testWebImage() {
    createCourseWithDescription()
    val name = getTestName(true) + ".html"
    val fileText = FileUtil.loadFile(File(testDataPath, name))
    val initialDocument = Jsoup.parse(fileText)

    val processedText = BrowserWindow.processContent(fileText, project)
    val processedDocument = Jsoup.parse(processedText)

    val initialImgElements = initialDocument.getElementsByTag(IMG_TAG)
    val processedImageElements = processedDocument.getElementsByTag(IMG_TAG)
    processedImageElements.zip(initialImgElements)
    initialImgElements.zip(processedImageElements).map { assert(it.first.attr(SRC_ATTRIBUTE) == it.second.attr(SRC_ATTRIBUTE)) }
  }

  private fun doTestImage() {
    createCourseWithDescription()
    myFixture.openFileInEditor(findFileInTask(0, 0, "taskFile1.txt"))
    val name = getTestName(true) + ".html"
    val fileText = FileUtil.loadFile(File(testDataPath, name))
    val processedText = BrowserWindow.processContent(fileText, project)
    val document = Jsoup.parse(processedText)
    val imageElements = document.getElementsByTag(IMG_TAG)
    imageElements
      .map { assert(BrowserUtil.isAbsoluteURL(it.attr(SRC_ATTRIBUTE))) }
  }

  fun `test js script`() {
    val description = """
      |<script src="task.js">
      |</script>
      |<button onclick="change_color(this)">Click me</button>
      |
      |Solve this task
    """.trimMargin()

    createCourseWithDescription(description)
    myFixture.openFileInEditor(findFileInTask(0, 0, "taskFile1.txt"))

    val processedText = BrowserWindow.processContent(description, project)
    val document = Jsoup.parse(processedText)
    for (script in document.getElementsByTag(SCRIPT_TAG)) {
      val url = script.attr(SRC_ATTRIBUTE)

      // it means there is no src attribute in this tag
      if (url.isEmpty()) {
        continue
      }
      assertTrue("Relative path for script:\n$script", BrowserUtil.isAbsoluteURL(url))
    }
  }

  fun testShortcutRendering() {
    val taskText = "You can use &shortcut:OverrideMethods; to override methods"
    val taskTextWithShortcuts = "You can use $overrideMethodShortcut to override methods"
    doTestShortcut(taskText, taskTextWithShortcuts)
  }

  fun testShortcutRenderingMarkdown() {
    val taskText = "You can use &shortcut:OverrideMethods; to override methods"
    val taskTextWithShortcuts = "You can use $overrideMethodShortcut to override methods"
    doTestShortcut(taskText, taskTextWithShortcuts, descriptionFormat = DescriptionFormat.MD)
  }

  fun testSeveralShortcutsRendering() {
    val taskText = "You can use &shortcut:OverrideMethods; to override methods. One more useful shortcut: &shortcut:GotoAction;"
    val taskTextWithShortcuts = "You can use $overrideMethodShortcut to override methods. " +
                                "One more useful shortcut: $goToActionShortcut"
    doTestShortcut(taskText, taskTextWithShortcuts)
  }

  fun testShortcutInsideTag() {
    val taskText = "You can use <code>&shortcut:OverrideMethods;</code> to override methods. One more useful shortcut: &shortcut:GotoAction;"
    val taskTextWithShortcuts = "You can use $overrideMethodShortcut to override methods. One more useful shortcut: $goToActionShortcut"
    doTestShortcut(taskText, taskTextWithShortcuts)
  }

  fun testDoNotChangeCurrentTaskOnExternalFileOpening() {
    val taskText = "Solve"
    courseWithFiles {
      lesson {
        eduTask(taskDescription = taskText, taskDescriptionFormat = DescriptionFormat.HTML) {
          taskFile("taskFile1.txt")
        }
      }
      additionalFile("File.txt")
    }
    //forcibly subscribing to FileEditorManagerListener here because it's not called in MockTaskDescriptionView init(), otherwise
    // current task won't be set on myFixture.openFileInEditor() call
    project.messageBus.connect(testRootDisposable).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER,
                                                             EduFileEditorManagerListener(project))
    try {
      myFixture.openFileInEditor(findFileInTask(0, 0, "taskFile1.txt"))
      val firstTask = TaskDescriptionView.getInstance(project).currentTask
      myFixture.openFileInEditor(findFile("File.txt"))
      val taskOnExternalFileOpen = TaskDescriptionView.getInstance(project).currentTask
      assertEquals(firstTask, taskOnExternalFileOpen)
    }
    finally {
      TaskDescriptionView.getInstance(project).currentTask = null
    }
  }

  private fun doTestShortcut(taskText: String,
                             taskTextWithShortcuts: String,
                             keymapName: String = "Default for XWin",
                             descriptionFormat: DescriptionFormat = DescriptionFormat.HTML) {
    createCourseWithDescription(taskText, descriptionFormat)
    val oldActiveKeymap = KeymapManager.getInstance().activeKeymap
    val keymapManager = KeymapManager.getInstance() as KeymapManagerImpl
    try {
      keymapManager.activeKeymap = keymapManager.getKeymap(keymapName)!!
      val task = findTask(0, 0)
      val taskDescription = EduUtils.getTaskTextFromTask(project, task)
      assertEquals(taskTextWithShortcuts, taskDescription!!.getBody())
    }
    finally {
      keymapManager.activeKeymap = oldActiveKeymap
    }
  }

  private fun createCourseWithDescription(taskText: String = "Solve", descriptionFormat: DescriptionFormat = DescriptionFormat.HTML) {
    courseWithFiles {
      lesson {
        eduTask(taskDescription = taskText, taskDescriptionFormat = descriptionFormat) {
          taskFile("taskFile1.txt")
        }
      }
    }
  }

  override fun getTestDataPath(): String {
    return super.getTestDataPath() + "/toolWindow"
  }

  private fun String.getBody() = Jsoup.parse(this).getElementsByTag("body").text()

}