package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextNumberToDouble {

    public double getInDouble (String numberInText)
    {
        return convertLineNumberToDouble (numberInText);
    }

    private static double convertLineNumberToDouble (String lineNumber)
    {
        Pattern pattern = Pattern.compile("\\D+", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(lineNumber);
        boolean letterFound = (matcher.find());

        if (!letterFound)
        {
            return Double.parseDouble(lineNumber);
        }
        else
        {
            return Double.parseDouble(lineNumber.substring(0, lineNumber.indexOf(matcher.group(0)))) + 0.5;
        }
    }
}
