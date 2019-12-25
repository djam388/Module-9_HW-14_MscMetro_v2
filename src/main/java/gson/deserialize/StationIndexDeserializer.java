package gson.deserialize;

import com.google.gson.*;
import model.Line;
import model.Station;
import utils.StationIndex;
import utils.TextNumberToDouble;

import javax.swing.text.Element;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class StationIndexDeserializer implements JsonDeserializer<StationIndex> {
    @Override
    public StationIndex deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException
    {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        TextNumberToDouble convertToDouble = new TextNumberToDouble();
        StationIndex stationIndex = new StationIndex();

        Set<Map.Entry<String, JsonElement>> entries = jsonObject.entrySet();
        for (Map.Entry<String, JsonElement> elementEntry : entries)
        {
            if (elementEntry.getKey().equals("lines"))
            {
                JsonArray lines = elementEntry.getValue().getAsJsonArray();
                for (JsonElement element : lines) //create lines
                {
                    JsonObject line = (JsonObject) element;
                    stationIndex.addLine(new Line(convertToDouble.getInDouble(line.get("number").getAsString()),
                            line.get("name").getAsString(),
                            line.get("color").getAsString()));
                }
            }
            if (elementEntry.getKey().equals("stations"))
            {
                JsonObject object = elementEntry.getValue().getAsJsonObject();
                Set<Map.Entry<String, JsonElement>> entrySet = object.entrySet();
                for (Map.Entry<String,JsonElement> entry : entrySet)
                {
                    entry.getValue().getAsJsonArray().forEach(station ->
                            stationIndex.addStation(new Station(station.getAsString(),
                                    stationIndex.getLine(convertToDouble.getInDouble(entry.getKey())))));
                }
            }
            if (elementEntry.getKey().equals("connections"))
            {
                JsonArray connections = elementEntry.getValue().getAsJsonArray();

                for (JsonElement element : connections) //create lines
                {
                    ArrayList<Station> stations = new ArrayList<>();
                    for (JsonElement connectedStation : element.getAsJsonArray())
                    {
                        JsonObject station = (JsonObject) connectedStation;
                        stations.add(new Station(station.get("station").getAsString(),
                                stationIndex.getLine(convertToDouble.getInDouble(station.get("line").getAsString()))));
                    }
                    stationIndex.addConnection(stations);
                }
            }
        }
        return stationIndex;
    }
}
