package uj.wmii.pwj.w7.insurance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

record InsuranceEntry(String county, double tiv2011, double tiv2012) { }

public class FloridaInsurance {

    private static List<InsuranceEntry> IElist;

    public static void main(String[] args) {
        try {
            IElist = loadEntriesFromZip("FL_insurance.csv.zip", "FL_insurance.csv");
            generateCount();
            generateTiv2012();
            generateMostValuable();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static List<InsuranceEntry> loadEntriesFromZip(String zipPath, String csvName) throws IOException {
        List<InsuranceEntry> list = new ArrayList<>();
        try (ZipFile zip = new ZipFile(zipPath)) {
            ZipEntry entry = zip.getEntry(csvName);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8))) {
                Iterator<String> it = br.lines().iterator();
                if (it.hasNext()) it.next();
                while (it.hasNext()) {
                    String line = it.next();
                    String[] parts = line.split(",");
                    String county = parts[2];
                    double tiv2011 = Double.parseDouble(parts[7]);
                    double tiv2012 = Double.parseDouble(parts[8]);
                    list.add(new InsuranceEntry(county, tiv2011, tiv2012));
                }
            }
        }
        return list;
    }

    public static void generateCount() throws IOException {
        long cnt = IElist.stream().map(InsuranceEntry::county).distinct().count();
        Files.writeString(Paths.get("count.txt"), Long.toString(cnt), StandardCharsets.UTF_8);
    }

    public static void generateTiv2012() throws IOException {
        double sum = IElist.stream().mapToDouble(InsuranceEntry::tiv2012).sum();
        Files.writeString(Paths.get("tiv2012.txt"), String.format(Locale.US, "%.2f", sum), StandardCharsets.UTF_8);
    }

    public static void generateMostValuable() throws IOException {
        String content = IElist.stream()
                .collect(Collectors.groupingBy(
                        InsuranceEntry::county,
                        Collectors.summingDouble(e -> e.tiv2012() - e.tiv2011())
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> String.format(Locale.US, "%s,%.2f", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"));

        Files.writeString(Paths.get("most_valuable.txt"), "country,value\n" + content, StandardCharsets.UTF_8);
    }
}
