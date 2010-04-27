/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.example.questionnaire.util;

import org.obiba.onyx.quartz.core.engine.questionnaire.util.QuestionnaireBuilder;
import org.obiba.onyx.util.data.DataType;

/**
 * Returns the content for the Variable Renaming Demo Questionnaire
 */
public class VariableRenamingDemoQuestionnaireContentBuilder {

  private static final String N = "N";

  private static final String Y = "Y";

  private static final String NC = "NC";

  private static final String OTHER = "OTHER";

  private static final String YES_SPECIFY = "YES_SPECIFY";

  private static final String PNA = "PNA";

  private static final String DNK = "DNK";

  public static QuestionnaireBuilder buildQuestionnaire() {
    QuestionnaireBuilder builder = QuestionnaireBuilder.createQuestionnaire("VariableRenamingDemoQuestionnaire", "1.0");

    // Specify variable name of a question.
    builder.withSection("SECTION_ONE").withSection("PARTICIPANT").withPage("1");
    builder.inPage("1").withQuestion("ABLE_TO_BALANCE_ON_ONE_FOOT").setVariableName("balance_on_one_foot").withSharedCategories(N, Y);

    // Specify the variable names of categories and shared categories. Questions have multiple answers.
    builder.inSection("PARTICIPANT").withPage("2");
    builder.inPage("2").withQuestion("ARRAY_OPEN", true);
    builder.inQuestion("ARRAY_OPEN").withQuestion("RED_WINE", true);
    builder.inQuestion("ARRAY_OPEN").withQuestion("WHITE_WINE", true);
    builder.inQuestion("ARRAY_OPEN").withCategory("WEEK").setVariableName("RED_WINE", "red_wine_week").setVariableName("WHITE_WINE", "white_wine_week");
    builder.inQuestion("ARRAY_OPEN").withSharedCategory(PNA).setVariableName("RED_WINE", "red_wine_pna").setVariableName("WHITE_WINE", "white_wine_pna");
    builder.inQuestion("ARRAY_OPEN").withSharedCategory(DNK).setVariableName("RED_WINE", "red_wine_dnk").setVariableName("WHITE_WINE", "white_wine_dnk");

    // Specify the variable name of a category. Question has a single answer.
    builder.inSection("PARTICIPANT").withPage("3").withQuestion("STANDING_HEIGHT_CI");
    builder.inQuestion("STANDING_HEIGHT_CI").withSharedCategory(NC);
    builder.inQuestion("STANDING_HEIGHT_CI").withCategory("SEVERE_SPINE_CONDITION").setVariableName("severe_spine_condition");
    builder.inQuestion("STANDING_HEIGHT_CI").withSharedCategory(OTHER);

    // Specify the variable name of an OpenAnswerDefinition.
    builder.inSection("PARTICIPANT").withPage("4").withQuestion("WAIST_HIPS_CI");
    builder.inQuestion("WAIST_HIPS_CI").withSharedCategory(NC);
    builder.inQuestion("WAIST_HIPS_CI").withSharedCategory(YES_SPECIFY).withOpenAnswerDefinition(YES_SPECIFY, DataType.TEXT).setVariableName("waist_hips_ci_reason").setSize(60);
    return builder;
  }
}