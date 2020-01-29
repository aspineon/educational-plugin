package com.jetbrains.edu.scala.sbt

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.edu.jvm.JdkProjectSettings
import com.jetbrains.edu.learning.EduCourseBuilder
import com.jetbrains.edu.learning.EduNames
import com.jetbrains.edu.learning.EduUtils
import com.jetbrains.edu.learning.checker.TaskChecker
import com.jetbrains.edu.learning.checker.TaskCheckerProvider
import com.jetbrains.edu.learning.configuration.EduConfiguratorWithSubmissions
import com.jetbrains.edu.learning.courseFormat.tasks.EduTask
import com.jetbrains.edu.learning.courseGeneration.GeneratorUtils.getInternalTemplateText
import icons.EducationalCoreIcons
import javax.swing.Icon

class ScalaSbtConfigurator : EduConfiguratorWithSubmissions<JdkProjectSettings>() {
  override val courseBuilder: EduCourseBuilder<JdkProjectSettings> = ScalaSbtCourseBuilder()
  override val testFileName: String = TEST_SCALA
  override val isEnabled: Boolean = !EduUtils.isAndroidStudio()

  override val taskCheckerProvider: TaskCheckerProvider = object : TaskCheckerProvider {
    override fun getEduTaskChecker(task: EduTask, project: Project): TaskChecker<EduTask> = ScalaSbtEduTaskChecker(task, project)
  }

  override val mockTemplate: String = getInternalTemplateText(MOCK_SCALA)
  override val sourceDir: String = EduNames.SRC
  override val testDirs: List<String> = listOf(EduNames.TEST)
  override val logo: Icon = EducationalCoreIcons.ScalaLogo

  override fun excludeFromArchive(project: Project, file: VirtualFile): Boolean {
    if (super.excludeFromArchive(project, file)) return true
    return generateSequence(file, VirtualFile::getParent).any { it.name == "target" || it.name == "project" }
  }

  companion object {
    const val TEST_SCALA = "TestSpec.scala"
    const val TASK_SCALA = "Task.scala"
    const val MOCK_SCALA = "Mock.scala"
  }
}
