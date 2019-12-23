package model;

import java.util.ArrayList;
import java.util.List;

public class Line implements Comparable<Line>
{
    private double number;
    private String name;
    private List<Station> stations;
    private String color;

    public Line(double number, String name, String color)
    {
        this.number = number;
        this.name = name;
        stations = new ArrayList<>();
        this.color = color;
    }

    public double getNumber()
    {
        return number;
    }

    public String getName()
    {
        return name;
    }

    public void addStation(Station station)
    {
        stations.add(station);
    }

    public List<Station> getStations()
    {
        return stations;
    }

    public String getColor()
    {
        return color;
    }

    @Override
    public int compareTo(Line line)
    {
        return Double.compare(number, line.getNumber());
        //return Integer.compare(number, line.getNumber());
    }

    @Override
    public boolean equals(Object obj)
    {
        return compareTo((Line) obj) == 0;
    }
}
