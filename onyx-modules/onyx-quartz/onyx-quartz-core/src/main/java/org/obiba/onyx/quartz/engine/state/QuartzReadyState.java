/***********************************************************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
/**
 * State ready for Quartz module
 * 
 * @author acarey
 * 
 * Coming from states: waiting, skipped, completed Possible forward states/actions/transitions: invalid, start,
 * notApplicable
 */
package org.obiba.onyx.quartz.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzReadyState extends AbstractQuartzStageState {

  private static final Logger log = LoggerFactory.getLogger(QuartzReadyState.class);

  public String getName() {
    return StageState.Ready.toString();
  }

  @Override
  protected void addUserActions(Set<ActionType> types) {
    types.add(ActionType.EXECUTE);
    types.add(ActionType.SKIP);
  }

  @Override
  public void execute(Action action) {
    super.execute(action);
    log.debug("Quartz Stage {} is starting", super.getStage().getName());
    castEvent(TransitionEvent.START);
  }

  @Override
  public void skip(Action action) {
    super.skip(action);
    log.debug("Quartz Stage {} is skipping", super.getStage().getName());
    castEvent(TransitionEvent.SKIP);
  }

  @Override
  protected boolean wantTransitionEvent(TransitionEvent transitionEvent) {
    if(transitionEvent.equals(TransitionEvent.VALID)) return false;
    else
      return true;
  }
}
