package com.im.bioassay.doseresponse;

import com.im.bioassay.curvefit.FourPLModel;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by timbo on 19/04/2014.
 */
public class DoseResponseUtils {

    public static final String DELIMITER = "#END";

    public Iterator<DoseResponseResult> doseResponseIterator(final BufferedReader reader) {
        return new Iterator<DoseResponseResult>() {

            StringBuilder builder;

            @Override
            public boolean hasNext() {
                return buildNextLines();
            }

            @Override
            public DoseResponseResult next() {
                if (builder != null || buildNextLines()) {
                    String s = builder.toString();
                    builder = null;
                    return toDoseResponseResult(s);
                }
                builder = null;
                throw new NoSuchElementException("No more data");
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove() not supported");
            }

            private boolean buildNextLines() {
                this.builder = null;
                StringBuilder b = new StringBuilder();
                String line;
                int count = 0;
                try {
                    while (true) {
                        line = reader.readLine();
                        if (line == null || line.startsWith(DELIMITER)) {
                            break;
                        }
                        count++;
                        b.append(line).append("\n");
                    }
                } catch (IOException ioe) {
                    throw new RuntimeException("Reading failed", ioe);
                }
                if (count > 0) {
                    this.builder = b;
                    return true;
                }
                return false;
            }

        };
    }

    static List<String> validate(DoseResponseResult drr) {
        List<String> errs = new ArrayList<String>();
        if (drr.getXValues() == null) {
            errs.add("No X values specified");
        }
        if (drr.getYValues() == null) {
            errs.add("No Y values specified");
        }
        if (drr.getXValues() == null && drr.getYValues() == null) {
            int count = drr.getXValues().size();
            int c = 0;
            for (Double d : drr.getXValues()) {
                if (d == null) errs.add("X values contain null values");
            }
            for (List<Double> v : drr.getYValues()) {
                c++;
                if (count != v.size()) {
                    errs.add("Y value count does not match X value count (set " + c + ")");
                }
                for (Double d : v) {
                    if (d == null) errs.add("Y values contain null values");
                }
            }

        }
        if (!errs.isEmpty()) {
            //log.info("Valid: $errs");
        }
        return errs;
    }


    private static final Pattern idPatt = Pattern.compile("([iI][dD])\\s*=(.*)");
    private static final Pattern xPatt = Pattern.compile("([xX])\\s*=(.*)");
    private static final Pattern yPatt = Pattern.compile("([yY])\\s*=(.*)");

    public static DoseResponseResult toDoseResponseResult(String input) {

        String[] lines = input.split("\r?\n");
        DoseResponseResult drr = new DoseResponseResult();
        for (String line : lines) {
            Matcher m = xPatt.matcher(line);
            if (m.matches()) {
                List<Double> vals = parseValues(m.group(2));
                //log.fine("Setting X values: $xList")
                drr.setXValues(vals);
                continue;
            }
            m = yPatt.matcher(line);
            if (m.matches()) {
                List<Double> vals = parseValues(m.group(2));
                //log.fine("Setting X values: $xList")
                drr.getYValues().add(vals);
                continue;
            }

            m = idPatt.matcher(line);
            if (m.matches()) {
                drr.setId(m.group(2).trim());
                continue;
            }

        }

        return drr;
    }

    public static String fromDoseResponseDataset(DoseResponseDataset input) {
        StringBuilder builder = new StringBuilder();
        for (DoseResponseResult result : input.getResults()) {
            builder.append(fromDoseResponseResult(result)).append("#END\n");
        }
        return builder.toString();
    }

    public static String fromDoseResponseResult(DoseResponseResult input) {
        StringBuilder builder = new StringBuilder();
        if (input.getId() != null) {
            builder.append("id = ").append(input.getId()).append("\n");
        }
        if (input.getXValues() != null) {
            builder.append("x = ");
            int count = 0;
            for (Double d : input.getXValues()) {
                if (count > 0) {
                    builder.append(" ");
                }
                builder.append(d);
                count++;
            }
            builder.append("\n");
        }
        if (input.getYValues() != null) {
            for (List<Double> l : input.getYValues()) {
                if (l != null) {
                    builder.append("y = ");
                    int count = 0;
                    for (Double d : l) {
                        if (count > 0) {
                            builder.append(" ");
                        }
                        builder.append(d);
                        count++;
                    }
                    builder.append("\n");
                }
            }
        }
        FourPLModel ic50 = input.getFitModel();
        if (ic50 != null) {
            builder.append("inflection = ").append(ic50.getInflection()).append("\n");
            builder.append("mod = ").append(ic50.getModifier()).append("\n");
            builder.append("slope = ").append(ic50.getSlope()).append("\n");
            builder.append("bottom = ").append(ic50.getBottom()).append("\n");
            builder.append("top = ").append(ic50.getTop()).append("\n");
            builder.append("ss = ").append(ic50.getSumSquares()).append("\n");
        }

        return builder.toString();
    }

    static private List<Double> lineMatches(Pattern patt, String line) {
        Matcher m = patt.matcher(line);
        if (m.matches()) {
            String param = m.group(1);
            String values = m.group(2);
            return parseValues(values);
        } else {
            return null;
        }
    }

    static private List<Double> parseValues(String vals) {
        String[] toks = vals.trim().split("\\s+");
        List<Double> list = new ArrayList<Double>();
        for (String tok : toks) {
            String s = tok.trim();
            Double d = null;
            if (!s.isEmpty()) {
                d = new Double(s);
            }
            list.add(d);
        }
        return list;
    }
}
