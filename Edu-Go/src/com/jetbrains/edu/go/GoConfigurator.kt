package com.jetbrains.edu.go

import com.goide.GoIcons
import com.intellij.openapi.project.Project
import com.jetbrains.edu.go.checker.GoEduTaskChecker
import com.jetbrains.edu.learning.EduCourseBuilder
import com.jetbrains.edu.learning.EduNames.TEST
import com.jetbrains.edu.learning.checker.TaskChecker
import com.jetbrains.edu.learning.checker.TaskCheckerProvider
import com.jetbrains.edu.learning.configuration.EduConfiguratorWithSubmissions
import com.jetbrains.edu.learning.courseFormat.tasks.EduTask
import javax.swing.Icon

class GoConfigurator : EduConfiguratorWithSubmissions<GoProjectSettings>() {
  override val courseBuilder: EduCourseBuilder<GoProjectSettings> = GoCourseBuilder()
  override val testFileName: String = TEST_GO
  override fun getMockFileName(text: String): String = TASK_GO
  override val testDirs: List<String> = listOf(TEST)
  override val logo: Icon = GoIcons.ICON

  override val taskCheckerProvider: TaskCheckerProvider = object : TaskCheckerProvider {
    override fun getEduTaskChecker(task: EduTask, project: Project): TaskChecker<EduTask> = GoEduTaskChecker(project, task)
  }

  companion object {
    const val TEST_GO = "task_test.go"
    const val TASK_GO = "task.go"
    const val MAIN_GO = "main.go"
    const val GO_MOD = "go.mod"
  }
}
