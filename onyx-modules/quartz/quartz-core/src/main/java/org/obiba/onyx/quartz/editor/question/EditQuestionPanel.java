/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.question;

import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.ARRAY_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_CHECKBOX;
import static org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType.LIST_DROP_DOWN;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.extensions.markup.html.tabs.PanelCachingTab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Category;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionCategory;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.QuestionType;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.wicket.layout.impl.standard.DropDownQuestionPanelFactory;
import org.obiba.onyx.quartz.editor.category.CategoriesPanel;
import org.obiba.onyx.quartz.editor.category.CategoryListPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswerDefinition.OpenAnswerPanel;
import org.obiba.onyx.quartz.editor.question.array.ArrayRowsPanel;
import org.obiba.onyx.quartz.editor.question.condition.ConditionPanel;
import org.obiba.onyx.quartz.editor.questionnaire.QuestionnairePersistenceUtils;
import org.obiba.onyx.quartz.editor.utils.tab.AjaxSubmitTabbedPanel;
import org.obiba.onyx.quartz.editor.utils.tab.HidableTab;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@SuppressWarnings("serial")
public abstract class EditQuestionPanel extends Panel {

  private transient Logger logger = LoggerFactory.getLogger(getClass());

  @SpringBean
  private transient LocalePropertiesUtils localePropertiesUtils;

  @SpringBean
  private transient QuestionnairePersistenceUtils questionnairePersistenceUtils;

  private final FeedbackPanel feedbackPanel;

  private final FeedbackWindow feedbackWindow;

  private final Form<EditedQuestion> form;

  private final IModel<LocaleProperties> localePropertiesModel;

  private final AjaxSubmitTabbedPanel tabbedPanel;

  private final IModel<Questionnaire> questionnaireModel;

  private final SavableHidableTab openAnswerTab;

  private final HidableTab categoriesTab;

  private final HidableTab rowsTab;

  private final HidableTab columnsTab;

  public EditQuestionPanel(String id, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, final ModalWindow questionWindow) {
    super(id);
    this.questionnaireModel = questionnaireModel;

    final Question question = questionModel.getObject();

    EditedQuestion editedQuestion = null;
    if(StringUtils.isBlank(question.getName())) {
      editedQuestion = new EditedQuestion(null);
      editedQuestion.setElement(question);
    } else {
      editedQuestion = new EditedQuestion(question);
    }

    final IModel<EditedQuestion> model = new Model<EditedQuestion>(editedQuestion);

    setDefaultModel(model);

    localePropertiesModel = new Model<LocaleProperties>(localePropertiesUtils.load(questionnaireModel.getObject(), question));

    feedbackPanel = new FeedbackPanel("content");
    feedbackWindow = new FeedbackWindow("feedback");
    feedbackWindow.setOutputMarkupId(true);

    add(feedbackWindow);

    add(form = new Form<EditedQuestion>("form", model));

    final List<ITab> tabs = new ArrayList<ITab>();

    openAnswerTab = new SavableHidableTab(new ResourceModel("OpenAnswer")) {

      private OpenAnswerPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        final OpenAnswerDefinition openAnswerDefinition;
        if(panel == null) {
          final List<Category> categories = question.getCategories();
          if(categories.isEmpty()) {
            openAnswerDefinition = new OpenAnswerDefinition();
          } else {
            openAnswerDefinition = categories.get(0).getOpenAnswerDefinition();
          }
          panel = new OpenAnswerPanel(panelId, new Model<OpenAnswerDefinition>(openAnswerDefinition), questionModel, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow) {
            @Override
            public void onSave(AjaxRequestTarget target) {
              if(categories.isEmpty()) {
                Category category = new Category(openAnswerDefinition.getName());
                category.setOpenAnswerDefinition(openAnswerDefinition);
                QuestionCategory questionCategory = new QuestionCategory();
                questionCategory.setCategory(category);
                question.addQuestionCategory(questionCategory);
              }
            }
          };
        }
        return panel;
      }

      @Override
      public void save(AjaxRequestTarget target) {
        if(panel != null) panel.onSave(target);
      }
    };
    openAnswerTab.setVisible(false);

