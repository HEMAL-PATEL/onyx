/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.ruby.magma;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.obiba.magma.Variable;
import org.obiba.magma.VariableValueSource;
import org.obiba.magma.beans.BeanPropertyVariableValueSource;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.magma.type.TextType;
import org.obiba.onyx.ruby.core.domain.BarcodePart;
import org.obiba.onyx.ruby.core.domain.BarcodeStructure;
import org.obiba.onyx.ruby.core.domain.TubeRegistrationConfiguration;
import org.obiba.onyx.ruby.core.domain.parser.IBarcodePartParser;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for creating VariableValueSources for tube barcode part variables.
 */
public class TubeBarcodePartVariableValueSourceFactory extends BeanVariableValueSourceFactory<BarcodePart> {
  //
  // Instance Variables
  //

  @Autowired(required = true)
  private Map<String, TubeRegistrationConfiguration> tubeRegistrationConfigurationMap;

  //
  // Constructors
  //

  public TubeBarcodePartVariableValueSourceFactory() {
    super("Participant", BarcodePart.class);
  }

  //
  // BeanVariableValueSourceFactory Methods
  //

  @Override
  public Set<VariableValueSource> createSources(String collection, ValueSetBeanResolver resolver) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(String stageName : tubeRegistrationConfigurationMap.keySet()) {
      TubeRegistrationConfiguration tubeRegistrationConfiguration = tubeRegistrationConfigurationMap.get(stageName);
      BarcodeStructure barcodeStructure = tubeRegistrationConfiguration.getBarcodeStructure();

      for(IBarcodePartParser partParser : barcodeStructure.getParsers()) {
        String barcodePartVariableName = partParser.getVariableName();
        if(barcodePartVariableName != null) {
          String variableName = lookupVariableName(barcodePartVariableName);
          Variable variable = this.doBuildVariable(collection, TextType.get().getJavaClass(), variableName);
          sources.add(new BeanPropertyVariableValueSource(variable, BarcodePart.class, resolver, "partValue"));
        }
      }
    }

    return sources;
  }
}
