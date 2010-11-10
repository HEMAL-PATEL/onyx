/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.openAnswer;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.apache.wicket.validation.validator.MaximumValidator;
import org.apache.wicket.validation.validator.MinimumValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.obiba.onyx.core.data.ComparingDataSource;
import org.obiba.onyx.core.data.VariableDataSource;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.OpenAnswerDefinition;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireFinder;
import org.obiba.onyx.quartz.editor.locale.LabelsPanel;
import org.obiba.onyx.quartz.editor.locale.LocaleProperties;
import org.obiba.onyx.quartz.editor.locale.LocalePropertiesUtils;
import org.obiba.onyx.quartz.editor.openAnswer.validation.ValidationDataSourceWindow;
import org.obiba.onyx.quartz.editor.utils.MapModel;
import org.obiba.onyx.util.data.DataType;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.behavior.RequiredFormFieldBehavior;
import org.obiba.onyx.wicket.data.IDataValidator;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.onyx.wicket.reusable.FeedbackWindow;
import org.obiba.wicket.markup.html.table.IColumnProvider;

/**
 *
 */
@SuppressWarnings("serial")
public class OpenAnswerPanel extends Panel {

  // TODO: localize date format
  public final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  @SpringBean
  private LocalePropertiesUtils localePropertiesUtils;

  private final DropDownChoice<DataType> dataType;

  private final WebMarkupContainer minMaxContainer;

  private final TextField<String> minLength;

  private final TextField<String> maxLength;

  private final TextField<String> minNumeric;

  private final TextField<String> maxNumeric;

  private final TextField<String> beforeDate;

  private final TextField<String> afterDate;

  private SimpleFormComponentLabel minimumLabel;

  private SimpleFormComponentLabel maximumLabel;

  private OnyxEntityList<ComparingDataSource> validators;

  private final ModalWindow validatorWindow;

  private final IModel<Questionnaire> questionnaireModel;

  private final IModel<Question> questionModel;

