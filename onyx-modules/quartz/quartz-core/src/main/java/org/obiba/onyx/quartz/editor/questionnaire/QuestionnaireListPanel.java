/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.questionnaire;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeaderlessColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.CSSPackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.resource.IResourceStream;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundle;
import org.obiba.onyx.quartz.core.engine.questionnaire.bundle.QuestionnaireBundleManager;
import org.obiba.onyx.quartz.core.engine.questionnaire.question.Questionnaire;
import org.obiba.onyx.quartz.core.service.ActiveQuestionnaireAdministrationService;
import org.obiba.onyx.quartz.core.wicket.layout.impl.singledocument.SingleDocumentQuestionnairePage;
import org.obiba.onyx.quartz.core.wicket.model.QuestionnaireModel;
import org.obiba.onyx.quartz.editor.utils.AJAXDownload;
import org.obiba.onyx.quartz.editor.utils.ZipResourceStream;
import org.obiba.onyx.wicket.Images;
import org.obiba.onyx.wicket.panel.OnyxEntityList;
import org.obiba.wicket.markup.html.table.IColumnProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class QuestionnaireListPanel extends Panel {

  private final transient Logger log = LoggerFactory.getLogger(getClass());

  @SpringBean
  private transient QuestionnaireBundleManager questionnaireBundleManager;

  @SpringBean
  private transient QuestionnairePersistenceUtils questionnairePersistenceUtils;

  @SpringBean
  private transient ActiveQuestionnaireAdministrationService activeQuestionnaireAdministrationService;

  private final ModalWindow modalWindow;

  public QuestionnaireListPanel(String id) {
    super(id);

    add(CSSPackageResource.getHeaderContribution(QuestionnaireListPanel.class, "QuestionnaireListPanel.css"));

    modalWindow = new ModalWindow("modalWindow");
    modalWindow.setCssClassName("onyx");
    modalWindow.setInitialWidth(1100);
    modalWindow.setInitialHeight(600);
    modalWindow.setResizable(true);
    modalWindow.setTitle(new ResourceModel("Questionnaire"));
    modalWindow.setCloseButtonCallback(new ModalWindow.CloseButtonCallback() {
      @Override
      public boolean onCloseButtonClicked(AjaxRequestTarget target) {
        return true; // same as cancel
      }
    });

    @SuppressWarnings("rawtypes")
    Form<?> form = new Form("form");
    form.add(modalWindow);
    add(form);

    final OnyxEntityList<Questionnaire> questionnaireList = new OnyxEntityList<Questionnaire>("questionnaires", new QuestionnaireProvider(), new QuestionnaireListColumnProvider(), new StringResourceModel("Questionnaires", QuestionnaireListPanel.this, null));
    add(questionnaireList);

    add(new AjaxLink<Void>("addQuestionnaire") {
      @Override
      public void onClick(AjaxRequestTarget target) {

        Questionnaire newQuestionnaire = new Questionnaire(new StringResourceModel("NewQuestionnaire", QuestionnaireListPanel.this, null).getString(), "1.0");
        newQuestionnaire.setConvertedToMagmaVariables(true);
        Model<Questionnaire> questionnaireModel = new Model<Questionnaire>(newQuestionnaire);
        final EditionPanel editionPanel = new EditionPanel("content", questionnaireModel);
        QuestionnairePanel rightPanel = new QuestionnairePanel(EditionPanel.RIGHT_PANEL, questionnaireModel, true) {
          @Override
          public void onSave(AjaxRequestTarget target1, Questionnaire questionnaire) {
            persist(target1);
            editionPanel.restoreDefaultRightPanel(target1);
            target1.addComponent(editionPanel.getTree());
            target1.addComponent(questionnaireList);
          }
        };
        editionPanel.setRightPanel(rightPanel, new Model<String>(""));

        modalWindow.setContent(editionPanel);
        modalWindow.show(target);
      }
    }.add(new Image("img", Images.ADD)));
  }

  protected class QuestionnaireProvider extends SortableDataProvider<Questionnaire> {

    @Override
    public Iterator<Questionnaire> iterator(int first, int count) {
      Set<QuestionnaireBundle> bundles = questionnaireBundleManager.bundles();
      List<Questionnaire> questionnaires = new ArrayList<Questionnaire>(bundles.size());
      for(QuestionnaireBundle bundle : bundles) {
        questionnaires.add(bundle.getQuestionnaire());
      }
      return questionnaires.iterator();
    }

    @Override
    public int size() {
      return questionnaireBundleManager.bundles().size();
    }

    @Override
    public IModel<Questionnaire> model(Questionnaire questionnaire) {
      return new Model<Questionnaire>(questionnaire);
    }

  }

  private class QuestionnaireListColumnProvider implements IColumnProvider<Questionnaire>, Serializable {

    private final List<IColumn<Questionnaire>> columns = new ArrayList<IColumn<Questionnaire>>();

    public QuestionnaireListColumnProvider() {
      columns.add(new AbstractColumn<Questionnaire>(new StringResourceModel("Name", QuestionnaireListPanel.this, null), "name") {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          final Questionnaire questionnaire = rowModel.getObject();
          final String name = questionnaire.getName();
          cellItem.add(new AjaxLazyLoadPanel(componentId) {
            @Override
            public Component getLazyLoadComponent(String componentId1) {
              String str = name;
              try {
                if(!questionnaire.isConvertedToMagmaVariables()) {
                  QuestionnaireDataSourceConverter.convertToVariableDataSources(questionnaire);
                  questionnaire.setConvertedToMagmaVariables(true);
                  questionnairePersistenceUtils.persist(questionnaire);
                }
              } catch(Exception e) {
                log.error("Cannot convert questionnaire", e);
                String errorMsg = new StringResourceModel("converting.error", QuestionnaireListPanel.this, null, new Object[] { e.getMessage() }).getString();
                str += " <img title=\"" + errorMsg + "\" alt=\"" + errorMsg + "\" src=\"" + RequestCycle.get().urlFor(Images.ERROR) + "\"/>";
              }
              return new Label(componentId1, str).setEscapeModelStrings(false);
            }

            @Override
            public Component getLoadingComponent(String markupId) {
              String message = new StringResourceModel("converting", QuestionnaireListPanel.this, null).getString();
              String conversion = "<span class=\"converting\"><img alt=\"" + message + "\" src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>" + message + "</span>";
              return new Label(markupId, name + conversion).setEscapeModelStrings(false);
            }

          });
        }
      });

      columns.add(new PropertyColumn<Questionnaire>(new StringResourceModel("Version", QuestionnaireListPanel.this, null), "version", "version"));
      columns.add(new AbstractColumn<Questionnaire>(new StringResourceModel("Language(s)", QuestionnaireListPanel.this, null)) {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          StringBuilder localeList = new StringBuilder();
          Locale sessionLocale = Session.get().getLocale();
          for(Locale locale : rowModel.getObject().getLocales()) {
            if(localeList.length() != 0) localeList.append(", ");
            localeList.append(locale.getDisplayLanguage(sessionLocale));
          }
          cellItem.add(new Label(componentId, localeList.toString()));
        }
      });

      columns.add(new HeaderlessColumn<Questionnaire>() {
        @Override
        public void populateItem(Item<ICellPopulator<Questionnaire>> cellItem, String componentId, IModel<Questionnaire> rowModel) {
          cellItem.add(new LinkFragment(componentId, rowModel));
        }
      });

    }

    @Override
    public List<IColumn<Questionnaire>> getAdditionalColumns() {
      return null;
    }

    @Override
    public List<String> getColumnHeaderNames() {
      return null;
    }

    @Override
    public List<IColumn<Questionnaire>> getDefaultColumns() {
      return columns;
    }

    @Override
    public List<IColumn<Questionnaire>> getRequiredColumns() {
      return columns;
    }

  }

  public class LinkFragment extends Fragment {

    @SuppressWarnings("rawtypes")
    public LinkFragment(String id, final IModel<Questionnaire> rowModel) {
      super(id, "linkFragment", QuestionnaireListPanel.this, rowModel);
      final Questionnaire questionnaire = rowModel.getObject();

      add(new AjaxLink<Questionnaire>("editLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setTitle(questionnaire.getName());
          modalWindow.setContent(new EditionPanel("content", rowModel));
          modalWindow.show(target);
        }
      });

      final AJAXDownload download = new AJAXDownload() {
        @Override
        protected IResourceStream getResourceStream() {
          try {
            return new ZipResourceStream(questionnaireBundleManager.generateBundleZip(questionnaire.getName()));
          } catch(IOException e) {
            log.error("Cannot generate questionnaire zip", e);
            return null;
          }
        }

        @Override
        protected String getFileName() {
          return questionnaire.getName() + ".zip";
        }
      };
      add(download);

      add(new AjaxLink("downloadLink") {
        @Override
        public void onClick(AjaxRequestTarget target) {
          download.initiate(target);
        }
      });

      add(new AjaxLink<Questionnaire>("previewLink", rowModel) {
        @Override
        public void onClick(AjaxRequestTarget target) {
          modalWindow.setContent(new Panel("contents"));
          modalWindow.show(target);
        }
      });
      add(new Link<Questionnaire>("exportLink", rowModel) {
        @Override
        public void onClick() {
          activeQuestionnaireAdministrationService.setQuestionnaire(questionnaire);
          activeQuestionnaireAdministrationService.setDefaultLanguage(questionnaire.getLocales().get(0));
          activeQuestionnaireAdministrationService.setQuestionnaireDevelopmentMode(true);
          setResponsePage(new SingleDocumentQuestionnairePage(new QuestionnaireModel<Questionnaire>(questionnaire)));
        }
      });
    }
  }
}
