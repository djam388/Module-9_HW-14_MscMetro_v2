package gson.serialize;

import com.google.gson.*;
import model.Station;
import utils.StationIndex;

import java.lang.reflect.Type;
import java.util.*;

public class StationIndexSerializer implements JsonSerializer<StationIndex> {

    @Override
    public JsonElement serialize(StationIndex stationIndex, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject result = new JsonObject();
        JsonArray lines = new JsonArray();
        JsonObject lineWithStations = new JsonObject();
        JsonArray connectionArray = new JsonArray();

        TreeMap<Station, TreeSet<Station>> connections = stationIndex.getConnections();

        // Lines JSON format
        for(Double lineNumber : stationIndex.getLines().keySet()) {
            String  lineNumberInString = convertLineNumberToString(stationIndex.getLines().get(lineNumber).getNumber());
            JsonObject line = new JsonObject();
            line.addProperty("number", lineNumberInString);
            line.addProperty("name", stationIndex.getLines().get(lineNumber).getName());
            line.addProperty("color", stationIndex.getLines().get(lineNumber).getColor());
            lines.add(line);
        }
        result.add("lines", lines);

        // Stations by Line JSON format
        for(Double lineNumber : stationIndex.getLines().keySet())
        {
            JsonArray stations = new JsonArray();
            for (Station station : stationIndex.getLines().get(lineNumber).getStations())
            {
                stations.add(station.getName());
            }
            lineWithStations.add(convertLineNumberToString(stationIndex.getLines().get(lineNumber).getNumber()), stations);
        }
        result.add("stations", lineWithStations);

        // Connections JSON format
        List<Station> connectionToExclude = new ArrayList<>();
        Set<Station> keySet = connections.keySet();
        for (Map.Entry<Station, TreeSet<Station>> key : connections.entrySet())
        {
            boolean skipIt = false;
            for (Station station : connectionToExclude)
            {
                if (station.getName() == key.getKey().getName()
                        && station.getLine().getNumber() == key.getKey().getLine().getNumber())
                {
                    skipIt = true;
                }
            }
            if (!skipIt) {
                boolean newLoop = true;
                JsonArray connection = new JsonArray();

                for (Station station : connections.get(key.getKey())) {
                    JsonObject connectedStation = new JsonObject();
                    if (newLoop) {
                        connectedStation.addProperty("line",
                                convertLineNumberToString(key.getKey().getLine().getNumber()));
                        connectedStation.addProperty("station", key.getKey().getName());
                        connection.add(connectedStation);
                        connectionToExclude.add(key.getKey());
                        connectedStation = new JsonObject();
                        newLoop = false;
                    }
                    connectedStation.addProperty("line",
                            convertLineNumberToString(station.getLine().getNumber()));
                    connectedStation.addProperty("station", station.getName());
                    connection.add(connectedStation);
                    connectionToExclude.add(station);
                }
                connectionArray.add(connection);
            }
        }

        result.add("connections", connectionArray);


        return result;

    }

    private String convertLineNumberToString (Double lineNumber)
    {
        double fPart = lineNumber - lineNumber.intValue();
        if (fPart == 0.5)
        {
            return lineNumber.intValue() + "A";
        }
        else
        {
            return String.valueOf(lineNumber.intValue());
        }
    }
}
