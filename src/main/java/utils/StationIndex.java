package utils;

import com.google.gson.annotations.Expose;
import model.Line;
import model.Station;

import java.util.*;
import java.util.stream.Collectors;

public class StationIndex
{
    Map<Double, Line> number2line;
    TreeSet<Station> stations;
    TreeMap<Station, TreeSet<Station>> connections;

    public StationIndex()
    {
        number2line = new TreeMap<>();
        stations = new TreeSet<>();
        connections = new TreeMap<>();
    }

    public void addStation(Station station)
    {
        stations.add(station);
    }

    public void addLine(Line line)
    {
        number2line.put(line.getNumber(), line);
    }

    public void addConnection(List<Station> stations)
    {
        for(Station station : stations)
        {
            if(!connections.containsKey(station)) {
                connections.put(station, new TreeSet<>());
            }
            TreeSet<Station> connectedStations = connections.get(station);
            connectedStations.addAll(stations.stream()
                    .filter(s -> !s.equals(station)).collect(Collectors.toList()));
        }
    }

    public TreeSet<Station> getStations()
    {
        return stations;
    }

    public Map<Double, Line> getLines()
    {
        return number2line;
    }

    public TreeMap<Station, TreeSet<Station>> getConnections()
    {
        return connections;
    }

    public Line getLine(double number)
    {
        return number2line.get(number);
    }

    public Station getStation(String name)
    {
        for(Station station : stations)
        {
            if(station.getName().equalsIgnoreCase(name)) {
                return station;
            }
        }
        return null;
    }

    public Station getStation(String name, double lineNumber)
    {
        Station query = new Station(name, getLine(lineNumber));
        Station station = stations.ceiling(query);
        return station.equals(query) ? station : null;
    }

    public Set<Station> getConnectedStations(Station station)
    {
        if(connections.containsKey(station)) {
            return connections.get(station);
        }
        return new TreeSet<>();
    }
}

