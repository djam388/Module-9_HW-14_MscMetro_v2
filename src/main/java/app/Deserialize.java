package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gson.deserialize.StationIndexDeserializer;
import model.Line;
import utils.StationIndex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

public class Deserialize {
    public static void main(String[] args) throws IOException {
        String fileName = "src/main/java/data/map_MSC.json";
        Path path = Paths.get(fileName);
        StationIndex stationIndex;

        try (
                Reader reader = Files.newBufferedReader(path,
                    StandardCharsets.UTF_8))
        {
            JsonParser parser = new JsonParser();
            JsonElement tree = parser.parse(reader);

            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(StationIndex.class, new StationIndexDeserializer())
                    .create();
            stationIndex = gson.fromJson(tree, StationIndex.class);
        }

        System.out.println("Deserialization finished");

        System.out.println("\nОбщее количество линий метро г.Москвы: " + stationIndex.getLines().size()
                + "\nВсего количество станиций метро г.Москвы: " + stationIndex.getStations().size() +"\n");

        System.out.printf("%-36s%-20s","Название","Количество станций");
        System.out.println();
        System.out.println("---------------------------------   ------------------");

        for (Map.Entry<Double, Line> line : stationIndex.getLines().entrySet())
        {
            System.out.printf("%-36s%-20d",line.getValue().getName(),
                    line.getValue().getStations().size());
            System.out.println();
        }
    }
}
