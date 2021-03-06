package com.jetbrains.edu.cpp

import com.jetbrains.cmake.CMakeListsFileType
import com.jetbrains.edu.learning.codeforces.courseFormat.CodeforcesCourse
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseGeneration.GeneratorUtils
import com.jetbrains.edu.learning.stepik.course.StepikCourse

/** Provides CMake file template information, where:
 * [mainCMakeList] - configures the course project, e.g. find all tasks `CMakeList.txt` files and adds them.
 * [taskCMakeList] - configures the task project.
 * [extraTopLevelFiles] - another depended on course type files that should be added to the top dir of the course.
 */
data class CppTemplates(
  val mainCMakeList: TemplateInfo,
  val taskCMakeList: TemplateInfo,
  val extraTopLevelFiles: List<TemplateInfo> = emptyList()
)

data class TemplateInfo(private val templateName: String, val generatedFileName: String) {
  private fun getTemplateVariables(projectName: String, cppStandardLine: String) = mapOf(
    CMAKE_MINIMUM_REQUIRED_LINE_KEY to CMAKE_MINIMUM_REQUIRED_LINE_VALUE,
    CMAKE_PROJECT_NAME_KEY to projectName,
    CMAKE_CPP_STANDARD_KEY to cppStandardLine,

    GTEST_VERSION_KEY to GTEST_VERSION_VALUE,
    GTEST_SOURCE_DIR_KEY to GTEST_SOURCE_DIR_VALUE,
    GTEST_BUILD_DIR_KEY to GTEST_BUILD_DIR_VALUE,

    CATCH_HEADER_URL_KEY to CATCH_HEADER_URL_VALUE,

    TEST_FRAMEWORKS_BASE_DIR_KEY to TEST_FRAMEWORKS_BASE_DIR_VALUE
  )

  fun getText(projectName: String = "", cppStandardLine: String = ""): String =
    GeneratorUtils.getInternalTemplateText(templateName, getTemplateVariables(projectName, cppStandardLine))
}

fun getCppTemplates(course: Course): CppTemplates =
  when {
    course is StepikCourse ->
      CppTemplates(TemplateInfo("StepikMainCMakeList.txt", CMakeListsFileType.FILE_NAME),
                   TemplateInfo("StepikTaskCMakeList.txt", CMakeListsFileType.FILE_NAME),
                   listOf(
                     TemplateInfo("simple_cmake_utils.cmake", GeneratorUtils.joinPaths("cmake", "utils.cmake"))
                   ))
    course is CodeforcesCourse ->
      CppTemplates(TemplateInfo("codeforces.MainCMakeLists.txt", CMakeListsFileType.FILE_NAME),
                   TemplateInfo("codeforces.TaskCMakeLists.txt", CMakeListsFileType.FILE_NAME),
                   listOf(
                     TemplateInfo("simple_cmake_utils.cmake", GeneratorUtils.joinPaths("cmake", "utils.cmake"))
                   ))
    course.environment == "GoogleTest" ->
      CppTemplates(TemplateInfo("EduMainCMakeList.txt", CMakeListsFileType.FILE_NAME),
                   TemplateInfo("EduTaskCMakeList.txt", CMakeListsFileType.FILE_NAME),
                   listOf(
                     TemplateInfo("cmake_utils.cmake", GeneratorUtils.joinPaths("cmake", "utils.cmake")),
                     TemplateInfo("cmake_googletest.cmake",
                                  GeneratorUtils.joinPaths("cmake", "googletest.cmake")),
                     TemplateInfo("cmake_googletest-download.cmake",
                                  GeneratorUtils.joinPaths("cmake", "googletest-download.cmake"))
                   ))
    course.environment == "Catch" ->
      CppTemplates(TemplateInfo("catch_CMakeLists.txt", CMakeListsFileType.FILE_NAME),
                   TemplateInfo("EduTaskCMakeList.txt", CMakeListsFileType.FILE_NAME),
                   listOf(
                     TemplateInfo("catch_cmake_utils.cmake", GeneratorUtils.joinPaths("cmake", "utils.cmake")),
                     TemplateInfo("catch_cmake_catch.cmake", GeneratorUtils.joinPaths("cmake", "catch.cmake"))
                   ))
    else ->
      throw IllegalStateException("Course must be Stepik type or have one of these environments: GoogleTest, Catch")
  }