  public OpenAnswerPanel(String id, final IModel<OpenAnswerDefinition> model, final IModel<Question> questionModel, final IModel<Questionnaire> questionnaireModel, IModel<LocaleProperties> localePropertiesModel, FeedbackPanel feedbackPanel, FeedbackWindow feedbackWindow) {
    super(id, model);
    this.questionModel = questionModel;
    this.questionnaireModel = questionnaireModel;

    final Question question = questionModel.getObject();
    final OpenAnswerDefinition openAnswer = model.getObject();

    validatorWindow = new ModalWindow("validatorWindow");
    validatorWindow.setCssClassName("onyx");
    validatorWindow.setInitialWidth(850);
    validatorWindow.setInitialHeight(450);
    validatorWindow.setResizable(true);
    validatorWindow.setTitle(new ResourceModel("Validator"));
    add(validatorWindow);

    final TextField<String> name = new TextField<String>("name", new PropertyModel<String>(model, "name"));
    name.setLabel(new ResourceModel("Name"));
    name.add(new RequiredFormFieldBehavior());
    name.add(new AbstractValidator<String>() {
      @Override
      protected void onValidate(IValidatable<String> validatable) {
        if(!StringUtils.equalsIgnoreCase(model.getObject().getName(), validatable.getValue())) {
          QuestionnaireFinder questionnaireFinder = QuestionnaireFinder.getInstance(questionnaireModel.getObject());
          if(questionnaireFinder.findOpenAnswerDefinition(validatable.getValue()) != null) {
            error(validatable, "OpenAnswerAlreadyExists");
          }
        }
      }
    });
    add(name);
    add(new SimpleFormComponentLabel("nameLabel", name));

    TextField<String> variable = new TextField<String>("variable", new MapModel<String>(new PropertyModel<Map<String, String>>(model, "variableNames"), question.getName()));
    variable.setLabel(new ResourceModel("Variable"));
    variable.add(new StringValidator.MaximumLengthValidator(20));
    add(variable);
    add(new SimpleFormComponentLabel("variableLabel", variable));

    List<DataType> typeChoices = new ArrayList<DataType>(Arrays.asList(DataType.values()));
    typeChoices.remove(DataType.BOOLEAN);
    typeChoices.remove(DataType.DATA);
    dataType = new DropDownChoice<DataType>("dataType", new PropertyModel<DataType>(model, "dataType"), typeChoices, new IChoiceRenderer<DataType>() {
      @Override
      public Object getDisplayValue(DataType type) {
        return new StringResourceModel("DataType." + type, OpenAnswerPanel.this, null).getString();
      }

      @Override
      public String getIdValue(DataType type, int index) {
        return type.name();
      }
    });

    dataType.setLabel(new ResourceModel("DataType"));
    dataType.add(new RequiredFormFieldBehavior());
    dataType.setNullValid(false);

    add(new SimpleFormComponentLabel("dataTypeLabel", dataType));
    add(dataType);

    TextField<String> unit = new TextField<String>("unit", new PropertyModel<String>(model, "unit"));
    unit.setLabel(new ResourceModel("Unit"));
    add(unit);
    add(new SimpleFormComponentLabel("unitLabel", unit));

    localePropertiesUtils.load(localePropertiesModel.getObject(), questionnaireModel.getObject(), openAnswer);
    add(new LabelsPanel("labels", localePropertiesModel, model, feedbackPanel, feedbackWindow));

    CheckBox requiredCheckBox = new CheckBox("required", new PropertyModel<Boolean>(model, "required"));
    requiredCheckBox.setLabel(new ResourceModel("AnswerRequired"));
    add(requiredCheckBox);
    add(new SimpleFormComponentLabel("requiredLabel", requiredCheckBox));

    // min/max validators
    String maxValue = null, minValue = null;
    for(IDataValidator<?> dataValidator : openAnswer.getDataValidators()) {
      IValidator<?> validator = dataValidator.getValidator();
      if(validator instanceof RangeValidator<?>) {
        RangeValidator<?> rangeValidator = (RangeValidator<?>) validator;
        Object minimum = rangeValidator.getMinimum();
        Object maximum = rangeValidator.getMaximum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(minimum != null) minValue = DATE_FORMAT.format((Date) minimum);
          if(maximum != null) maxValue = DATE_FORMAT.format((Date) maximum);
        } else {
          if(minimum != null) minValue = String.valueOf(minimum);
          if(maximum != null) maxValue = String.valueOf(maximum);
        }
      } else if(validator instanceof StringValidator.MaximumLengthValidator) {
        int maximum = ((StringValidator.MaximumLengthValidator) validator).getMaximum();
        if(maximum > 0) maxValue = String.valueOf(maximum);
      } else if(validator instanceof MaximumValidator<?>) {
        Object maximum = ((MaximumValidator<?>) validator).getMaximum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(maximum != null) maxValue = DATE_FORMAT.format((Date) maximum);
        } else {
          if(maximum != null) maxValue = String.valueOf(maximum);
        }
      } else if(validator instanceof StringValidator.MinimumLengthValidator) {
        int minimum = ((StringValidator.MinimumLengthValidator) validator).getMinimum();
        if(minimum > 0) minValue = String.valueOf(minimum);
      } else if(validator instanceof MinimumValidator<?>) {
        Object minimum = ((MinimumValidator<?>) validator).getMinimum();
        if(dataValidator.getDataType() == DataType.DATE) {
          if(minimum != null) minValue = DATE_FORMAT.format((Date) minimum);
        } else {
          if(minimum != null) minValue = String.valueOf(minimum);
        }
      }
    }

    minMaxContainer = new WebMarkupContainer("minMaxContainer");
    minMaxContainer.setOutputMarkupId(true);
    add(minMaxContainer);

    minLength = new TextField<String>("minLength", new Model<String>(minValue), String.class);
    minLength.setLabel(new ResourceModel("Minimum.length"));
    minMaxContainer.add(minLength);

    maxLength = new TextField<String>("maxLength", new Model<String>(maxValue), String.class);
    maxLength.setLabel(new ResourceModel("Maximum.length"));
    minMaxContainer.add(maxLength);

    PatternValidator numericPatternValidator = new PatternValidator("[0-9]*");
    minNumeric = new TextField<String>("minNumeric", new Model<String>(minValue), String.class);
    minNumeric.setLabel(new ResourceModel("Minimum"));
    minNumeric.add(numericPatternValidator);
    minMaxContainer.add(minNumeric);

    maxNumeric = new TextField<String>("maxNumeric", new Model<String>(maxValue), String.class);
    maxNumeric.setLabel(new ResourceModel("Maximum"));
    maxNumeric.add(numericPatternValidator);
    minMaxContainer.add(maxNumeric);

    // TODO validate date
    // PatternValidator datePatternValidator = new PatternValidator("[0-9]4-[0-9]2-[0-9]2");
    beforeDate = new TextField<String>("beforeDate", new Model<String>(minValue), String.class);
    beforeDate.setLabel(new ResourceModel("Before"));
    // beforeDate.add(datePatternValidator);
    minMaxContainer.add(beforeDate);

    afterDate = new TextField<String>("afterDate", new Model<String>(maxValue), String.class);
    afterDate.setLabel(new ResourceModel("After"));
    // afterDate.add(datePatternValidator);
    minMaxContainer.add(afterDate);

    minMaxContainer.add(minimumLabel = new SimpleFormComponentLabel("minimumLabel", minLength));
    minMaxContainer.add(maximumLabel = new SimpleFormComponentLabel("maximumLabel", maxLength));

    setMinMaxLabels(dataType.getModelObject());

    add(validators = new OnyxEntityList<ComparingDataSource>("validators", new ValidationDataSourcesProvider(), new ValidationDataSourcesColumnProvider(), new ResourceModel("Validators")));

    final AjaxLink<Void> addValidator = new AjaxLink<Void>("addValidator") {
      @Override
      public void onClick(AjaxRequestTarget target) {
        validatorWindow.setContent(new ValidationDataSourceWindow("content", new Model<ComparingDataSource>(), model, questionModel, questionnaireModel, validatorWindow) {
          @Override
          public void onSave(AjaxRequestTarget target1, ComparingDataSource comparingDataSource) {
            openAnswer.addValidationDataSource(comparingDataSource);
            target1.addComponent(validators);
          }
        });
        validatorWindow.show(target);
      }

      @Override
      public boolean isEnabled() {
        return StringUtils.isNotBlank(name.getModelObject());
      }
    };
    addValidator.setOutputMarkupId(true);
    add(addValidator.add(new Image("img", Images.ADD)));

    name.add(new OnChangeAjaxBehavior() {
      @Override
      protected void onUpdate(AjaxRequestTarget target) {
        target.addComponent(addValidator);
      }
    });

    // submit the whole form instead of just the dataType component
    dataType.add(new AjaxFormSubmitBehavior("onchange") {
      @Override
      protected void onSubmit(AjaxRequestTarget target) {
        String value = dataType.getValue(); // use value because model is not set if validation error
        setMinMaxLabels(value == null ? null : DataType.valueOf(value));
        target.addComponent(minMaxContainer);
      }

      @Override
      protected void onError(AjaxRequestTarget target) {
        Session.get().getFeedbackMessages().clear(); // we don't want to validate fields now
        onSubmit(target);
      }
    });
  }

  /**
   * 
   * @param target
   */
  public void onSave(AjaxRequestTarget target) {

  }

  @SuppressWarnings("incomplete-switch")
  private void setMinMaxLabels(DataType type) {
    if(type == null) {
      setMinimumLabel(minLength);
      setMaximumLabel(maxLength);
      minLength.setVisible(true).setEnabled(false);
      maxLength.setVisible(true).setEnabled(false);
      clearAndHide(minNumeric, maxNumeric, beforeDate, afterDate);
    } else {
      switch(type) {
      case TEXT:
        setMinimumLabel(minLength);
        setMaximumLabel(maxLength);
        minLength.setVisible(true).setEnabled(true);
        maxLength.setVisible(true).setEnabled(true);
        clearAndHide(minNumeric, maxNumeric, beforeDate, afterDate);
        break;

      case DECIMAL:
      case INTEGER:
        setMinimumLabel(minNumeric);
        setMaximumLabel(maxNumeric);
        minNumeric.setVisible(true);
        maxNumeric.setVisible(true);
        clearAndHide(minLength, maxLength, beforeDate, afterDate);
        break;

      case DATE:
        setMinimumLabel(beforeDate);
        setMaximumLabel(afterDate);
        beforeDate.setVisible(true);
        afterDate.setVisible(true);
        clearAndHide(minLength, maxLength, minNumeric, maxNumeric);
        break;
      }
    }
  }

  private void clearAndHide(FormComponent<?>... components) {
    if(components != null) {
      for(FormComponent<?> component : components) {
        component.setModelObject(null);
        component.setVisible(false);
      }
    }
  }

  private void setMinimumLabel(FormComponent<?> component) {
    SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel(minimumLabel.getId(), component);
    minimumLabel.replaceWith(newLabel);
    minimumLabel = newLabel;
  }

  private void setMaximumLabel(FormComponent<?> component) {
    SimpleFormComponentLabel newLabel = new SimpleFormComponentLabel(maximumLabel.getId(), component);
    maximumLabel.replaceWith(newLabel);
    maximumLabel = newLabel;
  }

  private class ValidationDataSourcesProvider extends SortableDataProvider<ComparingDataSource> {

    private final List<ComparingDataSource> dataSources;

    public ValidationDataSourcesProvider() {
      dataSources = ((OpenAnswerDefinition) getDefaultModelObject()).getValidationDataSources();
    }

    @Override
    public Iterator<ComparingDataSource> iterator(int first, int count) {
      return dataSources.iterator();
    }

    @Override
    public int size() {
      return dataSources.size();
    }

    @Override
    public IModel<ComparingDataSource> model(ComparingDataSource ds) {
      return new Model<ComparingDataSource>(ds);
    }

  }

  private class ValidationDataSourcesColumnProvider implements IColumnProvider<ComparingDataSource>, Serializable {

    private final List<IColumn<ComparingDataSource>> columns = new ArrayList<IColumn<ComparingDataSource>>();

    public ValidationDataSourcesColumnProvider() {
      columns.add(new AbstractColumn<ComparingDataSource>(new ResourceModel("Operator")) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new Label(componentId, new ResourceModel("Operator." + rowModel.getObject().getComparisonOperator())));
        }
      });
      columns.add(new AbstractColumn<ComparingDataSource>(new ResourceModel("Variable")) {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new Label(componentId, ((VariableDataSource) rowModel.getObject().getDataSourceRight()).getPath()));
        }
      });

      columns.add(new HeaderlessColumn<ComparingDataSource>() {
        @Override
        public void populateItem(Item<ICellPopulator<ComparingDataSource>> cellItem, String componentId, IModel<ComparingDataSource> rowModel) {
          cellItem.add(new ValidatorsActionFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<ComparingDataSource>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<ComparingDataSource>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<ComparingDataSource>> getRequiredColumns() {
      return columns;
    }

  }

  private class ValidatorsActionFragment extends Fragment {

    public ValidatorsActionFragment(String id, final IModel<ComparingDataSource> rowModel) {
      super(id, "validatorsAction", OpenAnswerPanel.this, rowModel);
      final ComparingDataSource comparingDataSource = rowModel.getObject();

      add(new AjaxLink<ComparingDataSource>("editLink", rowModel) {
        @Override
        @SuppressWarnings("unchecked")
        public void onClick(AjaxRequestTarget target) {
          validatorWindow.setContent(new ValidationDataSourceWindow("content", rowModel, (IModel<OpenAnswerDefinition>) OpenAnswerPanel.this.getDefaultModel(), questionModel, questionnaireModel, validatorWindow) {
            @Override
            public void onSave(AjaxRequestTarget target1, @SuppressWarnings("hiding") ComparingDataSource comparingDataSource) {
              target1.addComponent(validators);
            }
          });
          validatorWindow.show(target);
        }
      }.add(new Image("editImg", Images.EDIT).add(new AttributeModifier("title", true, new ResourceModel("Edit")))));

      add(new AjaxLink<ComparingDataSource>("deleteLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          OpenAnswerDefinition openAnswer = (OpenAnswerDefinition) OpenAnswerPanel.this.getDefaultModelObject();
          openAnswer.getValidationDataSources().remove(comparingDataSource);
          target.addComponent(validators);
        }
      }.add(new Image("deleteImg", Images.DELETE).add(new AttributeModifier("title", true, new ResourceModel("Delete")))));

    }
  }

}