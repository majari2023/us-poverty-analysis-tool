//********************************************************************
//
//  Developer:     Mauricio Rivas
//
//  Program #:     Three
//
//  File Name:     Program3.java
//
//  Course:        COSC 4301 Modern Programming
//
//  Due Date:      4/14/2025
//
//  Instructor:    Prof. Fred Kumi
//
//  Java Version:  11
//
//  Description:   This test class manages the execution of household
//                 data analysis including average income, poverty level
//                 checks, and Medicaid eligibility, using REST API calls.
//
//********************************************************************

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.*;
import java.util.*;

public class Program3 {
    private ArrayList<Household> households = new ArrayList<>();
    private double contigBase, contigPer;
    private double alaskaBase, alaskaPer;
    private double hawaiiBase, hawaiiPer;
    private PrintWriter writer;
    //***************************************************************
    //
    //  Method:       main
    // 
    //  Description:  Entry point of the program. Calls run().
    //
    //  Parameters:   String[] args - command-line arguments
    //
    //  Returns:      void
    //
    //***************************************************************
    public static void main(String[] args) {
        Program3 program = new Program3();
        program.run();
    }
    //***************************************************************
    //
    //  Method:       run
    // 
    //  Description:  Controls the execution flow and delegates tasks.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void run() {
        try {
            writer = new PrintWriter("Program3-Output.txt");

            readHouseholds("Program3Data.txt");
            printHouseholds();
            printAverageIncome();
            printAboveAverage();
            printBelowFPL();
            printPercentBelowFPL();
            fetchFPLFormulas();
            printPercentEligibleMedicaid();

            writer.close();
            System.out.println("Output successfully written to Program3-Output.txt");

        } catch (FileNotFoundException e) {
            System.out.println("Error: Unable to write to Program3-Output.txt");
        }
    }
    //***************************************************************
    //
    //  Method:       readHouseholds
    // 
    //  Description:  Loads household data from file into ArrayList.
    //
    //  Parameters:   String fileName - file containing household data
    //
    //  Returns:      void
    //
    //***************************************************************
    public void readHouseholds(String fileName) {
        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextLine()) {
                String[] parts = scanner.nextLine().trim().split("\\s+");
                int id = Integer.parseInt(parts[0]);
                double income = Double.parseDouble(parts[1]);
                int members = Integer.parseInt(parts[2]);
                StringBuilder state = new StringBuilder();
                for (int i = 3; i < parts.length; i++) {
                    state.append(parts[i]);
                    if (i < parts.length - 1) state.append(" ");
                }
                households.add(new Household(id, income, members, state.toString()));
            }
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
    //***************************************************************
    //
    //  Method:       printHouseholds
    // 
    //  Description:  Outputs the list of households with their data.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printHouseholds() {
        writer.printf("%-6s %-12s %-8s %s%n", "ID", "Income", "Members", "State");
        writer.println("-------------------------------------------------------");
        for (Household h : households) {
            writer.println(h);
        }
    }
    //***************************************************************
    //
    //  Method:       printAverageIncome
    // 
    //  Description:  Calculates and prints the average income.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printAverageIncome() {
        double total = 0;
        for (Household h : households) {
            total += h.getIncome();
        }
        double average = total / households.size();
        writer.printf("%nAverage Household Income: $%.2f%n", average);
    }
    //***************************************************************
    //
    //  Method:       printAboveAverage
    // 
    //  Description:  Prints households with income above the average.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printAboveAverage() {
        double total = 0;
        for (Household h : households) {
            total += h.getIncome();
        }
        double average = total / households.size();

        writer.printf("%nHouseholds with Income Above Average ($%.2f):%n", average);
        writer.printf("%-6s %-12s %-8s %s%n", "ID", "Income", "Members", "State");
        writer.println("-------------------------------------------------------");

        for (Household h : households) {
            if (h.getIncome() > average) {
                writer.println(h);
            }
        }
    }
    //***************************************************************
    //
    //  Method:       printBelowFPL
    // 
    //  Description:  Prints households below the poverty line.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printBelowFPL() {
        Map<Integer, Double> fplMap = fetchFPLBySize("us");

        writer.printf("%nHouseholds Below 2025 Poverty Level:%n");
        writer.printf("%-6s %-12s %-14s %-8s %s%n", "ID", "Income", "FPL", "Members", "State");
        writer.println("-------------------------------------------------------------------");

        for (Household h : households) {
            double fpl = fplMap.getOrDefault(h.getMembers(), Double.MAX_VALUE);
            if (h.getIncome() < fpl) {
                writer.printf("%-6d %-12.2f %-14.2f %-8d %s%n",
                        h.getId(), h.getIncome(), fpl, h.getMembers(), h.getState());
            }
        }
    }
    //***************************************************************
    //
    //  Method:       printPercentBelowFPL
    // 
    //  Description:  Calculates percentage of households below FPL.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printPercentBelowFPL() {
        Map<Integer, Double> fplMap = fetchFPLBySize("us");
        int below = 0;

        for (Household h : households) {
            double fpl = fplMap.getOrDefault(h.getMembers(), Double.MAX_VALUE);
            if (h.getIncome() < fpl) below++;
        }

        double percent = (below * 100.0) / households.size();
        writer.printf("%nPercentage of households below FPL: %.2f%%%n", percent);
    }
    //***************************************************************
    //
    //  Method:       printPercentEligibleMedicaid
    // 
    //  Description:  Calculates percentage eligible under 138% FPL.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    public void printPercentEligibleMedicaid() {
        int eligible = 0;
        for (Household h : households) {
            double base, per;
            String state = h.getState().toLowerCase();
            if (state.equals("alaska")) {
                base = alaskaBase;
                per = alaskaPer;
            } else if (state.equals("hawaii")) {
                base = hawaiiBase;
                per = hawaiiPer;
            } else {
                base = contigBase;
                per = contigPer;
            }

            double fpl = base + per * (h.getMembers() - 2);
            double threshold = fpl * 1.38;

            if (h.getIncome() < threshold) {
                eligible++;
            }
        }

        double percent = (eligible * 100.0) / households.size();
        writer.printf("%nPercentage of households eligible for Medicaid: %.2f%%%n", percent);
    }
    //***************************************************************
    //
    //  Method:       fetchFPLBySize
    // 
    //  Description:  Queries the API for multiple household sizes.
    //
    //  Parameters:   String regionCode - region identifier (us/ak/hi)
    //
    //  Returns:      Map<Integer, Double> - mapping of size to income
    //
    //***************************************************************
    private Map<Integer, Double> fetchFPLBySize(String regionCode) {
        Map<Integer, Double> map = new HashMap<>();
        HttpClient client = HttpClient.newHttpClient();

        for (int size = 1; size <= 12; size++) {
            try {
                String uri = "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines/api/2025/" + regionCode + "/" + size;
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                String body = response.body();
                double income = extractIncomeFromJson(body);
                map.put(size, income);
            } catch (Exception e) {
                System.out.println("API Error for size " + size);
            }
        }
        return map;
    }
    //***************************************************************
    //
    //  Method:       fetchFPLFormulas
    // 
    //  Description:  Retrieves FPL base and rate for each region.
    //
    //  Parameters:   None
    //
    //  Returns:      void
    //
    //***************************************************************
    private void fetchFPLFormulas() {
        contigBase = getFPL("us", 2);
        contigPer = (getFPL("us", 4) - contigBase) / 2;

        alaskaBase = getFPL("ak", 2);
        alaskaPer = (getFPL("ak", 4) - alaskaBase) / 2;

        hawaiiBase = getFPL("hi", 2);
        hawaiiPer = (getFPL("hi", 4) - hawaiiBase) / 2;
    }
    //***************************************************************
    //
    //  Method:       getFPL
    // 
    //  Description:  Retrieves FPL income for a single household size.
    //
    //  Parameters:   String region, int size
    //
    //  Returns:      double - FPL income
    //
    //***************************************************************
    private double getFPL(String region, int size) {
        try {
            String uri = "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines/api/2025/" + region + "/" + size;
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(uri)).GET().build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            return extractIncomeFromJson(response.body());
        } catch (Exception e) {
            System.out.println("Error fetching FPL for " + region + " size " + size);
            return 0;
        }
    }
    //***************************************************************
    //
    //  Method:       extractIncomeFromJson
    // 
    //  Description:  Parses a JSON string to extract income value.
    //
    //  Parameters:   String json - JSON response body
    //
    //  Returns:      double - extracted income value
    //
    //***************************************************************
    private double extractIncomeFromJson(String json) {
        int index = json.indexOf("\"income\":");
        if (index == -1) return 0;
        int start = index + 9;
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        String value = json.substring(start, end).replaceAll("[^0-9.]", "").trim();
        return Double.parseDouble(value);
    }
}
