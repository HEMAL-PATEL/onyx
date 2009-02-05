/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
package org.obiba.onyx.quartz.core.wicket.layout.impl.simplified;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.model.IModel;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.wicket.layout.impl.AbstractQuestionCategoriesView;
import org.obiba.onyx.quartz.core.wicket.layout.impl.DefaultEscapeQuestionCategoriesPanel;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryEscapeFilter;
import org.obiba.onyx.quartz.core.wicket.layout.impl.util.QuestionCategoryListToGridPermutator;
import org.obiba.onyx.quartz.core.wicket.layout.impl.validation.AnswerCountValidator;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireStringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Panel containing the question categories in a grid view of image buttons to be selected (multiple selection or not),
 * without open answers.
 */
public class SimplifiedQuestionCategoriesPanel extends Panel implements IQuestionCategorySelectionListener {

  private static final long serialVersionUID = 5144933183339704600L;

  @SuppressWarnings("unused")
  private static final Logger log = LoggerFactory.getLogger(SimplifiedQuestionCategoriesPanel.class);

  /**
   * Context in which answer are given (case of joined categories question array).
   */
  @SuppressWarnings("unused")
  private IModel parentQuestionCategoryModel;

  /**
   * Constructor for a stand-alone question.
   * @param id
   * @param questionModel
   */
  public SimplifiedQuestionCategoriesPanel(String id, IModel questionModel) {
    this(id, questionModel, null);
  }

  /**
   * Constructor for a joined categories question.
   * @param id
   * @param questionModel
   * @param parentQuestionCategoryModel
   */
  @SuppressWarnings("serial")
  public SimplifiedQuestionCategoriesPanel(String id, IModel questionModel, IModel parentQuestionCategoryModel) {
    super(id, questionModel);
    setOutputMarkupId(true);

    this.parentQuestionCategoryModel = parentQuestionCategoryModel;

    Question question = (Question) getModelObject();

    // seams like ugly but we need a form component to run the answer count validator
    final CheckGroup checkGroup = new CheckGroup("categories", new ArrayList<IModel>());
    checkGroup.setLabel(new QuestionnaireStringResourceModel(question, "label"));
    checkGroup.add(new AnswerCountValidator(getQuestionModel()));
    add(checkGroup);

    GridView repeater = new AbstractQuestionCategoriesView("category", getModel(), new QuestionCategoryEscapeFilter(false), new QuestionCategoryListToGridPermutator(getModel())) {

      @Override
      protected void populateItem(Item item) {
        if(item.getModel() == null) {
          item.add(new EmptyPanel("input").setVisible(false));
        } else {
          item.add(new QuestionCategoryLinkPanel("input", item.getModel()));
        }
      }

    };
    checkGroup.add(repeater);

    if(hasEscapeQuestionCategories()) {
      add(new SimplifiedEscapeQuestionCategoriesPanel("escapeCategories", getQuestionModel()));
    } else {
      add(new EmptyPanel("escapeCategories").setVisible(false));
    }
  }

  /**
   * Escape categories are presented in an additional radio grid view if any.
   * @return
   * @see DefaultEscapeQuestionCategoriesPanel
   */
  private boolean hasEscapeQuestionCategories() {
    return ((Question) getModelObject()).hasEscapeCategories();
  }

  private IModel getQuestionModel() {
    return getModel();
  }

  public void onQuestionCategorySelection(AjaxRequestTarget target, IModel questionModel, IModel questionCategoryModel, boolean isSelected) {
    log.info("onQuestionCategorySelection({}, {}, {})", new Object[] { questionModel, questionCategoryModel, isSelected });
    target.addComponent(this);
  }
}
