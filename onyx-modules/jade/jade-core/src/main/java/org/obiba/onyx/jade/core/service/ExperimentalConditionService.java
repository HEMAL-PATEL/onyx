/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.jade.core.service;

import java.util.List;

import org.obiba.core.service.PagingClause;
import org.obiba.core.service.SortingClause;
import org.obiba.onyx.core.domain.Attribute;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalCondition;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionLog;
import org.obiba.onyx.jade.core.domain.workstation.ExperimentalConditionValue;
import org.obiba.onyx.jade.core.domain.workstation.InstrumentCalibration;

/**
 * This service provides methods to register and retrieve {@link ExperimentalConditionLog}s and
 * {@link InstrumentCalibration}s as well as access the recorded calibration values themselves.
 */
public interface ExperimentalConditionService {

  /**
   * Persists an {@link ExperimentalCondition} and all its associated {@link ExperimentalConditionValues}.
   * @param experimentalCondition ExperimentalCondition to persist.
   */
  public void save(ExperimentalCondition experimentalCondition);

  /**
   * Returns a list all all the available {@link ExperimentalConditionLog}s.
   * @return List of all ExperimentalConditionLogs.
   */
  public List<ExperimentalConditionLog> getExperimentalConditionLog();

  /**
   * Register an {@link ExperimentalConditionLog} in order to make it available via this service.
   * @param log The ExperimentalConditionLog to be registered.
   */
  public void register(ExperimentalConditionLog log);

  /**
   * Returns a list of {@link ExperimentalCondition}s. If all parameters are null, then all ExperimentalConditions will
   * be returned.
   * @param template Supply a template with a "name" and "workstation" to match on those values.
   * @param paging
   * @param clauses
   * @return
   */
  public List<ExperimentalCondition> getExperimentalConditions(ExperimentalCondition template, PagingClause paging, SortingClause... clauses);

  /**
   * Returns the {@link ExperimentalConditionLog} with the provided name.
   * @param name The name of the ExperimentalConditionLog.
   * @return
   * @throws IllegalStateException If no ExperimentalLogCondition exists with the provided name.
   */
  public ExperimentalConditionLog getExperimentalConditionLogByName(String name);

  /**
   * Returns true in an {@link InstrumentCalibration} exists for the given instrumentType.
   * @param instrumentType
   * @return True if an InstrumentCalibration exists for the instrumentType.
   */
  public boolean instrumentCalibrationExists(String instrumentType);

  /**
   * Returns the {@link InstrumentCalibration} for the provided instrumentType.
   * @param instrumentType
   * @return
   */
  public InstrumentCalibration getInstrumentCalibrationByType(String instrumentType);

  /**
   * Returns a list of {@link InstrumentCalibration}s for the given instrumentType.
   * @param instrumentType
   * @return A list of InstrumentCalibrations.
   */
  public List<InstrumentCalibration> getInstrumentCalibrationsByType(String instrumentType);

  /**
   * Returns an {@link Attribute} for the given experimentalConditionValue.
   * @param experimentalConditionValue
   * @return
   * @throws IllegalStateException If the attribute does not exist.
   */
  public Attribute getAttribute(ExperimentalConditionValue experimentalConditionValue);

}
