/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.edu.coursecreator.actions.taskFile;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.diff.impl.util.LabeledEditor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.FrameWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.PathUtil;
import com.intellij.util.ui.JBUI;
import com.jetbrains.edu.coursecreator.CCUtils;
import com.jetbrains.edu.learning.EduUtils;
import com.jetbrains.edu.learning.OpenApiExtKt;
import com.jetbrains.edu.learning.PlaceholderPainter;
import com.jetbrains.edu.learning.StudyTaskManager;
import com.jetbrains.edu.learning.courseFormat.Course;
import com.jetbrains.edu.learning.courseFormat.TaskFile;
import com.jetbrains.edu.learning.courseFormat.tasks.Task;
import com.jetbrains.edu.learning.exceptions.BrokenPlaceholderException;
import com.jetbrains.edu.learning.statistics.EduCounterUsageCollector;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.intellij.openapi.ui.Messages.showErrorDialog;
import static com.intellij.openapi.ui.Messages.showInfoMessage;
import static com.jetbrains.edu.learning.EduUtils.createStudentFile;

public class CCShowPreview extends DumbAwareAction {
  private static final Logger LOG = Logger.getInstance(CCShowPreview.class.getName());

  public static final String SHOW_PREVIEW = "Preview Task File";
  public static final String NO_PREVIEW_MESSAGE = "Preview is available for task files with answer placeholders only";
  public static final String NO_PREVIEW_TITLE = "No Preview for This File";
  public static final String BROKEN_PREVIEW_TITLE = "Failed to Create Preview";

  public CCShowPreview() {
    super(SHOW_PREVIEW, SHOW_PREVIEW, null);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    presentation.setEnabledAndVisible(false);
    Project project = e.getProject();
    if (project == null) {
      return;
    }
    if (!CCUtils.isCourseCreator(project)) {
      return;
    }
    final PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (file != null) {
      TaskFile taskFile = EduUtils.getTaskFile(project, file.getVirtualFile());
      if (taskFile != null && taskFile.isVisible()) {
        presentation.setEnabledAndVisible(true);
      }
    }
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    Module module = LangDataKeys.MODULE.getData(e.getDataContext());
    if (project == null || module == null) {
      return;
    }
    final PsiFile file = CommonDataKeys.PSI_FILE.getData(e.getDataContext());
    if (file == null) {
      return;
    }
    Course course = StudyTaskManager.getInstance(project).getCourse();
    if (course == null) {
      return;
    }
    VirtualFile virtualFile = file.getVirtualFile();
    TaskFile taskFile = EduUtils.getTaskFile(project, virtualFile);
    if (taskFile == null || !taskFile.isVisible()) {
      return;
    }
    final PsiDirectory taskDir = file.getContainingDirectory();
    if (taskDir == null) {
      return;
    }
    PsiDirectory lessonDir = taskDir.getParentDirectory();
    if (lessonDir == null) {
      return;
    }

    if (taskFile.getAnswerPlaceholders().isEmpty()) {
      showInfoMessage(NO_PREVIEW_MESSAGE, NO_PREVIEW_TITLE);
      return;
    }

    final Task task = taskFile.getTask();
    ApplicationManager.getApplication().runWriteAction(() -> {
      TaskFile studentTaskFile;
      try {
        studentTaskFile = createStudentFile(project, virtualFile, task);
      }
      catch (BrokenPlaceholderException exception) {
        if (!OpenApiExtKt.isUnitTestMode()) {
          LOG.error(BROKEN_PREVIEW_TITLE + ": " + exception.getMessage());
        }
        showErrorDialog(exception.getPlaceholderInfo(), BROKEN_PREVIEW_TITLE);
        return;
      }
      if (studentTaskFile != null) {
        showPreviewDialog(project, studentTaskFile);
      }
    });
    EduCounterUsageCollector.previewTaskFile();
  }

  private static void showPreviewDialog(@NotNull Project project, @NotNull TaskFile taskFile) {
    final FrameWrapper showPreviewFrame = new FrameWrapper(project);
    final LightVirtualFile userFile = new LightVirtualFile(PathUtil.getFileName(taskFile.getName()), taskFile.getText());
    showPreviewFrame.setTitle(userFile.getName());
    LabeledEditor labeledEditor = new LabeledEditor(null);
    final EditorFactory factory = EditorFactory.getInstance();
    Document document = FileDocumentManager.getInstance().getDocument(userFile);
    if (document == null) {
      return;
    }
    final EditorEx createdEditor = (EditorEx)factory.createEditor(document, project, userFile, true);
    Disposer.register(project, () -> factory.releaseEditor(createdEditor));
    PlaceholderPainter.showPlaceholders(project, taskFile, createdEditor);
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.setBorder(JBUI.Borders.empty(10));
    header.add(new JLabel("Read-only preview."));
    String timeStamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
    header.add(new JLabel(String.format("Created %s.", timeStamp)));
    JComponent editorComponent = createdEditor.getComponent();
    labeledEditor.setComponent(editorComponent, header);
    createdEditor.setCaretVisible(false);
    createdEditor.setCaretEnabled(false);
    showPreviewFrame.setComponent(labeledEditor);
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      showPreviewFrame.setSize(new Dimension(500, 500));
      showPreviewFrame.show();
    }
  }
}