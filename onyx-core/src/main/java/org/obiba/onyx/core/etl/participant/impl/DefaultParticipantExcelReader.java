/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.onyx.core.etl.participant.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;
import org.obiba.onyx.core.domain.participant.Appointment;
import org.obiba.onyx.core.domain.participant.Gender;
import org.obiba.onyx.core.domain.participant.Participant;
import org.obiba.onyx.core.domain.participant.ParticipantAttribute;
import org.obiba.onyx.core.domain.participant.RecruitmentType;
import org.obiba.onyx.core.etl.participant.IParticipantReadListener;
import org.obiba.onyx.core.io.support.ExcelReaderSupport;
import org.obiba.onyx.util.data.Data;
import org.obiba.onyx.util.data.DataBuilder;
import org.obiba.onyx.util.data.DataType;

public class DefaultParticipantExcelReader extends AbstractParticipantReader {
  //
  // Instance Variables
  //

  /**
   * Sheet number.
   */
  private int sheetNumber;

  /**
   * Header row number.
   */
  private int headerRowNumber;

  /**
   * First data row number.
   */
  private int firstDataRowNumber;

  //
  // Constructors
  //

  public DefaultParticipantExcelReader() {
    columnNameToAttributeNameMap = new HashMap<String, String>();
  }

  @SuppressWarnings("unchecked")
  public void process(InputStream input, List<IParticipantReadListener> listeners) throws IOException, IllegalArgumentException {
    HSSFWorkbook wb = new HSSFWorkbook(input);
    HSSFSheet sheet = wb.getSheetAt(sheetNumber - 1);
    HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);

    // Keep track of enrollment ids -- duplicates not allowed!
    Set<String> enrollmentIds = new HashSet<String>();

    initAttributeNameToColumnIndexMap(sheet.getRow(headerRowNumber - 1));

    Iterator<HSSFRow> rowIter = (Iterator<HSSFRow>) sheet.rowIterator();

    // Skip ahead to the first data row.
    HSSFRow row = skipToFirstDataRow(rowIter);

    // Go ahead and process all the data rows.
    int line = 0;

    while(row != null) {
      // Need this check because even though the row iterator only returns "physical" rows, rows containing
      // cells with whitespace only are also returned. We want to ignore those rows.
      if(!rowContainsWhitespaceOnly(evaluator, row)) {
        line = row.getRowNum() + 1;

        Participant participant = null;

        try {
          participant = processParticipant(row, evaluator);
          participant.setAppointment(processAppointment(row, evaluator));

          checkUniqueEnrollmentId(enrollmentIds, participant.getEnrollmentId());
        } catch(IllegalArgumentException ex) {
          throw new IllegalArgumentException("Line " + line + ": " + ex.getMessage());
        }

        // Notify listeners that a participant has been processed.
        for(IParticipantReadListener listener : listeners) {
          listener.onParticipantRead(line, participant);
        }
      }

      if(rowIter.hasNext()) {
        row = rowIter.next();
      } else {
        row = null;
      }
    }

