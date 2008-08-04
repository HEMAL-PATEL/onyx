/**
 * 
 */
package org.obiba.onyx.jade.engine.state;

import org.obiba.onyx.engine.Action;
import org.obiba.onyx.engine.ActionDefinition;
import org.obiba.onyx.engine.state.AbstractStageState;
import org.obiba.onyx.engine.state.TransitionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JadeCompletedState extends AbstractStageState {

  private static final Logger log = LoggerFactory.getLogger(JadeCompletedState.class);

  public JadeCompletedState() {
    addAction(ActionDefinition.CANCEL_ACTION);
    addAction(ActionDefinition.COMMENT_ACTION);
  }

  @Override
  public void stop(Action action) {
    super.execute(action);
    log.info("Jade Stage {} is cancelling", super.getStage().getName());
    castEvent(TransitionEvent.CANCEL);
  }

  @Override
  public String getMessage() {
    return "Completed";
  }
  
  @Override
  public boolean isCompleted() {
    return true;
  }

}