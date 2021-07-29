package com.bmo.pbt.bmo.pay.the.bil.endpoint.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.bmo.pbt.bmo.pay.the.bil.endpoint.models.RestEndpoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;

@Component("ExcelDataLoadService")
public class ExcelDataLoadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExcelDataLoadService.class);

  @Autowired private ObjectMapper objectMapper;

  public List<RestEndpoint> readRestEndpointsFromExcelFile()
      throws IOException {
      
    List<RestEndpoint> listRestEndpoints = new ArrayList<>();
    String excelFilePath = "endpointList.xlsx";
    
    ClassPathResource excelDataFile = new ClassPathResource(excelFilePath);
    //FileInputStream inputStream = new FileInputStream(new File(excelFilePath));

    InputStream inputStream = excelDataFile.getInputStream();
    
    Workbook workbook = new XSSFWorkbook(inputStream);
    Sheet firstSheet = workbook.getSheetAt(0);
    Iterator<Row> iterator = firstSheet.iterator();

    final MapType headerMapType =
        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class);

    while (iterator.hasNext()) {
      Row nextRow = iterator.next();
      Iterator<Cell> cellIterator = nextRow.cellIterator();
      RestEndpoint aRestEndpoint = new RestEndpoint();

      while (cellIterator.hasNext()) {
        Cell nextCell = cellIterator.next();
        int columnIndex = nextCell.getColumnIndex();

        switch (columnIndex) {
          case 1:
            aRestEndpoint.setUrl((String) getCellValue(nextCell));
            break;
          case 2:
            aRestEndpoint.setMethod((String) getCellValue(nextCell));
            break;
          case 3:
            Map<String, String> httpHeaderMapResult =
                objectMapper.readValue((String) getCellValue(nextCell), headerMapType);

            aRestEndpoint.setHttpHeaders(httpHeaderMapResult);
            break;

          case 4:
            aRestEndpoint.setJsonPayload((String) getCellValue(nextCell));
            break;
        }
      }
      listRestEndpoints.add(aRestEndpoint);
    }

    workbook.close();
    inputStream.close();

    LOGGER.debug(listRestEndpoints.toString());;
    return listRestEndpoints;
  }

  private Object getCellValue(Cell cell) {
    switch (cell.getCellType()) {
      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();

      case Cell.CELL_TYPE_BOOLEAN:
        return cell.getBooleanCellValue();

      case Cell.CELL_TYPE_NUMERIC:
        return cell.getNumericCellValue();
    }

    return null;
  }
  
  
}