    // Notify listeners that the last participant has been processed.
    for(IParticipantReadListener listener : listeners) {
      listener.onParticipantReadEnd();
    }

  }

  public boolean accept(File dir, String name) {
    return (name.toLowerCase().endsWith(".xls"));
  }

  //
  // Methods
  //

  public void setSheetNumber(int sheetNumber) {
    this.sheetNumber = sheetNumber;
  }

  public void setHeaderRowNumber(int headerRowNumber) {
    this.headerRowNumber = headerRowNumber;
  }

  public void setFirstDataRowNumber(int firstDataRowNumber) {
    this.firstDataRowNumber = firstDataRowNumber;
  }

  public int getFirstDataRowNumber() {
    return firstDataRowNumber;
  }

  private void initAttributeNameToColumnIndexMap(HSSFRow headerRow) {
    if(headerRow == null) {
      throw new IllegalArgumentException("Null headerRow");
    }

    attributeNameToColumnIndexMap = new HashMap<String, Integer>();

    Iterator cellIter = headerRow.cellIterator();

    while(cellIter.hasNext()) {
      HSSFCell cell = (HSSFCell) cellIter.next();

      if(cell != null) {
        if(cell.getCellType() != HSSFCell.CELL_TYPE_STRING) {
          throw new IllegalArgumentException("Header row contains unexpected cell type");
        }

        String columnName = cell.getRichStringCellValue().getString();

        if(columnName != null) {
          String attributeName = (String) columnNameToAttributeNameMap.get(columnName.toUpperCase());

          if(attributeName != null) {
            if(!attributeNameToColumnIndexMap.containsKey(attributeName.toUpperCase())) {
              attributeNameToColumnIndexMap.put(attributeName.toUpperCase(), cell.getColumnIndex());
            } else {
              throw new IllegalArgumentException("Duplicate column for field: " + attributeName);
            }
          }
        }
      }
    }

    checkColumnsForMandatoryAttributesPresent();
  }

  private HSSFRow skipToFirstDataRow(Iterator<HSSFRow> rowIter) {
    HSSFRow row = null;

    while(true) {
      row = rowIter.next();

      if(row.getRowNum() >= (getFirstDataRowNumber() - 1)) {
        break;
      }
    }

    return row;
  }

  private Participant processParticipant(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Participant participant = new Participant();

    setParticipantEssentialAttributes(participant, row, evaluator);
    setParticipantConfiguredAttributes(participant, row, evaluator);

    return participant;
  }

  private Appointment processAppointment(HSSFRow row, HSSFFormulaEvaluator evaluator) {
    Appointment appointment = new Appointment();
    Data data = null;

    data = getEssentialAttributeValue(ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String enrollmentId = data.getValue();
    appointment.setAppointmentCode(enrollmentId);

    data = getEssentialAttributeValue(APPOINTMENT_TIME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(APPOINTMENT_TIME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    Date appointmentTime = data.getValue();
    appointment.setDate(appointmentTime);

    return appointment;
  }

  protected void setParticipantEssentialAttributes(Participant participant, HSSFRow row, HSSFFormulaEvaluator evaluator) {
    participant.setRecruitmentType(RecruitmentType.ENROLLED);

    Data data = null;

    data = getEssentialAttributeValue(ENROLLMENT_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ENROLLMENT_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String enrollmentId = data.getValue();
    participant.setEnrollmentId(enrollmentId);

    data = getEssentialAttributeValue(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(ASSESSMENT_CENTER_ID_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String assessmentCenterId = data.getValue();
    participant.setSiteNo(assessmentCenterId);

    data = getEssentialAttributeValue(FIRST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(FIRST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String firstName = data.getValue();
    participant.setFirstName(firstName);

    data = getEssentialAttributeValue(LAST_NAME_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(LAST_NAME_ATTRIBUTE_NAME.toUpperCase())), evaluator);
    String lastName = data.getValue();
    participant.setLastName(lastName);

    if(attributeNameToColumnIndexMap.containsKey(BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())) {
      data = getEssentialAttributeValue(BIRTH_DATE_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(BIRTH_DATE_ATTRIBUTE_NAME.toUpperCase())), evaluator);
      Date birthDate = (data != null) ? (Date) data.getValue() : null;
      participant.setBirthDate(birthDate);
    }

    if(attributeNameToColumnIndexMap.containsKey(GENDER_ATTRIBUTE_NAME.toUpperCase())) {
      data = getEssentialAttributeValue(GENDER_ATTRIBUTE_NAME, row.getCell(attributeNameToColumnIndexMap.get(GENDER_ATTRIBUTE_NAME.toUpperCase())), evaluator);
      String gender = (data != null) ? data.getValueAsString() : "";
      if(gender.equals("M")) {
        participant.setGender(Gender.MALE);
      } else if(gender.equals("F")) {
        participant.setGender(Gender.FEMALE);
      } else {
        participant.setGender(null);
      }
    }
  }

  protected void setParticipantConfiguredAttributes(Participant participant, HSSFRow row, HSSFFormulaEvaluator evaluator) {
    for(ParticipantAttribute configuredAttribute : participantMetadata.getConfiguredAttributes()) {
      if(configuredAttribute.isAssignableAtEnrollment() && attributeNameToColumnIndexMap.containsKey(configuredAttribute.getName().toUpperCase())) {
        HSSFCell cell = row.getCell(attributeNameToColumnIndexMap.get(configuredAttribute.getName().toUpperCase()));
        setConfiguredAttribute(participant, configuredAttribute, cell, evaluator);
      }
    }
  }

  private void setConfiguredAttribute(Participant participant, ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    Data data = getAttributeValue(attribute, cell, evaluator);
    participant.setConfiguredAttributeValue(attribute.getName(), data);
  }

  private Data getEssentialAttributeValue(String attributeName, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    ParticipantAttribute attribute = participantMetadata.getEssentialAttribute(attributeName);
    Data data = getAttributeValue(attribute, cell, evaluator);

    return data;
  }

  /**
   * Returns the value of the participant attribute stored in the specified data cell.
   * 
   * @param attribute participant attribute
   * @param cell data cell
   * @param evaluator cell evaluator
   * @return attribute value (or <code>null</code> if none)
   * @throws IllegalArgumentException if the cell type is not compatible with the attribute type, or if the attribute is
   * mandatory but its value is <code>null</code>
   */
  private Data getAttributeValue(ParticipantAttribute attribute, HSSFCell cell, HSSFFormulaEvaluator evaluator) {
    if(cell == null) {
      checkMandatoryCondition(attribute, null);
      return null;
    }

    Data data = null;

    try {
      switch(attribute.getType()) {
      case DECIMAL:
        data = DataBuilder.buildDecimal(ExcelReaderSupport.getNumericValue(evaluator, cell));
        break;
      case INTEGER:
        data = DataBuilder.buildInteger(ExcelReaderSupport.getNumericValue(evaluator, cell).longValue());
        break;
      case DATE:
        data = DataBuilder.buildDate(ExcelReaderSupport.getDateValue(evaluator, cell));
        break;
      case TEXT:
        String textValue = ExcelReaderSupport.getTextValue(evaluator, cell);

        if(textValue != null && textValue.trim().length() != 0) {
          data = DataBuilder.buildText(textValue);
        }

        break;
      }
    } catch(IllegalArgumentException ex) {
      if(attribute.isMandatoryAtEnrollment()) {
        throw new IllegalArgumentException("Wrong data type value for field '" + attribute.getName() + "': " + cell.toString());
      } else {
        return null;
      }
    }

    // For TEXT-type attributes, if the attribute has a list of allowed values, validate that the value
    // is within that list.
    if(attribute.getType().equals(DataType.TEXT) && data != null) {
      checkValueAllowed(attribute, data);
    }

    // For non-null attribute values, execute the attribute's validators.
    if(data != null && data.getValue() != null) {
      Validatable validatableData = new Validatable(data);

      for(IValidator validator : attribute.getValidators()) {
        validator.validate(validatableData);
      }

      // In case of any errors, substitute null.
      if(!validatableData.getErrors().isEmpty()) {
        data.setValue(null);
      }
    }

    checkMandatoryCondition(attribute, data);

    return data;
  }

  @SuppressWarnings("unchecked")
  private boolean rowContainsWhitespaceOnly(HSSFFormulaEvaluator evaluator, HSSFRow row) {
    boolean rowContainsWhitespaceOnly = true;

    Iterator cellIter = row.cellIterator();

    while(cellIter.hasNext()) {
      HSSFCell cell = (HSSFCell) cellIter.next();

      if(!ExcelReaderSupport.containsWhitespace(evaluator, cell)) {
        rowContainsWhitespaceOnly = false;
        break;
      }
    }

    return rowContainsWhitespaceOnly;
  }
}