/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.test.provider.impl;

import java.util.HashMap;
import java.util.Map;

import org.obiba.onyx.quartz.core.domain.answer.CategoryAnswer;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.test.provider.AnswerProvider;

/**
 * 
 */
public class ConfigurableAnswerProvider implements AnswerProvider {

  //
  // Instance Variables
  //

  private CategoryAnswer defaultAnswer;

  private Map<String, CategoryAnswer> answers;

  //
  // Constructors
  //

  public ConfigurableAnswerProvider() {
    answers = new HashMap<String, CategoryAnswer>();
  }

  //
  // AnswerProvider Methods
  //

  public CategoryAnswer getAnswer(Question question) {
    CategoryAnswer answer = answers.get(question.getName());

    if(answer == null) {
      answer = defaultAnswer;
    }

    return answer;
  }

  //
  // Methods
  //

  /**
   * Sets the default answer.
   * 
   * This is returned by <code>getAnswer</code> if no answer has been configured for the specified question.
   * 
   * @param answer default answer
   */
  public void setDefaultAnswer(CategoryAnswer answer) {
    this.defaultAnswer = answer;
  }

  /**
   * Configures the provider to return the specified answer for the specified question.
   * 
   * @param question question
   * @param answer answer
   */
  public void setAnswer(Question question, CategoryAnswer answer) {
    answers.put(question.getName(), answer);
  }
}