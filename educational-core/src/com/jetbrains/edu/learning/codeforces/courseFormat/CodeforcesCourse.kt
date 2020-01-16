package com.jetbrains.edu.learning.codeforces.courseFormat

import com.jetbrains.edu.learning.actions.CheckAction
import com.jetbrains.edu.learning.codeforces.CodeforcesContestConnector.getContestURLFromID
import com.jetbrains.edu.learning.codeforces.CodeforcesLanguageProvider
import com.jetbrains.edu.learning.codeforces.CodeforcesNames
import com.jetbrains.edu.learning.codeforces.CodeforcesNames.CODEFORCES_COURSE_TYPE
import com.jetbrains.edu.learning.codeforces.ContestURLInfo
import com.jetbrains.edu.learning.configuration.EduConfigurator
import com.jetbrains.edu.learning.courseFormat.Course
import com.jetbrains.edu.learning.courseFormat.Lesson
import icons.EducationalCoreIcons
import org.jsoup.nodes.Document
import javax.swing.Icon

class CodeforcesCourse : Course {
  @Suppress("unused") //used for deserialization
  constructor()

  constructor(contestURLInfo: ContestURLInfo, doc: Document) {
    id = contestURLInfo.id
    language = contestURLInfo.languageId
    languageCode = contestURLInfo.locale

    parseResponseToAddContent(doc)
  }

  override fun getIcon(): Icon = EducationalCoreIcons.Codeforces
  override fun getId(): Int = myId
  override fun getItemType(): String = CODEFORCES_COURSE_TYPE
  override fun getCheckAction(): CheckAction = CheckAction(CodeforcesNames.RUN_LOCAL_TESTS)

  fun getContestUrl(): String = getContestURLFromID(id)
  fun getSubmissionUrl(): String = "${getContestUrl()}/submit?locale=$languageCode"

  fun getConfigurator(): EduConfigurator<*>? {
    return CodeforcesLanguageProvider.getConfigurator(languageID)
  }

  private fun parseResponseToAddContent(doc: Document) {
    name = doc.selectFirst(".caption").text()

    val problems = doc.select(".problem-statement")

    description = problems.joinToString("\n") {
      it.select("div.header").select("div.title").text()
    }

    val lesson = Lesson()
    lesson.name = CodeforcesNames.CODEFORCES_PROBLEMS
    lesson.course = this

    addLesson(lesson)
    problems.forEach { lesson.addTask(CodeforcesTask.create(it, lesson)) }
  }
}