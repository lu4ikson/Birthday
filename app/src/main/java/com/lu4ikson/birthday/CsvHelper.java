package com.lu4ikson.birthday;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvHelper {

    private static final String HEADER = "name,day,month,year,notifyDayBefore,notifyHour,notifyMinute";

    public static void exportToStream(OutputStream outputStream, List<Person> people) throws IOException {
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(HEADER);
        writer.write("\n");
        for (Person p : people) {
            writer.write(escapeCsv(p.name));
            writer.write(",");
            writer.write(String.valueOf(p.day));
            writer.write(",");
            writer.write(String.valueOf(p.month));
            writer.write(",");
            writer.write(p.year != null ? String.valueOf(p.year) : "");
            writer.write(",");
            writer.write(String.valueOf(p.notifyDayBefore));
            writer.write(",");
            writer.write(p.notifyHour != null ? String.valueOf(p.notifyHour) : "");
            writer.write(",");
            writer.write(p.notifyMinute != null ? String.valueOf(p.notifyMinute) : "");
            writer.write("\n");
        }
        writer.flush();
    }

    public static List<Person> importFromStream(InputStream inputStream) throws IOException {
        List<Person> result = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        reader.readLine(); // пропускаем строку заголовка
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            Person person = parseLine(line);
            if (person != null) {
                result.add(person);
            }
            // некорректные строки просто пропускаем, не роняя весь импорт
        }
        return result;
    }

    private static Person parseLine(String line) {
        List<String> fields = splitCsvLine(line);
        if (fields.size() < 7) return null;
        try {
            String name = fields.get(0).trim();
            if (name.isEmpty()) return null;

            int day = Integer.parseInt(fields.get(1).trim());
            int month = Integer.parseInt(fields.get(2).trim());

            String yearStr = fields.get(3).trim();
            Integer year = yearStr.isEmpty() ? null : Integer.parseInt(yearStr);

            boolean notifyDayBefore = Boolean.parseBoolean(fields.get(4).trim());

            String hourStr = fields.get(5).trim();
            Integer notifyHour = hourStr.isEmpty() ? null : Integer.parseInt(hourStr);

            String minuteStr = fields.get(6).trim();
            Integer notifyMinute = minuteStr.isEmpty() ? null : Integer.parseInt(minuteStr);

            Person person = new Person(name, day, month, year);
            person.notifyDayBefore = notifyDayBefore;
            person.notifyHour = notifyHour;
            person.notifyMinute = notifyMinute;
            return person;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Разбивает строку CSV с учётом кавычек — на случай если имя содержит запятую
    private static List<String> splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result;
    }

    private static String escapeCsv(String value) {
        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}