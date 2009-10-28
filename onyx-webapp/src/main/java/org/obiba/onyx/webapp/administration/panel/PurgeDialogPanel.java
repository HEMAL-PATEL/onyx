/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.webapp.administration.panel;

import java.io.Serializable;
import java.util.Date;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.obiba.onyx.core.service.ParticipantService;
import org.obiba.onyx.core.service.PurgeParticipantDataService;

public class PurgeDialogPanel extends Panel {

  private static final long serialVersionUID = 1L;

  private ConfirmationFragment confirmationFragment;

  private ProgressFragment progressFragment;

  private ResultFragment resultFragment;

  @SpringBean
  private PurgeParticipantDataService purgeParticipantDataService;

  @SpringBean
  private ParticipantService participantService;

  //
  // Constructors
  //

  public PurgeDialogPanel(String id) {
    super(id);

    confirmationFragment = new ConfirmationFragment("contentFragment");
    progressFragment = new ProgressFragment("contentFragment", new ResourceModel("PurgeInProgress"));
    resultFragment = new ResultFragment("contentFragment");
  }

  //
  // Methods
  //

  public void showConfirmation() {
    replaceOrAddFragment(confirmationFragment);
  }

  public void showProgress() {
    replaceOrAddFragment(progressFragment);
  }

  public void showResult(boolean purgeSucceeded, int participantsDeleted) {
    IModel messageModel;
    if(purgeSucceeded) {
      resultFragment.successImage.setVisible(true);
      messageModel = (participantsDeleted > 0) ? new StringResourceModel("SuccessPurgeResult", PurgeDialogPanel.this, null, new Object[] { participantsDeleted }) : new StringResourceModel("SuccessPurgeResultNoParticipant", PurgeDialogPanel.this, null);
    } else {
      resultFragment.failedImage.setVisible(true);
      messageModel = new StringResourceModel("FailedPurgeResult", PurgeDialogPanel.this, null);
    }

    resultFragment.resultLabel.setDefaultModel(messageModel);

    replaceOrAddFragment(resultFragment);
  }

  private void replaceOrAddFragment(Fragment fragment) {
    Fragment currentFragment = (Fragment) get("contentFragment");

    if(currentFragment != null) {
      replace(fragment);
    } else {
      add(fragment);
    }
  }

  //
  // Inner Classes
  //

  class ConfirmationFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private class PurgeInformation implements Serializable {

      private static final long serialVersionUID = 1L;

      public String getPurgeDataOlderThanInDays() {
        return purgeParticipantDataService.getPurgeDataOlderThanInDays();
      }

      public int getExportedDeleteCount() {
        Date maxDeletionDate = purgeParticipantDataService.getMaxDateForDeletion();
        return participantService.getExportedParticipants(maxDeletionDate).size();
      }

      public int getNonExportableDeleteCount() {
        Date maxDeletionDate = purgeParticipantDataService.getMaxDateForDeletion();
        return participantService.getNonExportableParticipants(maxDeletionDate).size();
      }

    }

    public ConfirmationFragment(String id) {
      super(id, "confirmationFragment", PurgeDialogPanel.this);

      PurgeInformation purgeInfo = new PurgeInformation();
      IModel<PurgeInformation> purgeInfoModel = new Model<PurgeInformation>(purgeInfo);
      add(new MultiLineLabel("confirmMessage", new StringResourceModel("ConfirmPurge", this, purgeInfoModel, new Object[] { new PropertyModel<Object>(purgeInfoModel, "purgeDataOlderThanInDays"), new PropertyModel<Object>(purgeInfoModel, "exportedDeleteCount"), new PropertyModel<Object>(purgeInfoModel, "nonExportableDeleteCount") })));

    }
  }

  class ProgressFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label progressLabel;

    private Image progressImage;

    public ProgressFragment(String id, IModel messageModel) {
      super(id, "progressFragment", PurgeDialogPanel.this);

      progressLabel = new Label("progressLabel", messageModel);
      add(progressLabel);

      progressImage = new Image("progressImage");
      add(progressImage);
    }
  }

  class ResultFragment extends Fragment {

    private static final long serialVersionUID = 1L;

    private Label resultLabel;

    private Image successImage;

    private Image failedImage;

    public ResultFragment(String id) {
      super(id, "resultFragment", PurgeDialogPanel.this);

      resultLabel = new Label("resultLabel");
      add(resultLabel);

      successImage = new Image("successImage");
      successImage.setVisible(false);
      add(successImage);

      failedImage = new Image("failedImage");
      failedImage.setVisible(false);
      add(failedImage);
    }
  }
}