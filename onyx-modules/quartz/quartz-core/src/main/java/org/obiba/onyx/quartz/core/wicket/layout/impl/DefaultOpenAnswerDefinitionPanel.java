/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.quartz.core.domain.answer.OpenAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.DataValidator;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.wicket.data.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultOpenAnswerDefinitionPanel extends AbstractOpenAnswerDefinitionPanel {

  private static final long serialVersionUID = 8950481253772691811L;

  private static final Logger log = LoggerFactory.getLogger(DefaultOpenAnswerDefinitionPanel.class);

  @SpringBean
  private ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private DataField openField;

  /**
   * Constructor given the question category (needed for persistency).
   * @param id
   * @param questionCategoryModel
   * @param openAnswerDefinitionModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionCategoryModel) {
    super(id, questionCategoryModel);
    initialize();
  }

  /**
   * Constructor.
   * 
   * @param id
   * @param questionModel
   * @param questionCategoryModel
   */
  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel) {
    super(id, questionModel, questionCategoryModel);
    initialize();
  }

  public DefaultOpenAnswerDefinitionPanel(String id, IModel questionModel, IModel questionCategoryModel, IModel openAnswerDefinitionModel) {
    super(id, questionModel, questionCategoryModel, openAnswerDefinitionModel);
    initialize();
  }

  private void initialize() {
    setOutputMarkupId(true);

    OpenAnswer previousAnswer = activeQuestionnaireAdministrationService.findOpenAnswer(getQuestion(), getQuestionCategory().getCategory(), getOpenAnswerDefinition());
    if(previousAnswer != null) {
      setData(previousAnswer.getData());
    }

    QuestionnaireStringResourceModel openLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "label");
    QuestionnaireStringResourceModel unitLabel = new QuestionnaireStringResourceModel(getOpenAnswerDefinitionModel(), "unitLabel");

    add(new Label("label", openLabel));

    if(getOpenAnswerDefinition().getDefaultValues().size() > 1) {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), getOpenAnswerDefinition().getDefaultValues(), new IChoiceRenderer() {

        public Object getDisplayValue(Object object) {
          Data data = (Data) object;
          return (String) new QuestionnaireStringResourceModel(new PropertyModel(DefaultOpenAnswerDefinitionPanel.this.getModel(), "category.openAnswerDefinition"), data.getValueAsString()).getObject();
        }

        public String getIdValue(Object object, int index) {
          Data data = (Data) object;
          return data.getValueAsString();
        }

      }, unitLabel.getString());
    } else if(getOpenAnswerDefinition().getDefaultValues().size() > 0) {
      setData(getOpenAnswerDefinition().getDefaultValues().get(0));
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    } else {
      openField = new DataField("open", new PropertyModel(this, "data"), getOpenAnswerDefinition().getDataType(), unitLabel.getString());
    }

    if(getOpenAnswerDefinition().getValidators() != null) {
      for(DataValidator validator : getOpenAnswerDefinition().getValidators()) {
        openField.add(validator);
      }
    }

    // TODO check if open answer is always required when defined ?
    openField.setRequired(true);
    add(openField);

    openField.add(new AjaxFormComponentUpdatingBehavior("onblur") {

      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        log.info("openField.onUpdate.{}.data={}", getQuestion() + ":" + getQuestionCategory() + ":" + getOpenAnswerDefinition().getName(), getData());
        // persist data
        activeQuestionnaireAdministrationService.answer(getQuestion(), getQuestionCategory(), getOpenAnswerDefinition(), getData());
        DefaultOpenAnswerDefinitionPanel.this.onSubmit(target, getQuestionModel(), DefaultOpenAnswerDefinitionPanel.this.getModel());
      }

    });

    openField.add(new AjaxEventBehavior("onclick") {

      @Override
      protected void onEvent(AjaxRequestTarget target) {
        log.info("openField.onClick");
        DefaultOpenAnswerDefinitionPanel.this.onSelect(target, getQuestionModel(), getQuestionCategoryModel(), getOpenAnswerDefinitionModel());
        openField.focusField(target);
      }

    });

    // set the label of the field
    QuestionnaireStringResourceModel questionCategoryLabel = new QuestionnaireStringResourceModel(getQuestionCategory(), "label");
    QuestionnaireStringResourceModel questionLabel = new QuestionnaireStringResourceModel(getQuestionModel(), "label");
    if(!getQuestionCategory().getQuestion().getName().equals(getQuestion().getName())) {
      openField.setLabel(new Model(questionLabel.getString() + " / " + questionCategoryLabel.getString()));
    } else if(!isEmptyString(openLabel.getString())) {
      openField.setLabel(openLabel);
    } else if(!isEmptyString(unitLabel.getString())) {
      openField.setLabel(unitLabel);
    } else if(!isEmptyString(questionCategoryLabel.getString())) {
      openField.setLabel(questionCategoryLabel);
    } else {
      // last chance : the question label !
      openField.setLabel(questionLabel);
    }
  }

  @Override
  public int hashCode() {
    return super.hashCode() + getModel().hashCode();
  }

  /**
   * Equals if they refer to the an equal model.
   */
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof DefaultOpenAnswerDefinitionPanel) {
      return this.getModel() != null && this.getModel().equals(((DefaultOpenAnswerDefinitionPanel) obj).getModel());
    }
    return super.equals(obj);
  }

  private boolean isEmptyString(String str) {
    return str == null || str.trim().length() == 0;
  }

  public void setRequired(boolean required) {
    log.info("required={}", required);
    openField.setRequired(required);
  }

  public void setFieldEnabled(boolean enabled) {
    openField.setFieldEnabled(enabled);
  }

  public boolean isFieldEnabled() {
    return openField.isFieldEnabled();
  }

}
