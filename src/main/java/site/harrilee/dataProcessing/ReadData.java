package site.harrilee.dataProcessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

import static java.lang.Integer.parseInt;

/**
 * This class is used to read the data from the Excel file and return as the form of GraphData.
 *
 * @see GraphData
 */
public class ReadData {
    /**
     * Return format for the network data
     */
    public static class GraphData {
        public int numNodes, numZones, numLinks, firstThruNode;
        public double[][][] tripRtFunc; //tripRtFUnc[i][j] = [Free flow time, B, Capacity, Power]
        public double[][] odPs;
    }

    public GraphData readData(String filename) throws IOException {
        GraphData graphData = new GraphData();

        String path = "./src/main/java/site/harrilee/dataProcessing/data/" + filename;
        FileInputStream fis = new FileInputStream(new File(path));
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        // Read setup data
        if (sheet.getRow(0).getCell(3) == null || (sheet.getRow(0).getCell(3).getCellType()==CellType.BLANK || (sheet.getRow(0).getCell(3).getCellType()==CellType.STRING && sheet.getRow(0).getCell(3).getStringCellValue().equals("")))) {
            graphData.numZones = parseInt(sheet.getRow(0).getCell(0).getStringCellValue().replaceAll("<.*> *", ""));
            graphData.numNodes = parseInt(sheet.getRow(1).getCell(0).getStringCellValue().replaceAll("<.*> *", ""));
            graphData.firstThruNode = parseInt(sheet.getRow(2).getCell(0).getStringCellValue().replaceAll("<.*> *", ""));
            graphData.numLinks = parseInt(sheet.getRow(3).getCell(0).getStringCellValue().replaceAll("<.*> *", ""));
        } else {
            graphData.numZones = (int) sheet.getRow(0).getCell(3).getNumericCellValue();
            graphData.numNodes = (int) sheet.getRow(1).getCell(3).getNumericCellValue();
            graphData.firstThruNode = (int) sheet.getRow(2).getCell(3).getNumericCellValue();
            graphData.numLinks = (int) sheet.getRow(3).getCell(3).getNumericCellValue();
        }


        graphData.tripRtFunc = new double[graphData.numNodes][graphData.numNodes][0];
        graphData.odPs = new double[graphData.numNodes][graphData.numNodes];

        // Setup tripRtFunc
        // Get position of "Start Node"
        int startNodeRow = 0;
        for (int i = 4; i < 10; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            Cell cell = row.getCell(1);
            if (cell == null) continue;
            if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equalsIgnoreCase("start node")) {
                startNodeRow = i;
                break;
            }
            if (i == 9) {
                throw new IOException("Cannot find start node");
            }
        }
        for (int i = startNodeRow + 2; i < graphData.numLinks + startNodeRow + 2; i++) {
            Row row = sheet.getRow(i);
            for (int j = 0; j < 3; j++) {
                int startNode = (int) row.getCell(1).getNumericCellValue() - 1;
                int endNode = (int) row.getCell(2).getNumericCellValue() - 1;
                graphData.tripRtFunc[startNode][endNode] = new double[4];
                graphData.tripRtFunc[startNode][endNode][0] = (double) row.getCell(4).getNumericCellValue();
                graphData.tripRtFunc[startNode][endNode][1] = (double) row.getCell(5).getNumericCellValue();
                graphData.tripRtFunc[startNode][endNode][2] = (double) row.getCell(3).getNumericCellValue();
                graphData.tripRtFunc[startNode][endNode][3] = (double) row.getCell(6).getNumericCellValue();

                if (graphData.tripRtFunc[startNode][endNode][0] == 0 && graphData.tripRtFunc[startNode][endNode][1] == 0 && graphData.tripRtFunc[startNode][endNode][2] == 0 && graphData.tripRtFunc[startNode][endNode][3] == 0) {
                    graphData.tripRtFunc[startNode][endNode] = new double[]{};
                }
            }
        }

        // Setup ODPs
        // Get position of "Start Node"
        int startNodeRow2 = 0;
//        System.out.println("start" +( graphData.numLinks + startNodeRow + 2)+" startNodeRow "+startNodeRow);
        for (int i = graphData.numLinks + startNodeRow + 4; i < graphData.numLinks + startNodeRow + 14; i++) {
            Row row = sheet.getRow(i-1);
            if (row == null) continue;
            Cell cell = row.getCell(1);
            if (cell == null) continue;
            if ((row.getCell(0)==null || Objects.equals(row.getCell(0).getStringCellValue(), "" )|| row.getCell(0).getCellType()==CellType.BLANK)  && cell.getCellType()==CellType.NUMERIC && cell.getNumericCellValue() == 1) {
                startNodeRow2 = i;
                break;
            }
            if (i == graphData.numLinks + startNodeRow + 13) {
                throw new IOException("Cannot find start node for OD pairs");
            }
        }
        for (int i = 0; i < graphData.numZones; i++) {
            Row row = sheet.getRow(i + startNodeRow2);
            for (int j = 0; j < graphData.numZones; j++) {
                Cell cell = row.getCell(j + 1);
                if (cell==null){
                    graphData.odPs[i][j] = 0;
                } else {
                    graphData.odPs[i][j] = (double) row.getCell(j + 1).getNumericCellValue();
                }
            }
        }

        return graphData;
    }
}
