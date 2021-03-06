package com.jetbrains.edu.coursecreator

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.jetbrains.edu.coursecreator.handlers.CCVirtualFileListener
import com.jetbrains.edu.learning.CourseSetListener
import com.jetbrains.edu.learning.StudyTaskManager
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.statistics.EduCounterUsageCollector

@Suppress("ComponentNotRegistered") // educational-core.xml
class CCProjectComponent(private val myProject: Project) : ProjectComponent {

  private fun startTaskDescriptionFilesSynchronization() {
    StudyTaskManager.getInstance(myProject).course ?: return
    EditorFactory.getInstance().eventMulticaster.addDocumentListener(SynchronizeTaskDescription(myProject), myProject)
  }

  override fun getComponentName(): String {
    return "CCProjectComponent"
  }

  override fun projectOpened() {
    val course = StudyTaskManager.getInstance(myProject).course
    if (course != null) {
      initCCProject(course)
    }
    else {
      val connection = myProject.messageBus.connect()
      connection.subscribe(StudyTaskManager.COURSE_SET, object : CourseSetListener {
        override fun courseSet(course: Course) {
          connection.disconnect()
          initCCProject(course)
        }
      })
    }
  }

  private fun initCCProject(course: Course) {
    if (CCUtils.isCourseCreator(myProject)) {
      if (!ApplicationManager.getApplication().isUnitTestMode) {
        registerListener()
        EduCounterUsageCollector.eduProjectOpened(course)
        startTaskDescriptionFilesSynchronization()
      }
    }
  }

  private fun registerListener() {
    // TODO: use some project service as parent disposable
    @Suppress("IncorrectParentDisposable")
    ApplicationManager.getApplication().messageBus
      .connect(myProject)
      .subscribe(VirtualFileManager.VFS_CHANGES, CCVirtualFileListener(myProject))
  }
}
