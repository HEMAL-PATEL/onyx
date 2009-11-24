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
import java.util.List;
import java.util.Set;

import org.obiba.magma.VariableValueSource;
import org.obiba.magma.VariableValueSourceFactory;
import org.obiba.magma.beans.BeanVariableValueSourceFactory;
import org.obiba.magma.beans.ValueSetBeanResolver;
import org.obiba.onyx.core.domain.contraindication.Contraindication;
import org.obiba.onyx.engine.Stage;
import org.obiba.onyx.ruby.core.domain.ParticipantTubeRegistration;
import org.obiba.onyx.ruby.core.domain.RegisteredParticipantTube;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Factory for creating VariableValueSources for Ruby variables.
 */
public class TubeVariableValueSourceFactory implements VariableValueSourceFactory {
  //
  // Constants
  //

  public static final String PARTICIPANT_TUBE_REGISTRATION = "ParticipantTubeRegistration";

  public static final String REGISTERED_PARTICIPANT_TUBE = "RegisteredParticipantTube";

  public static final String CONTRAINDICATION = "Contraindication";

  //
  // Instance Variables
  //

  private TubeValueSetBeanResolver beanResolver;

  private String variableRoot;

  private List<Stage> stages;

  //
  // VariableValueSourceFactory Methods
  //

  public Set<VariableValueSource> createSources(String collection) {
    Set<VariableValueSource> sources = new HashSet<VariableValueSource>();

    for(Stage stage : stages) {
      String prefix = (variableRoot != null) ? variableRoot + '.' + stage.getName() : stage.getName();

      sources.addAll(createParticipantTubeRegistrationSources(collection, prefix));
      sources.addAll(createRegisteredParticipantTubeSources(collection, prefix, stage.getName()));
    }

    return sources;
  }

  //
  // Methods
  //

  public void setBeanResolver(TubeValueSetBeanResolver beanResolver) {
    this.beanResolver = beanResolver;
  }

  public void setVariableRoot(String variableRoot) {
    this.variableRoot = variableRoot;
  }

  public String getVariableRoot() {
    return variableRoot;
  }

  public void setStage(List<Stage> stages) {
    this.stages = stages;
  }

  private Set<VariableValueSource> createParticipantTubeRegistrationSources(String collection, String prefix) {
    String tubeRegistrationPrefix = prefix + '.' + PARTICIPANT_TUBE_REGISTRATION;

    // Create sources for participant tube registration variables.
    BeanVariableValueSourceFactory<ParticipantTubeRegistration> delegateFactory = new BeanVariableValueSourceFactory<ParticipantTubeRegistration>("Participant", ParticipantTubeRegistration.class);
    delegateFactory.setPrefix(tubeRegistrationPrefix);
    delegateFactory.setProperties(ImmutableSet.of("startTime", "endTime", "otherContraindication"));
    Set<VariableValueSource> sources = delegateFactory.createSources(collection, beanResolver);

    // Add source for contraindication code variable.
    String ciVariablePrefix = tubeRegistrationPrefix + '.' + CONTRAINDICATION;
    delegateFactory.setPrefix(ciVariablePrefix);
    delegateFactory.setProperties(ImmutableSet.of("contraindicationCode"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("contraindicationCode", "code").build());
    sources.addAll(delegateFactory.createSources(collection, beanResolver));

    // Add source for contraindication type variable.
    sources.add(createContraindicationTypeSource(collection, ciVariablePrefix, beanResolver));

    return sources;
  }

  private Set<VariableValueSource> createRegisteredParticipantTubeSources(String collection, String prefix, String stageName) {
    String tubePrefix = prefix + '.' + REGISTERED_PARTICIPANT_TUBE;

    // Create sources for registered participant tube variables.
    BeanVariableValueSourceFactory<RegisteredParticipantTube> delegateFactory = new BeanVariableValueSourceFactory<RegisteredParticipantTube>("Participant", RegisteredParticipantTube.class);
    delegateFactory.setPrefix(tubePrefix);
    delegateFactory.setOccurrenceGroup(REGISTERED_PARTICIPANT_TUBE);
    delegateFactory.setProperties(ImmutableSet.of("barcode", "registrationTime", "comment"));
    delegateFactory.setPropertyNameToVariableName(new ImmutableMap.Builder<String, String>().put("type", "actionType").build());
    Set<VariableValueSource> sources = delegateFactory.createSources(collection, beanResolver);

    // Add sources for barcode part variables.
    TubeBarcodePartVariableValueSourceFactory barcodePartFactory = new TubeBarcodePartVariableValueSourceFactory();
    barcodePartFactory.setPrefix(tubePrefix);
    sources.addAll(barcodePartFactory.createSources(collection, beanResolver));

    return sources;
  }

  private VariableValueSource createContraindicationTypeSource(String collection, String prefix, ValueSetBeanResolver resolver) {
    BeanVariableValueSourceFactory<Contraindication> delegateFactory = new BeanVariableValueSourceFactory<Contraindication>("Participant", Contraindication.class);
    delegateFactory.setPrefix(prefix);
    delegateFactory.setProperties(ImmutableSet.of("type"));

    return delegateFactory.createSources(collection, resolver).iterator().next();
  }
}
