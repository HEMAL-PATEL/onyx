/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import java.util.Set;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionType;
import org.obiba.onyx.engine.state.IStageExecution;
import org.obiba.onyx.engine.state.StageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jade Skipped State.
 */
public class JadeSkippedState extends AbstractJadeStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeSkippedState.class);

  @Override
  protected void addUserActions(Set<ActionType> types) {
    // Allows leaving the skipped state
    types.add(ActionType.STOP);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.debug("Jade Stage {} is cancelling", super.getStage().getName());

    // If our dependencies aren't complete, cast INVALID, otherwise cast CANCEL
    // This changes the destination state
    if(areDependenciesCompleted() != null && areDependenciesCompleted()) {
      castEvent(TransitionEvent.CANCEL);
    } else {
      castEvent(TransitionEvent.INVALID);
    }
  }

  @Override
  public void onTransition(IStageExecution execution, StageState fromState, TransitionEvent event) {
    if(event == TransitionEvent.CONTRAINDICATED) {
      super.onTransition(execution, fromState, event);
    }
    // case not applicable transition
    Boolean var = areDependenciesCompleted();
    if(var != null && var == false) {
      castEvent(TransitionEvent.NOTAPPLICABLE);
    }
  }

  @Override
  public boolean isCompleted() {
    return true;
  }

  public String getName() {
    return StageState.Skipped.toString();
  }

  @Override
  public ActionType getStartingActionType() {
    return ActionType.SKIP;
  }

}
