package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gson.serialize.StationIndexSerializer;
import model.Line;
import model.Station;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.StationIndex;
import utils.TextNumberToDouble;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Serialize
{

    private static Map<Double, Line> lines = new TreeMap<>();
    //private static ArrayList<Line> sortedLinesLA = new ArrayList<>();
    private static ArrayList<String> listStationsFlat = new ArrayList<>();
    private static Map<String, List<String>> connectionsFlat = new HashMap<>();
    private static StationIndex stationIndex = new StationIndex();
    public static void main(String[] args)
    {
        String htmlString = null;

        try {
            htmlString = Jsoup.connect("https://ru.wikipedia.org/wiki/%D0%A1%D0%BF%D0%B8%D1%81%D0%BE%D0%BA_%D1%81%D1%82%D0%B0%D0%BD%D1%86%D0%B8%D0%B9_%D0%9C%D0%BE%D1%81%D0%BA%D0%BE%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE_%D0%BC%D0%B5%D1%82%D1%80%D0%BE%D0%BF%D0%BE%D0%BB%D0%B8%D1%82%D0%B5%D0%BD%D0%B0")
                    .maxBodySize(0)
                    .get()
                    .html();
        } catch (IOException e) {
            e.printStackTrace();
        }

        parseHTML(htmlString);

        initializeStationIndex();

        System.out.println("Parsing finished");

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(StationIndex.class, new StationIndexSerializer())
                .create();
        //String json = gson.toJson(stationIndex);
        //System.out.println(json);

        try (FileWriter writer = new FileWriter("src/main/java/data/map_MSC.json")) {
            gson.toJson(stationIndex, writer);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("JSON created");

    }

    private static void initializeStationIndex()
    {
        for (Double lineNumber : lines.keySet()) // добавление линии и станции в переменную stationIndex
        {
            //sortedLinesLA.add(lines.get(lineNumber));
            stationIndex.addLine(lines.get(lineNumber));
            lines.get(lineNumber).getStations().forEach(station -> {
                stationIndex.addStation(new Station(station.getName(), station.getLine()));
            });
        }

        for (String station : connectionsFlat.keySet()) // добавление перехода в переменную stationIndex
        {
            ArrayList<Station> stations = new ArrayList<>();
            //Station stationConnected = stationIndex.getStation(station);
            stations.add(new Station(station.substring(station.indexOf(",") + 2),
                    lines.get(Double.parseDouble(station.substring(0, station.indexOf(",") - 1)))));
            for (String info : connectionsFlat.get(station))
            {
                //stationConnected = stationIndex.getStation(extractStationName(info, listStations));
                if (info.equals("Переход на станцию Деловой центр Калининской линии")) // на Калининской линии нет станции Деловой центр
                {
                    //System.out.println("stop!");
                    stations.add(extractStationName("Переход на станцию Деловой центр Солнцевской линии", lines));
                }
                else
                {
                    stations.add(extractStationName(info, lines));
                }
            }
            stationIndex.addConnection(stations);
        }
    }

    private static void parseHTML(String htmlString)
    {
        Double lineNumberInDouble = 0.0;
        Document doc = Jsoup.parse(htmlString);
        TextNumberToDouble convertToDouble = new TextNumberToDouble();
        for (int tbl = 3; tbl < 6; tbl++) {
            Element table = doc.select("tbody").get(tbl);
            Elements rows = table.select("tr");
            int rowNumber;
            if (tbl == 3)
            {
                rowNumber = 1;
            }
            else
            {
                rowNumber = 2;
            }
            for (int i = rowNumber; i < rows.size(); i++) {
                boolean connectionFound = false;
                Element row = rows.get(i);
                Elements colLineDetails = row.select("td");
                Elements connectedStations = colLineDetails.get(3).select("span");
                if (connectedStations.size() > 0)
                {
                    connectionFound = true;
                }
                ArrayList<String> connectedStation = new ArrayList<>();
                String stationInfo;
                Line lineToCheck;


                int foundIndex = colLineDetails.get(1).select("a").attr("title").indexOf("(") > 0 ?
                        colLineDetails.get(1).select("a").attr("title").indexOf("(") :
                        colLineDetails.get(1).select("a").attr("title").length();

                if (!colLineDetails.get(0).text().contains(" ")) // проверка на присутствие другой линии для одной станции
                { // если одна линия
                    if (colLineDetails.get(0).text().substring(0, 1).equals("0")) // проверяем есть ли "0" перед номером линии
                    { // если есть, убираем "0"
                        lineNumberInDouble = convertToDouble.getInDouble(colLineDetails.get(0).text().substring(1, (colLineDetails.get(0).text().length() - 2)));
                        stationInfo = lineNumberInDouble.toString()
                                + ", "
                                + colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1);
                        if (connectionFound)
                        {
                            connectionsFlat.putAll(addConnectedStationsFlat(stationInfo, connectedStations));
                        }
                        listStationsFlat.add(stationInfo); //формируется список станций с номером линии
                        if (lines.containsKey(lineNumberInDouble)) //проверка на существовании линии в списке
                        { //если есть, получаем данные для проверки цвета -> для коррекции
                            lineToCheck = lines.get(lineNumberInDouble);
                            if (lineToCheck.getColor().equals("#000000")) {//если не был определен цвет, меняем на правильный цвет
                                lines.replace(lineNumberInDouble,
                                        new Line(lineNumberInDouble,
                                                colLineDetails.get(0).select("span").attr("title"),
                                                (colLineDetails.get(0).attr("style").length() > 0 ? colLineDetails.get(0).attr("style").replaceAll(("background:"), "") : "#000000"))); // #000000 используется, если при парсинге не был обнаружен цвет
                            }
                        } else {//добавляем новую линию
                            lines.put(lineNumberInDouble,
                                    new Line(lineNumberInDouble,
                                            colLineDetails.get(0).select("span").attr("title"),
                                            (colLineDetails.get(0).attr("style").length() > 0 ? colLineDetails.get(0).attr("style").replaceAll(("background:"), "") : "#000000"))); // #000000 используется, если при парсинге не был обнаружен цвет
                        }


                    } else {//используем номер линии без корректировки

                        lineNumberInDouble = convertToDouble.getInDouble(colLineDetails.get(0).text().substring(0, (colLineDetails.get(0).text().length() - 2)));
                        stationInfo = lineNumberInDouble
                                + ", "
                                + colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1);
                        if (connectionFound)
                        {
                            connectionsFlat.putAll(addConnectedStationsFlat(stationInfo, connectedStations));
                        }
                        listStationsFlat.add(stationInfo); //формируется список станций с номером линии
                        if (lines.containsKey(lineNumberInDouble)) //проверка на существовании линии в списке
                        { // если есть, получаем данные для проверки цвета -> для коррекции
                            lineToCheck = lines.get(lineNumberInDouble);
                            if (lineToCheck.getColor().equals("#000000")) { // если не был определен цвет, меняем на правильный цвет
                                lines.replace(lineNumberInDouble,
                                        new Line(lineNumberInDouble,
                                                colLineDetails.get(0).select("span").attr("title"),
                                                (colLineDetails.get(0).attr("style").length() > 0 ? colLineDetails.get(0).attr("style").replaceAll(("background:"), "") : "#000000"))); // #000000 используется, если при парсинге не был обнаружен цвет
                            }
                        } else { // добавляем новую линию
                            lines.put(lineNumberInDouble,
                                    new Line(lineNumberInDouble,
                                            colLineDetails.get(0).select("span").attr("title"),
                                            (colLineDetails.get(0).attr("style").length() > 0 ? colLineDetails.get(0).attr("style").replaceAll(("background:"), "") : "#000000")));
                        }

                    }
                } else { // если станция относится к двум линия, то разбиваем поле с двумя значениями на отдельные фрагменты
                    String[] fragments = colLineDetails.get(0).text().split(" ");
                    stationInfo = convertToDouble.getInDouble(fragments[0]) + ", " + colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1);
                    listStationsFlat.add(stationInfo);
                    if (connectionFound)
                    {
                        connectionsFlat.putAll(addConnectedStationsFlat(stationInfo, connectedStations));
                    }
                    stationInfo = convertToDouble.getInDouble(fragments[1].substring(0, 2)) + ", " + colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1);
                    listStationsFlat.add(stationInfo);
                    if (connectionFound)
                    {
                        connectionsFlat.putAll(addConnectedStationsFlat(stationInfo, connectedStations));
                    }
                }
            }
        }
        Line linesToEdit;
        for (String station : listStationsFlat) // добавляем станции к линиям соответственно
        {
            String[] fragments = station.split(", ");
            linesToEdit = lines.get(Double.parseDouble(fragments[0]));
            linesToEdit.addStation(new Station(fragments[1], lines.get(Double.parseDouble(fragments[0]))));

        }
    }

    private static Map<String, List<String>> addConnectedStationsFlat(String stationInfo, Elements connectedStationsInfo)
    {
        ArrayList<String> addedConnectedStation = new ArrayList<>();
        Map<String, List<String>> addedConnectionsFlat = new HashMap<>();
        for (Element station : connectedStationsInfo) //предварительные данные для переходов между станциями
        {

            if (station.attr("title").length() > 0) {
                addedConnectedStation.add(station.attr("title"));
                //System.out.println(stationInfo + ": " + station.attr("title"));
                //System.out.println(colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1)
                //        + ": "
                //        + station.attr("title"));
            }
            //connectionsFlat.put(colLineDetails.get(1).select("a").attr("title").substring(0, foundIndex - 1),
            //    connectedStation);
        }
        addedConnectionsFlat.put(stationInfo, addedConnectedStation);
        return addedConnectionsFlat;
    }

    private static Station extractStationName (String title, Map<Double, Line> stationsToCompare)
    {
        String dataToExtractLine = null;
        String foundStation = null;
        boolean searchCompleted = false;
        for (Double stationNumber : stationsToCompare.keySet())
        {
            List<Station> stationNames = stationsToCompare.get(stationNumber).getStations();
            for (Station station : stationNames){
                Pattern pattern;
                pattern = Pattern.compile("(" + "ю " + station.getName() + ")", Pattern.MULTILINE);
                Matcher matcher = pattern.matcher(title);
                boolean stationFound = (matcher.find());
                if (stationFound)
                {
                    //System.out.println(title);
                    foundStation = matcher.group(0);
                    int startIndex = title.indexOf(matcher.group(0)) + foundStation.length();
                    dataToExtractLine = title.substring(startIndex + 1).trim();
                    //System.out.println(dataToExtractLine);
                    if (dataToExtractLine.equals("Московского центрального кольца"))
                    {
                        dataToExtractLine = "Московское центральное кольцо";
                    }
                    else if (dataToExtractLine.equals("Московского монорельса"))
                    {
                        dataToExtractLine = "Московский монорельс";
                    }
                    else if (dataToExtractLine.equals("Большой кольцевой линии"))
                    {
                        dataToExtractLine = "Большая кольцевая линия";
                    }
                    else
                    {
                        dataToExtractLine = dataToExtractLine.substring(0, dataToExtractLine.indexOf("ой линии"))
                                + "ая линия";
                    }
                    //Московского центрального кольца -> Московское центральное кольцо
                    //Московского монорельса -> Московский монорельс
                    //Большой кольцевой линии -> Большая кольцевая линия
                    //Кольцевой линии -> Кольцевая линия
                    //System.out.println(dataToExtractLine);
                }
            }
        }

        for (Double stationNumber : stationsToCompare.keySet())
        {
            if (stationsToCompare.get(stationNumber).getName().equals(dataToExtractLine))
            {
                for (Station station : stationsToCompare.get(stationNumber).getStations())
                {
                    if (station.getName().equals(foundStation.substring(2)))
                    {
                        return station;
                    }
                }
            }
        }
        return null;
    }
}
