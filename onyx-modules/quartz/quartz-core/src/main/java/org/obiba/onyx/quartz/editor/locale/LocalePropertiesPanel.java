/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.quartz.editor.locale;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.Loop;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Locale panel witch contains textboxes of locale labels
 */
public class LocalePropertiesPanel extends Panel {

  private static final long serialVersionUID = 1L;

  protected final Logger log = LoggerFactory.getLogger(getClass());

  public LocalePropertiesPanel(String id, LocaleProperties localeProperties, List<LocaleProperties> listLocaleProperties) {
    super(id);
    listLocaleProperties.add(localeProperties);
    add(new LocalePropertiesForm("labelsForm", localeProperties));
  }

  public class LocalePropertiesForm extends Form<LocaleProperties> {

    private static final long serialVersionUID = 1L;

    public LocalePropertiesForm(String id, final LocaleProperties localeProperties) {
      super(id, new Model<LocaleProperties>(localeProperties));

      Loop labels = new Loop("labelsItem", localeProperties.getListKeys().length) {

        private static final long serialVersionUID = 1L;

        @Override
        protected void populateItem(LoopItem item) {
          TextField<String> labelInput = new TextField<String>("labelsInput", new PropertyModel<String>(getModelObject(), "listValues[" + item.getIteration() + "]"));
          Label labelLabel = new Label("labelsLabel", getModelObject().getListKeys()[item.getIteration()]);
          labelInput.add(new StringValidator.MaximumLengthValidator(20));
          labelInput.add(new AjaxFormComponentUpdatingBehavior("onblur") {

            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
              // null, because submitting compoment is a javascript event : "onblur"
              delegateSubmit(null);
            }

          });
          item.add(labelLabel);
          item.add(labelInput);
        }
      };
      add(labels);
    }
  }
}
