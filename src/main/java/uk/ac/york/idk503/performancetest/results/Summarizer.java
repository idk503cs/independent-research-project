package uk.ac.york.idk503.performancetest.results;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xddf.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.RevisionSyntaxException;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Summarizer {
    private static final Logger LOG = LoggerFactory.getLogger(Summarizer.class);

    public static void main(String[] args) {
        var repoPath = args.length > 0 ? args[0] : "C:\\Dev\\git\\independent-research-project";
        var destinationDir = args.length > 1 ? args[1] : "C:\\temp\\data";
        var isFileCopied = copyDataToDestination(destinationDir, repoPath);

        if(isFileCopied){
            var tasksOnServer = getTasksOnServer(destinationDir);
            // Add results into Map
            TreeMap<String, TreeMap<Integer, Results>> resultsByTaskAndRow = new TreeMap<>();
            TreeMap<String, Results> resultsByTask = new TreeMap<>();

            for(String taskOnServer : tasksOnServer){
                TreeMap<Integer, Results> summarizedResultsByTaskAndRow = getSummarisedResultsByTaskAndRow(taskOnServer, destinationDir);
                resultsByTaskAndRow.put(taskOnServer, summarizedResultsByTaskAndRow);
                Results summarizedResultsByTask = getSummarisedResultsByTask(taskOnServer, destinationDir);
                resultsByTask.put(taskOnServer, summarizedResultsByTask);
            }
            writeToExcel("DataLoad", resultsByTaskAndRow, resultsByTask);
            writeToExcel("MergeSort", resultsByTaskAndRow, resultsByTask);
            writeToExcel("Multistage", resultsByTaskAndRow, resultsByTask);
            writeToExcel("Service", resultsByTaskAndRow, resultsByTask);
        }
    }

    private static TreeSet<String> getTasksOnServer(String destinationDir) {
        try (var dataFiles = Files.walk(Path.of(destinationDir))){
            return new TreeSet<>(dataFiles.filter(fileName -> fileName.toString().endsWith(".csv"))
                    .map(file -> {
                        String fileName = file.toString().substring(destinationDir.length());
                        return fileName.substring(1, fileName.length() - 20);
                    })
                    .collect(Collectors.toSet()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static TreeMap<Integer, Results> getSummarisedResultsByTaskAndRow(String taskOnServer, String destinationDir) {
        TreeMap<Integer, Results> summarisedResultsForTaskOnServerByRow = new TreeMap<>();

        try (var files = Files.walk(Path.of(destinationDir))){
            var dataFiles = files.filter(fileName -> fileName.toString().contains(taskOnServer))
                    .collect(Collectors.toSet());
            for(var dataFile : dataFiles){
                int id = 0;
                for(String line : Files.readAllLines(dataFile)){
                    id++;
                    String[] record = line.split(",");
                    summarisedResultsForTaskOnServerByRow.putIfAbsent(id, new Results());
                    summarisedResultsForTaskOnServerByRow.get(id).getTestProgram().add(record[0]);
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapNonNmethodsUsed().addValue(Double.valueOf(record[2]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapNonNmethodsCommitted().addValue(Double.valueOf(record[3]));
                    summarisedResultsForTaskOnServerByRow.get(id).getMetaspaceUsed().addValue(Double.valueOf(record[5]));
                    summarisedResultsForTaskOnServerByRow.get(id).getMetaspaceCommitted().addValue(Double.valueOf(record[6]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapProfiledNmethodsUsed().addValue(Double.valueOf(record[8]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapProfiledNmethodsCommitted().addValue(Double.valueOf(record[9]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCompressedClassSpaceUsed().addValue(Double.valueOf(record[11]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCompressedClassSpaceCommitted().addValue(Double.valueOf(record[12]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1EdenSpaceUsed().addValue(Double.valueOf(record[14]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1EdenSpaceCommitted().addValue(Double.valueOf(record[15]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1OldGenUsed().addValue(Double.valueOf(record[17]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1OldGenCommitted().addValue(Double.valueOf(record[18]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1SurvivorSpaceUsed().addValue(Double.valueOf(record[20]));
                    summarisedResultsForTaskOnServerByRow.get(id).getG1SurvivorSpaceCommitted().addValue(Double.valueOf(record[21]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapNonProfiledNmethodsUsed().addValue(Double.valueOf(record[23]));
                    summarisedResultsForTaskOnServerByRow.get(id).getCodeHeapNonProfiledNmethodsCommitted().addValue(Double.valueOf(record[24]));
                    summarisedResultsForTaskOnServerByRow.get(id).getHeapUsed().addValue(Double.valueOf(record[26]));
                    summarisedResultsForTaskOnServerByRow.get(id).getHeapCommitted().addValue(Double.valueOf(record[27]));
                    summarisedResultsForTaskOnServerByRow.get(id).getNonHeapUsed().addValue(Double.valueOf(record[28]));
                    summarisedResultsForTaskOnServerByRow.get(id).getNonHeapCommitted().addValue(Double.valueOf(record[29]));
                    summarisedResultsForTaskOnServerByRow.get(id).getSystemLoadAverage().addValue(Double.valueOf(record[30]));
                    summarisedResultsForTaskOnServerByRow.get(id).getAvailableProcessors().addValue(Double.valueOf(record[31]));
                    summarisedResultsForTaskOnServerByRow.get(id).getName().add(record[32]);
                    summarisedResultsForTaskOnServerByRow.get(id).getArch().add(record[33]);
                    summarisedResultsForTaskOnServerByRow.get(id).getVersion().add(record[34]);
                    summarisedResultsForTaskOnServerByRow.get(id).getMemoryRecordCount().addValue(Double.valueOf(record[35]));
                    summarisedResultsForTaskOnServerByRow.get(id).getThreadsRecordCount().addValue(Double.valueOf(record[36]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return summarisedResultsForTaskOnServerByRow;
    }

    private static Results getSummarisedResultsByTask(String taskOnServer, String destinationDir) {
        Results summaryResultForTask = new Results();

        try (var files = Files.walk(Path.of(destinationDir))){
            var dataFiles = files.filter(fileName -> fileName.toString().contains(taskOnServer))
                    .collect(Collectors.toSet());

            for(var dataFile : dataFiles){
                for(String line : Files.readAllLines(dataFile)){
                    String[] record = line.split(",");
                    summaryResultForTask.getTestProgram().add(record[0]);
                    summaryResultForTask.getCodeHeapNonNmethodsUsed().addValue(Double.valueOf(record[2]));
                    summaryResultForTask.getCodeHeapNonNmethodsCommitted().addValue(Double.valueOf(record[3]));
                    summaryResultForTask.getMetaspaceUsed().addValue(Double.valueOf(record[5]));
                    summaryResultForTask.getMetaspaceCommitted().addValue(Double.valueOf(record[6]));
                    summaryResultForTask.getCodeHeapProfiledNmethodsUsed().addValue(Double.valueOf(record[8]));
                    summaryResultForTask.getCodeHeapProfiledNmethodsCommitted().addValue(Double.valueOf(record[9]));
                    summaryResultForTask.getCompressedClassSpaceUsed().addValue(Double.valueOf(record[11]));
                    summaryResultForTask.getCompressedClassSpaceCommitted().addValue(Double.valueOf(record[12]));
                    summaryResultForTask.getG1EdenSpaceUsed().addValue(Double.valueOf(record[14]));
                    summaryResultForTask.getG1EdenSpaceCommitted().addValue(Double.valueOf(record[15]));
                    summaryResultForTask.getG1OldGenUsed().addValue(Double.valueOf(record[17]));
                    summaryResultForTask.getG1OldGenCommitted().addValue(Double.valueOf(record[18]));
                    summaryResultForTask.getG1SurvivorSpaceUsed().addValue(Double.valueOf(record[20]));
                    summaryResultForTask.getG1SurvivorSpaceCommitted().addValue(Double.valueOf(record[21]));
                    summaryResultForTask.getCodeHeapNonProfiledNmethodsUsed().addValue(Double.valueOf(record[23]));
                    summaryResultForTask.getCodeHeapNonProfiledNmethodsCommitted().addValue(Double.valueOf(record[24]));
                    summaryResultForTask.getHeapUsed().addValue(Double.valueOf(record[26]));
                    summaryResultForTask.getHeapCommitted().addValue(Double.valueOf(record[27]));
                    summaryResultForTask.getNonHeapUsed().addValue(Double.valueOf(record[28]));
                    summaryResultForTask.getNonHeapCommitted().addValue(Double.valueOf(record[29]));
                    summaryResultForTask.getSystemLoadAverage().addValue(Double.valueOf(record[30]));
                    summaryResultForTask.getAvailableProcessors().addValue(Double.valueOf(record[31]));
                    summaryResultForTask.getName().add(record[32]);
                    summaryResultForTask.getArch().add(record[33]);
                    summaryResultForTask.getVersion().add(record[34]);
                    summaryResultForTask.getMemoryRecordCount().addValue(Double.valueOf(record[35]));
                    summaryResultForTask.getThreadsRecordCount().addValue(Double.valueOf(record[36]));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return summaryResultForTask;
    }

    private static void writeToExcel(final String taskOnServer, final TreeMap<String, TreeMap<Integer, Results>> summarisedResultsForTaskByRow, final TreeMap<String, Results> summarisedResultsForTask) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             FileOutputStream fileOut = new FileOutputStream(taskOnServer + ".xlsx")) {
            List<XSSFSheet> collectionOfSheets = new ArrayList<>();

            // Add datasheets containing mean data points by second
            createSheetsByTaskAndServer(taskOnServer, summarisedResultsForTaskByRow, summarisedResultsForTask, wb, collectionOfSheets);

            // Answer questions
            answerQuestionsInSummarySheet1(taskOnServer, summarisedResultsForTaskByRow, summarisedResultsForTask, wb);
            answerQuestionsInSummarySheet2(taskOnServer, summarisedResultsForTaskByRow, summarisedResultsForTask, wb);

            wb.write(fileOut);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addBarChart(XSSFSheet sheet, int row, int dataBlock, int dataColumn, String chartTitleText, String axisTitle) {
        final int CHART_HEIGHT = 10;
        final int CHART_WIDTH = 9;
        final int RELATIVE_FIRST_DATA_ROW = 2;
        final int RELATIVE_LAST_DATA_ROW = 6;
        final int START_COLUMN = (dataBlock * 10) + 9;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, START_COLUMN, row, START_COLUMN+CHART_WIDTH, row+CHART_HEIGHT);
        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(chartTitleText);
        chart.setTitleOverlay(false);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Concurrency Utility");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(axisTitle);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
        XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, 0, 0));
        XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn, dataColumn));
        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFChartData.Series series = data.addSeries(xs, ys1);
        series.setTitle(axisTitle, (CellReference)null);
        chart.plot(data);
        XDDFBarChartData bar = (XDDFBarChartData)data;
        bar.setBarDirection(BarDirection.BAR);
        solidFillSeries(data, 0, PresetColor.GRAY);
    }

    private static void answerQuestionsInSummarySheet1(String taskOnServer, TreeMap<String, TreeMap<Integer, Results>> allSummarisedResults,
                                                       final TreeMap<String, Results> summarisedResultsForTask, XSSFWorkbook wb) {
        XSSFSheet sheet = wb.createSheet("Summary1");
        wb.setSheetOrder(sheet.getSheetName(), 0);
        wb.setSelectedTab(0);

        Row row;
        int rowNumber = 0;

        TreeSet<String> architectures = getArchitectures(allSummarisedResults);
        TreeSet<String> machineSizes = getMachineSize(allSummarisedResults);

        for(String architecture : architectures) {
            for(String machineSize : machineSizes) {
                row = sheet.createRow(rowNumber++);
                (row.createCell(0)).setCellValue(String.format("%s - %s", architecture, machineSize));

                int columnNumber = 0;
                row = sheet.createRow(rowNumber++);
                (row.createCell(columnNumber++)).setCellValue("Concurrency Utility");
                (row.createCell(columnNumber++)).setCellValue("Runtime in Seconds");
                (row.createCell(columnNumber++)).setCellValue("Total Records Count");
                (row.createCell(columnNumber++)).setCellValue("Heap used (MB)");
                (row.createCell(columnNumber++)).setCellValue("Heap used per record (KB)");
                (row.createCell(columnNumber++)).setCellValue("Heap used per second (MB)");

                for (Map.Entry<String, TreeMap<Integer, Results>> taskResults : allSummarisedResults.entrySet()) {
                    wb.setSheetOrder(sheet.getSheetName(), 0);
                    columnNumber = 0;
                    if (taskResults.getKey().endsWith(taskOnServer) && taskResults.getKey().contains(architecture) && taskResults.getKey().contains("." + machineSize)) {
                        Map<Integer, Results> summarisedResultsForTaskByServer = taskResults.getValue();
                        var maxCountIdx = summarisedResultsForTaskByServer.keySet().stream().filter(i -> summarisedResultsForTaskByServer.get(i).getTestProgram().size() > 0).mapToInt(i -> i).max().getAsInt();

                        row = sheet.createRow(rowNumber++);
                        // Which concurrency utility was quickest by server?
                        (row.createCell(columnNumber++)).setCellValue(summarisedResultsForTaskByServer.values().stream().toList().getFirst().getTestProgram().getFirst());
                        (row.createCell(columnNumber++)).setCellValue(maxCountIdx);

                        // Which concurrency utility processed most records by server?
                        int totalRecords = (int)(summarisedResultsForTaskByServer.get(maxCountIdx).getThreadsRecordCount().getMean() + summarisedResultsForTaskByServer.get(maxCountIdx).getMemoryRecordCount().getMean());
                        (row.createCell(columnNumber++)).setCellValue(totalRecords);

                        // Which concurrency utility used the most memory by server?
                        double usedHeapMemoryInMB = summarisedResultsForTaskByServer.get(maxCountIdx).getHeapUsed().getMean() / (1024 * 1024);
                        double usedHeapMemoryInKB = summarisedResultsForTaskByServer.get(maxCountIdx).getHeapUsed().getMean() / 1024;
                        (row.createCell(columnNumber++)).setCellValue((int)(usedHeapMemoryInMB));
                        (row.createCell(columnNumber++)).setCellValue(totalRecords==0 ? 0 : (int)(usedHeapMemoryInKB/totalRecords));
                        (row.createCell(columnNumber++)).setCellValue((int)(usedHeapMemoryInMB/maxCountIdx));

                    }
                }

                int blockNumber = 0;
                int dataColumnIdx = 1;
                addBarChart(sheet, rowNumber-7, blockNumber++, dataColumnIdx++, "Execution time", "Time in seconds");
                addBarChart(sheet, rowNumber-7, blockNumber++, dataColumnIdx++, "Database inserts in a 60 seconds", "Total record count");
                addBarChart(sheet, rowNumber-7, blockNumber++, dataColumnIdx++, "Heap used (MB)", "Heap (MB)");
                rowNumber+=10;
            }

        }
    }

    private static Map<String, List<SummaryData>>
    getResultsSummaryMap(String taskOnServer, TreeMap<String, TreeMap<Integer, Results>> allSummarisedResults,
                         final TreeMap<String, Results> summarisedResultsForTask) {
        Map<String, List<SummaryData>> mapSummaryData = new TreeMap<>();

        TreeSet<String> architectures = getArchitectures(allSummarisedResults);
        TreeSet<String> machineSizes = getMachineSize(allSummarisedResults);
        for(String architecture : architectures) {
            for(String machineSize : machineSizes) {
                List<SummaryData> summaryDataList = new ArrayList<>();
                String archMachine = String.format("%s.%s", architecture, machineSize);
                for (Map.Entry<String, TreeMap<Integer, Results>> taskResults : allSummarisedResults.entrySet()) {
                    if (taskResults.getKey().endsWith(taskOnServer) && taskResults.getKey().contains(architecture) && taskResults.getKey().contains("." + machineSize)) {
                        SummaryData summaryData = new SummaryData();
                        Map<Integer, Results> summarisedResultsForTaskByServer = taskResults.getValue();
                        var maxCountIdx = summarisedResultsForTaskByServer.keySet().stream().filter(i -> summarisedResultsForTaskByServer.get(i).getTestProgram().size() > 1).mapToInt(i -> i).max().getAsInt();
                        // Which concurrency utility was quickest by server?
                        summaryData.setConcurrencyUtility(summarisedResultsForTaskByServer.values().stream().toList().getFirst().getTestProgram().getFirst());
                        summaryData.setRuntimeInSeconds(maxCountIdx);

                        // Which concurrency utility processed most records by server?
                        int totalRecords = (int)(summarisedResultsForTaskByServer.get(maxCountIdx).getThreadsRecordCount().getMean() + summarisedResultsForTaskByServer.get(maxCountIdx).getMemoryRecordCount().getMean());
                        summaryData.setTotalRecordCount(totalRecords);

                        // Which concurrency utility used the most memory by server?
//                        double usedHeapMemoryInMB = summarisedResultsForTaskByServer.get(maxCountIdx).getHeapUsed().getMean() / (1024 * 1024);
//                        double usedHeapMemoryInKB = summarisedResultsForTaskByServer.get(maxCountIdx).getHeapUsed().getMean() / 1024;
                        double usedHeapMemoryInKB = summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getHeapUsed().getMean()/1024)
                                .average()
                                .getAsDouble();

                        double usedHeapMemoryInMB = usedHeapMemoryInKB / 1024;
                        summaryData.setHeapUsed((int)(usedHeapMemoryInMB));
                        summaryData.setHeapUsedPerRecordKb(totalRecords==0 ? 0 : (int)(usedHeapMemoryInKB/totalRecords));
                        summaryData.setHeapUsedPerSecondMb((int)(usedHeapMemoryInMB/maxCountIdx));

                        int codeHeapNonNmethodsUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getCodeHeapNonNmethodsUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setCodeHeapNonNmethodsUsedMax(codeHeapNonNmethodsUsed);

                        int metaspaceUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getMetaspaceUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setMetaspaceUsed(metaspaceUsed);

                        int codeHeapProfiledNmethodsUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getCodeHeapProfiledNmethodsUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setCodeHeapProfiledNmethodsUsed(codeHeapProfiledNmethodsUsed);

                        int compressedClassSpaceUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getCompressedClassSpaceUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setCompressedClassSpaceUsed(compressedClassSpaceUsed);

                        int g1EdenSpaceUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> (entry.getValue().getG1EdenSpaceUsed().getMean()/1024/1024))
                                .average()
                                .getAsDouble();

                        summaryData.setG1EdenSpaceUsedMb(g1EdenSpaceUsed);

                        int g1OldGenUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getG1OldGenUsed().getMean()/1024)
                                .average()
                                .getAsDouble();
                        summaryData.setG1OldGenUsedKb(g1OldGenUsed);

                        int g1SurvivorSpaceUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getG1SurvivorSpaceUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setG1SurvivorSpaceUsed(g1SurvivorSpaceUsed);

                        int codeHeapNonProfiledNmethodsUsed = (int) summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> entry.getValue().getCodeHeapNonProfiledNmethodsUsed().getMean())
                                .average()
                                .getAsDouble();
                        summaryData.setCodeHeapNonProfiledNmethodsUsed(codeHeapNonProfiledNmethodsUsed);

                        double systemLoadAverage = summarisedResultsForTask
                                .entrySet()
                                .stream()
                                .filter(entry -> entry.getKey().contains(taskResults.getKey()))
                                .mapToDouble(entry -> (double)
                                        ((entry.getValue().getSystemLoadAverage().getMean() * 1000) /
                                        entry.getValue().getAvailableProcessors().getMean()) / 1000)
                                .findAny()
                                .getAsDouble();
                        summaryData.setSystemLoadAverageByProcessors(systemLoadAverage);

                        summaryDataList.add(summaryData);
                    }
                }
                mapSummaryData.put(archMachine, summaryDataList);
            }
        }
        return mapSummaryData;
    }

    private static void answerQuestionsInSummarySheet2(String taskOnServer, TreeMap<String, TreeMap<Integer, Results>> allSummarisedResults,
                                                       final TreeMap<String, Results> summarisedResultsForTask, XSSFWorkbook wb) {
        Map<String, List<SummaryData>> mapSummaryData = getResultsSummaryMap(taskOnServer, allSummarisedResults, summarisedResultsForTask);
        XSSFSheet sheet = wb.createSheet("Summary2");
        wb.setSheetOrder(sheet.getSheetName(), 0);
        wb.setActiveSheet(0);
        wb.setSelectedTab(0);

        Row rowHeadings;
        Row rowRuntimeInSeconds;
        Row rowTotalRecordCount;
        Row rowHeapUsed;
        Row rowHeapUsedPerRecordKb;
        Row heapUsedPerSecondMb;
        Row codeHeapNonNmethodsUsed;
        Row metaspaceUsed;
        Row codeHeapProfiledNmethodsUsed;
        Row compressedClassSpaceUsed;
        Row g1EdenSpaceUsed;
        Row g1OldGenUsed;
        Row g1SurvivorSpaceUsed;
        Row codeHeapNonProfiledNmethodsUsed;
        Row systemLoadAverageByProcessors;

        int rowIdx = 0;
        int columnIdx = 0;

        rowHeadings = sheet.createRow(rowIdx++);
        (rowHeadings.createCell(columnIdx)).setCellValue(taskOnServer);

        List<String> seriesTitles = new ArrayList<>();
        for(Map.Entry<String, List<SummaryData>> summaryDataEntry : mapSummaryData.entrySet()) {
            seriesTitles.add(summaryDataEntry.getKey().replace(".", "\n"));
        }
        addHeadings(sheet, 1, seriesTitles);
        addHeadings(sheet, 29, seriesTitles);
        addHeadings(sheet, 56, seriesTitles);
        addHeadings(sheet, 83, seriesTitles);
        addHeadings(sheet, 110, seriesTitles);
        addHeadings(sheet, 138, seriesTitles);
        addHeadings(sheet, 164, seriesTitles);
        addHeadings(sheet, 191, seriesTitles);
        addHeadings(sheet, 218, seriesTitles);
        addHeadings(sheet, 245, seriesTitles);
        addHeadings(sheet, 272, seriesTitles);
        addHeadings(sheet, 299, seriesTitles);
        addHeadings(sheet, 326, seriesTitles);
        addHeadings(sheet, 353, seriesTitles);

        for(int i=0; i < mapSummaryData.values().stream().findAny().get().size(); i++) {
            try {
                columnIdx=0;
                rowRuntimeInSeconds = sheet.createRow(++rowIdx);
                rowTotalRecordCount = sheet.createRow(rowIdx+28);
                rowHeapUsed = sheet.createRow(rowIdx+55);
                rowHeapUsedPerRecordKb = sheet.createRow(rowIdx+82);
                heapUsedPerSecondMb = sheet.createRow(rowIdx+109);
                codeHeapNonNmethodsUsed = sheet.createRow(rowIdx+137);
                metaspaceUsed = sheet.createRow(rowIdx+163);
                codeHeapProfiledNmethodsUsed = sheet.createRow(rowIdx+190);
                compressedClassSpaceUsed = sheet.createRow(rowIdx+217);
                g1EdenSpaceUsed = sheet.createRow(rowIdx+244);
                g1OldGenUsed = sheet.createRow(rowIdx+271);
                g1SurvivorSpaceUsed = sheet.createRow(rowIdx+298);
                codeHeapNonProfiledNmethodsUsed = sheet.createRow(rowIdx+325);
                systemLoadAverageByProcessors = sheet.createRow(rowIdx+352);

                for (Map.Entry<String, List<SummaryData>> summaryDataEntry : mapSummaryData.entrySet()) {
                    var summaryDataList = summaryDataEntry.getValue();
                    if (columnIdx == 0) {
                        (rowRuntimeInSeconds.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (rowTotalRecordCount.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (rowHeapUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (rowHeapUsedPerRecordKb.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (heapUsedPerSecondMb.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (codeHeapNonNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (metaspaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (codeHeapProfiledNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (compressedClassSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (g1EdenSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (g1OldGenUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (g1SurvivorSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (codeHeapNonProfiledNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());
                        (systemLoadAverageByProcessors.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getConcurrencyUtility());

                        columnIdx++;
                    }
                    (rowRuntimeInSeconds.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getRuntimeInSeconds());
                    (rowTotalRecordCount.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getTotalRecordCount());
                    (rowHeapUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getHeapUsed());
                    (rowHeapUsedPerRecordKb.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getHeapUsedPerRecordKb());
                    (heapUsedPerSecondMb.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getHeapUsedPerSecondMb());
                    (codeHeapNonNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getCodeHeapNonNmethodsUsedMax());
                    (metaspaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getMetaspaceUsed());
                    (codeHeapProfiledNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getCodeHeapProfiledNmethodsUsed());
                    (compressedClassSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getCompressedClassSpaceUsed());
                    (g1EdenSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getG1EdenSpaceUsedMb());
                    (g1OldGenUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getG1OldGenUsedKb());
                    (g1SurvivorSpaceUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getG1SurvivorSpaceUsed());
                    (codeHeapNonProfiledNmethodsUsed.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getCodeHeapNonProfiledNmethodsUsed());
                    (systemLoadAverageByProcessors.createCell(columnIdx)).setCellValue(summaryDataList.get(i).getSystemLoadAverageByProcessors());

                    columnIdx++;
                }
            }catch(Exception e){
                LOG.error(e.getMessage());
            }
        }
        addBarChart2(sheet, 2, 0,1, "Execution time", "Time in seconds", seriesTitles);
        addBarChart2(sheet, 30, 0,1, "Rows processed in 60 seconds", "Rows processed", seriesTitles);
        addBarChart2(sheet, 57, 0,1, "Heap Used", "Heap Used (MB)", seriesTitles);
        addBarChart2(sheet, 84, 0,1, "Heap Used per record (KB)", "Heap Used per record (KB)", seriesTitles);
        addBarChart2(sheet, 111, 0,1, "Heap Used per second (MB)", "Heap Used per second (MB)", seriesTitles);
        addBarChart2(sheet, 139, 0,1, "Max Code Heap Non-Nmethods Used", "Code Heap Non-Nmethods", seriesTitles);
        addBarChart2(sheet, 165, 0,1, "Metaspace Used", "Metaspace Used", seriesTitles);
        addBarChart2(sheet, 192, 0,1, "Code Heap Profiled Nmethods Used", "Code Heap Profiled Nmethods Used", seriesTitles);
        addBarChart2(sheet, 219, 0,1, "Compressed Class Space Used", "Compressed Class Space Used", seriesTitles);
        addBarChart2(sheet, 246, 0,1, "G1 Eden Space Used MB", "G1 Eden Space Used MB", seriesTitles);
        addBarChart2(sheet, 273, 0,1, "G1 Old Gen Used KB", "G1 Old Gen Used KB", seriesTitles);
        addBarChart2(sheet, 300, 0,1, "G1 Survivor Space Used", "G1 Survivor Space Used", seriesTitles);
        addBarChart2(sheet, 327, 0,1, "Code Heap Non Profiled Nmethods Used", "Code Heap Non Profiled Nmethods Used", seriesTitles);
        addBarChart2(sheet, 354, 0,1, "System Load Average By Processors", "System Load Average By Processors", seriesTitles);
    }

    private static void addHeadings(XSSFSheet sheet, int row, List<String> seriesTitles){
        var rowHeadings = sheet.createRow(row);
        var columnIdx = 0;

        (rowHeadings.createCell(columnIdx++)).setCellValue("Concurrency Utility");

        for(String title : seriesTitles) {
            (rowHeadings.createCell(columnIdx++)).setCellValue(title);
        }
    }

    private static void addBarChart2(XSSFSheet sheet, int row, int dataBlock, int dataColumn, String chartTitleText, String axisTitle, List<String> seriesTitles) {
        final int DATA_HEIGHT = 6;
        final int CHART_HEIGHT = 20;
        final int CHART_WIDTH = 15;
        final int RELATIVE_FIRST_DATA_ROW = 0;
        final int RELATIVE_LAST_DATA_ROW = 4;
        final int START_COLUMN = (dataBlock * 10) + 0;
        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, START_COLUMN, row + DATA_HEIGHT, START_COLUMN+CHART_WIDTH, row+CHART_HEIGHT+DATA_HEIGHT);
        XSSFChart chart = drawing.createChart(anchor);
        XDDFChartLegend legend = chart.getOrAddLegend();
        legend.setPosition(LegendPosition.TOP_RIGHT);

        chart.setTitleText(chartTitleText);
        chart.setTitleOverlay(false);
        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Concurrency Utility");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(axisTitle);
        leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
        leftAxis.setCrossBetween(AxisCrossBetween.BETWEEN);
        XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, 0, 0));
        XDDFNumericalDataSource<Double> ys1 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn, dataColumn));
        XDDFNumericalDataSource<Double> ys2 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn+1, dataColumn+1));
        XDDFNumericalDataSource<Double> ys3 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn+2, dataColumn+2));
        XDDFNumericalDataSource<Double> ys4 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn+3, dataColumn+3));
        XDDFNumericalDataSource<Double> ys5 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn+4, dataColumn+4));
        XDDFNumericalDataSource<Double> ys6 = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new CellRangeAddress(row + RELATIVE_FIRST_DATA_ROW, row + RELATIVE_LAST_DATA_ROW, dataColumn+5, dataColumn+5));
        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);
        XDDFChartData.Series series1 = data.addSeries(xs, ys1);
        XDDFChartData.Series series2 = data.addSeries(xs, ys2);
        XDDFChartData.Series series3 = data.addSeries(xs, ys3);
        XDDFChartData.Series series4 = data.addSeries(xs, ys4);
        XDDFChartData.Series series5 = data.addSeries(xs, ys5);
        XDDFChartData.Series series6 = data.addSeries(xs, ys6);
        series1.setTitle(seriesTitles.get(0), (CellReference)null);
        series2.setTitle(seriesTitles.get(1), (CellReference)null);
        series3.setTitle(seriesTitles.get(2), (CellReference)null);
        series4.setTitle(seriesTitles.get(3), (CellReference)null);
        series5.setTitle(seriesTitles.get(4), (CellReference)null);
        series6.setTitle(seriesTitles.get(5), (CellReference)null);
        chart.plot(data);
        XDDFBarChartData bar = (XDDFBarChartData)data;
        bar.setBarDirection(BarDirection.BAR);
        solidFillSeries(data, 0, PresetColor.GRAY);
        solidFillSeries(data, 1, PresetColor.DIM_GRAY);
        solidFillSeries(data, 2, PresetColor.BLACK);
        solidFillSeries(data, 3, PresetColor.DARK_SLATE_GRAY);
        solidFillSeries(data, 4, PresetColor.LIGHT_GRAY);
        solidFillSeries(data, 5, PresetColor.LIGHT_SLATE_GRAY);
    }

    private static void solidFillSeries(XDDFChartData data, int index, PresetColor color) {
        XDDFSolidFillProperties fill = new XDDFSolidFillProperties(XDDFColor.from(color));
        XDDFChartData.Series series = data.getSeries(index);

        XDDFShapeProperties properties = series.getShapeProperties();
        if (properties == null) {
            properties = new XDDFShapeProperties();
        }

        properties.setFillProperties(fill);
        series.setShapeProperties(properties);
    }

    private static TreeSet<String> getMachineSize(TreeMap<String, TreeMap<Integer, Results>> summarisedResultsForTaskOnServer) {
        return new TreeSet<>(summarisedResultsForTaskOnServer
                .keySet()
                .stream()
                .map(id -> id.split("-")[0])
                .map(id -> id.split("[^a-zA-Z0-9]")[1])
                .collect(Collectors.toSet()));
    }

    private static TreeSet<String> getArchitectures(TreeMap<String, TreeMap<Integer, Results>> summarisedResultsForTaskOnServer) {
        return new TreeSet<>(summarisedResultsForTaskOnServer
                .keySet()
                .stream()
                .map(id -> id.split("-")[1])
                .collect(Collectors.toSet()));
    }

    private static void createSheetsByTaskAndServer(String taskOnServer, TreeMap<String, TreeMap<Integer, Results>> summarisedResultsForTaskByRow,
                                                    TreeMap<String, Results> summarisedResultsForTask, XSSFWorkbook wb, List<XSSFSheet> collectionOfSheets) {
        for(Map.Entry<String, TreeMap<Integer, Results>> taskResults : summarisedResultsForTaskByRow.entrySet()) {
            if(taskResults.getKey().endsWith(taskOnServer)) {
                XSSFSheet sheet = wb.createSheet(taskResults.getKey());
                XSSFSheet sheetSummary = wb.createSheet("Summary_" + taskResults.getKey());

                Map<Integer, Results> summarisedResultsForTaskByServerAndRow = taskResults.getValue();
                Results summarisedResultsForTaskByServer = summarisedResultsForTask.get(taskResults.getKey());

                addSheetHeadings(sheet, 0);
                for (Map.Entry<Integer, Results> entry : summarisedResultsForTaskByServerAndRow.entrySet()) {
                    addMeanResult(entry, sheet);
                }

                addSheetHeadings(sheetSummary, 0);
                addMeanResult(1,summarisedResultsForTaskByServer, sheetSummary);
                addMinResult(2,summarisedResultsForTaskByServer, sheetSummary);
                addMaxResult(3,summarisedResultsForTaskByServer, sheetSummary);
                addStandardDeviationResult(4, summarisedResultsForTaskByServer, sheetSummary);
                add25thPercentileResult(5, summarisedResultsForTaskByServer, sheetSummary);
                add50thPercentileResult(6, summarisedResultsForTaskByServer, sheetSummary);
                add75thPercentileResult(7, summarisedResultsForTaskByServer, sheetSummary);

                collectionOfSheets.add(sheet);
                collectionOfSheets.add(sheetSummary);
            }
        }
    }

    private static void addSheetHeadings(XSSFSheet sheet, int rowIdx) {
        Row row;
        row = sheet.createRow(rowIdx);

        if(sheet.getSheetName().contains("Summary")) {
            (row.createCell(0)).setCellValue("Aggrigation");
        } else {
            (row.createCell(0)).setCellValue("Seconds");
        }
        (row.createCell(1)).setCellValue("TestProgram");
        (row.createCell(2)).setCellValue("CodeHeapNonNmethodsUsed");
        (row.createCell(3)).setCellValue("CodeHeapNonNmethodsCommitted");
        (row.createCell(4)).setCellValue("MetaspaceUsed");
        (row.createCell(5)).setCellValue("MetaspaceCommitted");
        (row.createCell(6)).setCellValue("CodeHeapProfiledNmethodsUsed");
        (row.createCell(7)).setCellValue("CodeHeapProfiledNmethodsCommitted");
        (row.createCell(8)).setCellValue("CompressedClassSpaceUsed");
        (row.createCell(9)).setCellValue("CompressedClassSpaceCommitted");
        (row.createCell(10)).setCellValue("G1EdenSpaceUsed");
        (row.createCell(11)).setCellValue("G1EdenSpaceCommitted");
        (row.createCell(12)).setCellValue("G1OldGenUsed");
        (row.createCell(13)).setCellValue("G1OldGenCommitted");
        (row.createCell(14)).setCellValue("G1SurvivorSpaceUsed");
        (row.createCell(15)).setCellValue("G1SurvivorSpaceCommitted");
        (row.createCell(16)).setCellValue("CodeHeapNonProfiledNmethodsUsed");
        (row.createCell(17)).setCellValue("CodeHeapNonProfiledNmethodsCommitted");
        (row.createCell(18)).setCellValue("HeapUsed");
        (row.createCell(19)).setCellValue("HeapCommitted");
        (row.createCell(20)).setCellValue("SystemLoadAverage");
        (row.createCell(21)).setCellValue("AvailableProcessors");
        (row.createCell(22)).setCellValue("Name");
        (row.createCell(23)).setCellValue("Arch");
        (row.createCell(24)).setCellValue("Version");
        (row.createCell(25)).setCellValue("Memory");
        (row.createCell(26)).setCellValue("Threads");
    }

    private static void addMeanResult(Map.Entry<Integer, Results> entry, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(entry.getKey().intValue());
        (row.createCell(0)).setCellValue(entry.getKey().intValue());
        (row.createCell(1)).setCellValue(entry.getValue().getTestProgram().getFirst());
        (row.createCell(2)).setCellValue(entry.getValue().getCodeHeapNonNmethodsUsed().getMean());
        (row.createCell(3)).setCellValue(entry.getValue().getCodeHeapNonNmethodsCommitted().getMean());
        (row.createCell(4)).setCellValue(entry.getValue().getMetaspaceUsed().getMean());
        (row.createCell(5)).setCellValue(entry.getValue().getMetaspaceCommitted().getMean());
        (row.createCell(6)).setCellValue(entry.getValue().getCodeHeapProfiledNmethodsUsed().getMean());
        (row.createCell(7)).setCellValue(entry.getValue().getCodeHeapProfiledNmethodsCommitted().getMean());
        (row.createCell(8)).setCellValue(entry.getValue().getCompressedClassSpaceUsed().getMean());
        (row.createCell(9)).setCellValue(entry.getValue().getCompressedClassSpaceCommitted().getMean());
        (row.createCell(10)).setCellValue(entry.getValue().getG1EdenSpaceUsed().getMean());
        (row.createCell(11)).setCellValue(entry.getValue().getG1EdenSpaceCommitted().getMean());
        (row.createCell(12)).setCellValue(entry.getValue().getG1OldGenUsed().getMean());
        (row.createCell(13)).setCellValue(entry.getValue().getG1OldGenCommitted().getMean());
        (row.createCell(14)).setCellValue(entry.getValue().getG1SurvivorSpaceUsed().getMean());
        (row.createCell(15)).setCellValue(entry.getValue().getG1SurvivorSpaceCommitted().getMean());
        (row.createCell(16)).setCellValue(entry.getValue().getCodeHeapNonProfiledNmethodsUsed().getMean());
        (row.createCell(17)).setCellValue(entry.getValue().getCodeHeapNonProfiledNmethodsCommitted().getMean());
        (row.createCell(18)).setCellValue(entry.getValue().getHeapUsed().getMean());
        (row.createCell(19)).setCellValue(entry.getValue().getHeapCommitted().getMean());
        (row.createCell(20)).setCellValue(entry.getValue().getSystemLoadAverage().getMean());
        (row.createCell(21)).setCellValue(entry.getValue().getAvailableProcessors().getMean());
        (row.createCell(22)).setCellValue(entry.getValue().getName().getFirst());
        (row.createCell(23)).setCellValue(entry.getValue().getArch().getFirst());
        (row.createCell(24)).setCellValue(entry.getValue().getVersion().getFirst());
        (row.createCell(25)).setCellValue(entry.getValue().getMemoryRecordCount().getMean());
        (row.createCell(26)).setCellValue(entry.getValue().getThreadsRecordCount().getMean());
    }

    private static void addMeanResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("Mean");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getMean());
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getMean());
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getMean());
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getMean());
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getMean());
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getMean());
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getMean());
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getMean());
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getMean());
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getMean());
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getMean());
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getMean());
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getMean());
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getMean());
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getMean());
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getMean());
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getMean());
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getMean());
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getMean());
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getMean());
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getMean());
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getMean());
    }

    private static void addMaxResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("Max");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getMax());
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getMax());
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getMax());
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getMax());
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getMax());
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getMax());
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getMax());
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getMax());
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getMax());
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getMax());
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getMax());
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getMax());
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getMax());
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getMax());
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getMax());
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getMax());
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getMax());
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getMax());
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getMax());
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getMax());
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getMax());
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getMax());
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static void addMinResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("Min");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getMin());
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getMin());
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getMin());
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getMin());
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getMin());
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getMin());
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getMin());
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getMin());
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getMin());
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getMin());
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getMin());
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getMin());
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getMin());
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getMin());
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getMin());
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getMin());
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getMin());
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getMin());
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getMin());
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getMin());
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getMin());
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getMin());
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static void addStandardDeviationResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("Standard Deviation");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getStandardDeviation());
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getStandardDeviation());
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getStandardDeviation());
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getStandardDeviation());
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getStandardDeviation());
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getStandardDeviation());
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getStandardDeviation());
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getStandardDeviation());
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getStandardDeviation());
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getStandardDeviation());
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getStandardDeviation());
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getStandardDeviation());
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getStandardDeviation());
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getStandardDeviation());
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getStandardDeviation());
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getStandardDeviation());
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getStandardDeviation());
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getStandardDeviation());
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getStandardDeviation());
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getStandardDeviation());
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getStandardDeviation());
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getStandardDeviation());
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static void add25thPercentileResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("25th Percentile");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getPercentile(25d));
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getPercentile(25d));
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getPercentile(25d));
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getPercentile(25d));
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getPercentile(25d));
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getPercentile(25d));
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getPercentile(25d));
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getPercentile(25d));
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getPercentile(25d));
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getPercentile(25d));
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getPercentile(25d));
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getPercentile(25d));
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getPercentile(25d));
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getPercentile(25d));
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getPercentile(25d));
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getPercentile(25d));
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getPercentile(25d));
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getPercentile(25d));
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getPercentile(25d));
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getPercentile(25d));
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getPercentile(25d));
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getPercentile(25d));
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static void add50thPercentileResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("50th Percentile");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getPercentile(50d));
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getPercentile(50d));
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getPercentile(50d));
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getPercentile(50d));
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getPercentile(50d));
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getPercentile(50d));
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getPercentile(50d));
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getPercentile(50d));
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getPercentile(50d));
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getPercentile(50d));
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getPercentile(50d));
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getPercentile(50d));
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getPercentile(50d));
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getPercentile(50d));
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getPercentile(50d));
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getPercentile(50d));
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getPercentile(50d));
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getPercentile(50d));
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getPercentile(50d));
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getPercentile(50d));
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getPercentile(50d));
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getPercentile(50d));
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static void add75thPercentileResult(int rowIdx, Results results, XSSFSheet sheet) {
        Row row;
        row = sheet.createRow(rowIdx);
        (row.createCell(0)).setCellValue("75th Percentile");
        (row.createCell(1)).setCellValue(results.getTestProgram().getFirst());
        (row.createCell(2)).setCellValue((long) results.getCodeHeapNonNmethodsUsed().getPercentile(75d));
        (row.createCell(3)).setCellValue((long) results.getCodeHeapNonNmethodsCommitted().getPercentile(75d));
        (row.createCell(4)).setCellValue((long) results.getMetaspaceUsed().getPercentile(75d));
        (row.createCell(5)).setCellValue((long) results.getMetaspaceCommitted().getPercentile(75d));
        (row.createCell(6)).setCellValue((long) results.getCodeHeapProfiledNmethodsUsed().getPercentile(75d));
        (row.createCell(7)).setCellValue((long) results.getCodeHeapProfiledNmethodsCommitted().getPercentile(75d));
        (row.createCell(8)).setCellValue((long) results.getCompressedClassSpaceUsed().getPercentile(75d));
        (row.createCell(9)).setCellValue((long) results.getCompressedClassSpaceCommitted().getPercentile(75d));
        (row.createCell(10)).setCellValue((long) results.getG1EdenSpaceUsed().getPercentile(75d));
        (row.createCell(11)).setCellValue((long) results.getG1EdenSpaceCommitted().getPercentile(75d));
        (row.createCell(12)).setCellValue((long) results.getG1OldGenUsed().getPercentile(75d));
        (row.createCell(13)).setCellValue((long) results.getG1OldGenCommitted().getPercentile(75d));
        (row.createCell(14)).setCellValue((long) results.getG1SurvivorSpaceUsed().getPercentile(75d));
        (row.createCell(15)).setCellValue((long) results.getG1SurvivorSpaceCommitted().getPercentile(75d));
        (row.createCell(16)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsUsed().getPercentile(75d));
        (row.createCell(17)).setCellValue((long) results.getCodeHeapNonProfiledNmethodsCommitted().getPercentile(75d));
        (row.createCell(18)).setCellValue((long) results.getHeapUsed().getPercentile(75d));
        (row.createCell(19)).setCellValue((long) results.getHeapCommitted().getPercentile(75d));
        (row.createCell(20)).setCellValue(results.getSystemLoadAverage().getPercentile(75d));
        (row.createCell(21)).setCellValue((long) results.getAvailableProcessors().getPercentile(75d));
        (row.createCell(25)).setCellValue((long) results.getMemoryRecordCount().getPercentile(75d));
        (row.createCell(26)).setCellValue((long) results.getThreadsRecordCount().getPercentile(75d));
        (row.createCell(22)).setCellValue(results.getName().getFirst());
        (row.createCell(23)).setCellValue(results.getArch().getFirst());
        (row.createCell(24)).setCellValue(results.getVersion().getFirst());
    }

    private static boolean copyDataToDestination(final String destinationDir, final String repoPath) {
        final var destinationPath = getDestinationPath(destinationDir);

        try (final var git = Git.open(new File(repoPath))) {
            var branches = git.branchList()
                    .setListMode(ListBranchCommand.ListMode.ALL)
                    .call();
            for(var branch: branches) {
                copyDataFromAllBranches(branch, git, destinationPath);
            }
        } catch (GitAPIException | IOException | RevisionSyntaxException e){
            LOG.error("File copy failed.");
            return false;
        }
        return true;
    }

    private static Path getDestinationPath(String destinationDir) {
        var destinationPath = Paths.get(destinationDir);
        if (!Files.exists(destinationPath)) {
            try {
                Files.createDirectories(destinationPath);
            } catch (IOException e) {
                LOG.error("Destination directory could not be created");
            }
        }
        return destinationPath;
    }

    private static void copyDataFromAllBranches(final Ref branch, final Git git, final Path destinationPath)
            throws GitAPIException, IOException, RevisionSyntaxException {
        git.checkout().setName(branch.getName()).call();

        try (final var repository = git.getRepository();
            final var revWalk = new RevWalk(repository)) {
            final var head = repository.resolve("HEAD");
            revWalk.markStart(revWalk.parseCommit(head));
            RevCommit commit;

            while (Objects.nonNull(commit = revWalk.next())) {
                try (final var treeWalk = new TreeWalk(repository)) {
                    treeWalk.addTree(commit.getTree());
                    treeWalk.setRecursive(true);

                    while (treeWalk.next()) {
                        if (treeWalk.getPathString().endsWith(".csv")) {
                            copyFileToDestination(destinationPath, repository, treeWalk);
                        }
                    }
                }
            }
        }
    }

    private static void copyFileToDestination(final Path destinationPath, final Repository repository,
                                              final TreeWalk treeWalk) throws IOException {
        final var loader = repository.open(treeWalk.getObjectId(0));
        final var bytes = loader.getBytes();
        final var filePath = destinationPath.resolve(treeWalk.getPathString());
        Files.write(filePath, bytes);

        LOG.info("File: {}", filePath);
    }
}
