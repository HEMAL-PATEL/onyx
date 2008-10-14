package org.obiba.onyx.quartz.core.engine.questionnaire.util;

import java.util.Properties;

import org.obiba.onyx.quartz.core.engine.questionnaire.question.Page;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Question;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Section;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.AbstractQuestionnaireElementBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.IPropertyKeyWriter;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.PageBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.QuestionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.PropertyKeyWriterVisitor;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.SectionBuilder;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.builder.impl.PropertiesPropertyKeyWriterImpl;
import org.obiba.onyx.quartz.core.engine.questionnaire.util.localization.IPropertyKeyProvider;
import org.obiba.runtime.Version;

/**
 * The {@link Questionnaire} main builder.
 * @author Yannick Marcon
 * 
 */
public class QuestionnaireBuilder extends AbstractQuestionnaireElementBuilder<Questionnaire> {

  /**
   * Constructor.
   * @param name
   * @param version
   * @throws IllegalArgumentException if name does not respect naming pattern and if version does not respect
   * versionning pattern.
   * @see Version
   */
  private QuestionnaireBuilder(String name, String version) {
    super(null);
    if(!checkNamePattern(name)) {
      throw invalidNamePatternException(name);
    }
    this.element = new Questionnaire(name, version);
    this.questionnaire = this.element;
  }

  private QuestionnaireBuilder(Questionnaire questionnaire) {
    super(questionnaire);
    this.element = questionnaire;
  }

  /**
   * Create a new {@link Questionnaire}.
   * @param name
   * @param version
   * @return
   * @throws IllegalArgumentException if name does not respect naming pattern and if version does not respect
   * versionning pattern.
   * @see Version
   */
  public static QuestionnaireBuilder createQuestionnaire(String name, String version) {
    return new QuestionnaireBuilder(name, version);
  }

  /**
   * Get an instance on the builder given a questionnaire.
   * @param questionnaire
   * @return
   */
  public static QuestionnaireBuilder getInstance(Questionnaire questionnaire) {
    return new QuestionnaireBuilder(questionnaire);
  }

  /**
   * Add a top level {@link Section} to current {@link Questionnaire}, and make it the current {@link Section}
   * @param name
   * @return
   */
  public SectionBuilder withSection(String name) {
    return SectionBuilder.createSection(this, name);
  }

  /**
   * Position the builder to the {@link Section} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no section can be found with this name
   */
  public SectionBuilder inSection(String name) {
    Section section = getElement().findSection(name);
    if(section == null) {
      throw invalidElementNameException(Section.class, name);
    }
    return SectionBuilder.inSection(getQuestionnaire(), section);
  }

  /**
   * Position the builder to the {@link Page} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no page can be found with this name
   */
  public PageBuilder inPage(String name) {
    Page page = getElement().findPage(name);
    if(page == null) {
      throw invalidElementNameException(Page.class, name);
    }
    return PageBuilder.inPage(getQuestionnaire(), page);
  }

  /**
   * Position the builder to the {@link Question} with the given name.
   * @param name
   * @return
   * @throws IllegalStateException if no question can be found with this name
   */
  public QuestionBuilder inQuestion(String name) {
    Question question = getElement().findQuestion(name);
    if(question == null) {
      throw invalidElementNameException(Question.class, name);
    }
    return QuestionBuilder.inQuestion(getQuestionnaire(), question);
  }

  /**
   * Write the questionnaire properties.
   * @param writer
   */
  public void writeProperties(IPropertyKeyProvider propertyKeyProvider, IPropertyKeyWriter writer) {
    QuestionnaireWalker walker = new QuestionnaireWalker(new PropertyKeyWriterVisitor(propertyKeyProvider, writer));
    walker.walk(questionnaire, false);
    writer.end();
  }

  /**
   * Create the localization properties for the current {@link Questionnaire}.
   * @return
   */
  public Properties getProperties(IPropertyKeyProvider propertyKeyProvider) {
    PropertiesPropertyKeyWriterImpl pWriter = new PropertiesPropertyKeyWriterImpl();

    writeProperties(propertyKeyProvider, pWriter);

    return pWriter.getProperties();
  }

}