    categoriesTab = new HidableTab(new ResourceModel("Categories")) {
      private CategoriesPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new CategoriesPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }

    };
    categoriesTab.setVisible(false);

    rowsTab = new HidableTab(new ResourceModel("Rows(questions)")) {
      private ArrayRowsPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new ArrayRowsPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }
    };
    rowsTab.setVisible(false);

    columnsTab = new HidableTab(new ResourceModel("Columns(categories)")) {
      private CategoryListPanel panel;

      @Override
      public Panel getPanel(String panelId) {
        if(panel == null) {
          panel = new CategoryListPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow);
        }
        return panel;
      }
    };
    columnsTab.setVisible(false);

    ITab questionTab = new AbstractTab(new ResourceModel("Question")) {
      @Override
      public Panel getPanel(String panelId) {
        return new QuestionPanel(panelId, model, questionnaireModel, localePropertiesModel, feedbackPanel, feedbackWindow, true) {
          @Override
          public void onQuestionTypeChange(AjaxRequestTarget target, QuestionType questionType) {
            setTabsVisibility(questionType);
            if(tabbedPanel != null && target != null) {
              target.addComponent(tabbedPanel);
            }
          }
        };
      }
    };

    tabs.add(new PanelCachingTab(questionTab));
    tabs.add(new PanelCachingTab(openAnswerTab));
    tabs.add(categoriesTab);
    tabs.add(rowsTab);
    tabs.add(columnsTab);
    tabs.add(new AbstractTab(new ResourceModel("Conditions")) {
      @Override
      public Panel getPanel(String panelId) {
        return new ConditionPanel(panelId, questionModel, questionnaireModel);
      }
    });
    tabs.add(new AbstractTab(new ResourceModel("Preview")) {
      @Override
      public Panel getPanel(String panelId) {
        return new QuestionPreviewPanel(panelId, model, questionnaireModel);
      }
    });

    setTabsVisibility(editedQuestion.getQuestionType());

    form.add(tabbedPanel = new AjaxSubmitTabbedPanel("tabs", feedbackPanel, feedbackWindow, tabs));

    form.add(new AjaxButton("save", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        QuestionType questionType = form.getModelObject().getQuestionType();
        if(questionType != null) {
          int nbCategories = question.getCategories().size();
          switch(questionType) {
          case SINGLE_OPEN_ANSWER:
            openAnswerTab.save(target);
            if(nbCategories == 0 || question.getCategories().get(0).getOpenAnswerDefinition() == null) {
              form.error(new StringResourceModel("Validator.SingleOpenAnswerNotDefined", EditQuestionPanel.this, null).getObject());
            }
            break;

          case LIST_CHECKBOX:
          case LIST_RADIO:
          case LIST_DROP_DOWN:
            question.setUIFactoryName(questionType == LIST_DROP_DOWN ? new DropDownQuestionPanelFactory().getBeanName() : null);
            question.setMultiple(questionType == LIST_CHECKBOX);
            if(nbCategories < 2) {
              form.error(new StringResourceModel("Validator.ListNotDefined", EditQuestionPanel.this, null).getObject());
            }
            break;

          case ARRAY_CHECKBOX:
          case ARRAY_RADIO:
            question.setMultiple(questionType == ARRAY_CHECKBOX);
            if(question.getQuestions().size() < 2 || nbCategories < 1) {
              form.error(new StringResourceModel("Validator.ArrayNotDefined", EditQuestionPanel.this, null).getObject());
            }
            break;

          case BOILER_PLATE:
            break;
          }
        }

        onSave(target, form.getModelObject());
        questionWindow.close(target);
      }

      @Override
      protected void onError(AjaxRequestTarget target, Form<?> form2) {
        feedbackWindow.setContent(feedbackPanel);
        feedbackWindow.show(target);
      }
    });

    form.add(new AjaxButton("cancel", form) {
      @Override
      public void onSubmit(AjaxRequestTarget target, Form<?> form2) {
        questionWindow.close(target);
      }
    }.setDefaultFormProcessing(false));

  }

  private void setTabsVisibility(QuestionType questionType) {
    if(questionType == null) return;
    switch(questionType) {
    case SINGLE_OPEN_ANSWER:
      openAnswerTab.setVisible(true);
      categoriesTab.setVisible(false);
      rowsTab.setVisible(false);
      columnsTab.setVisible(false);
      break;

    case LIST_CHECKBOX:
    case LIST_RADIO:
    case LIST_DROP_DOWN:
      categoriesTab.setVisible(true);
      openAnswerTab.setVisible(false);
      rowsTab.setVisible(false);
      columnsTab.setVisible(false);
      break;

    case ARRAY_CHECKBOX:
    case ARRAY_RADIO:
      rowsTab.setVisible(true);
      columnsTab.setVisible(true);
      openAnswerTab.setVisible(false);
      categoriesTab.setVisible(false);
      break;

    case BOILER_PLATE:
      break;
    }
  }

  /**
   * 
   * @param target
   * @param editedQuestion
   */
  public abstract void onSave(AjaxRequestTarget target, EditedQuestion editedQuestion);

  // {
  // final Question question = editedQuestion.getElement();
  //
  // if(form.getModelObject().getElement().getParentQuestion() == null) {
  // categoryList.save(target, new SortableListCallback<QuestionCategory>() {
  // @Override
  // public void onSave(List<QuestionCategory> orderedItems, AjaxRequestTarget target1) {
  // question.getQuestionCategories().clear();
  // for(QuestionCategory questionCategory : orderedItems) {
  // question.getQuestionCategories().add(questionCategory);
  // }
  // }
  // });
  // }
  //
  // // Layout single or grid: make sure that the categories are added before this...
  // String layoutSelection = layoutRadioGroup.getModelObject();
  // if(SINGLE_COLUMN_LAYOUT.equals(layoutSelection)) {
  // question.clearUIArguments();
  // question.addUIArgument(ROW_COUNT_KEY, question.getCategories().size() + "");
  // } else if(GRID_LAYOUT.equals(layoutSelection)) {
  // question.clearUIArguments();
  // question.addUIArgument(ROW_COUNT_KEY, nbRowsField.getModelObject() + "");
  // }
  //
  // editedQuestion.setConditions(((Conditions) conditionPanel.getDefaultModelObject()));
  // }

  public void persist(AjaxRequestTarget target) {
    try {
      questionnairePersistenceUtils.persist(questionnaireModel.getObject(), localePropertiesModel.getObject());
    } catch(Exception e) {
      logger.error("Cannot persist questionnaire", e);
      error(e.getMessage());
      feedbackWindow.setContent(feedbackPanel);
      feedbackWindow.show(target);
    }
  }
}
