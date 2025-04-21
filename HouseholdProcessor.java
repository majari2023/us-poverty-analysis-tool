//********************************************************************
//
//  Developer:     Mauricio Rivas
//
//  Program #:     Three
//
//  File Name:     HouseholdProcessor.java
//
//  Course:        COSC 4301 Modern Programming
//
//  Due Date:      4/14/2025
//
//  Instructor:    Prof. Fred Kumi 
//
//  Java Version:  11
//
//  Description:   This class handles data loading, analysis, and output
//                 for household records, including calculations for
//                 average income, poverty levels, and Medicaid eligibility.
//
//********************************************************************
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class HouseholdProcessor {
    private ArrayList<Household> households = new ArrayList<>();
    //***************************************************************
    //
    //  Method:       loadData
    // 
    //  Description:  Loads household data from a file into the list.
    //
    //  Parameters:   String filename - Name of the input file
    //
    //  Returns:      None
    //
    //***************************************************************
    public void loadData(String filename) {
        try (Scanner scanner = new Scanner(new File(filename))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    int id = Integer.parseInt(parts[0]);
                    double income = Double.parseDouble(parts[1]);
                    int members = Integer.parseInt(parts[2]);

                    StringBuilder stateBuilder = new StringBuilder();
                    for (int i = 3; i < parts.length; i++) {
                        stateBuilder.append(parts[i]);
                        if (i < parts.length - 1) {
                            stateBuilder.append(" ");
                        }
                    }

                    String state = stateBuilder.toString();
                    households.add(new Household(id, income, members, state));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error: File not found.");
        }
    }
    //***************************************************************
    //
    //  Method:       displayHouseholds
    // 
    //  Description:  Displays all household records in a tabular format.
    //
    //  Parameters:   None
    //
    //  Returns:      None
    //
    //***************************************************************
    public void displayHouseholds() {
        System.out.printf("%-6s %-12s %-8s %s%n", "ID", "Income", "Members", "State");
        System.out.println("-------------------------------------------------------");
        for (Household h : households) {
            System.out.println(h);
        }
    }
    //***************************************************************
    //
    //  Method:       displayAverageIncome
    // 
    //  Description:  Calculates and displays the average income.
    //
    //  Parameters:   None
    //
    //  Returns:      None
    //
    //***************************************************************
    public void displayAverageIncome() {
        if (!households.isEmpty()) {
            double totalIncome = 0;
            for (Household h : households) {
                totalIncome += h.getIncome();
            }
            double average = totalIncome / households.size();
            System.out.printf("%nAverage Household Income: $%.2f%n", average);
        }
    }
    //***************************************************************
    //
    //  Method:       displayAboveAverageHouseholds
    // 
    //  Description:  Displays households with income above the average.
    //
    //  Parameters:   None
    //
    //  Returns:      None
    //
    //***************************************************************
    public void displayAboveAverageHouseholds() {
        if (!households.isEmpty()) {
            double totalIncome = 0;
            for (Household h : households) {
                totalIncome += h.getIncome();
            }
            double average = totalIncome / households.size();

            System.out.printf("%nHouseholds with Income Exceeding the Average ($%.2f):%n", average);
            System.out.printf("%-6s %-12s %-8s %s%n", "ID", "Income", "Members", "State");
            System.out.println("-------------------------------------------------------");

            for (Household h : households) {
                if (h.getIncome() > average) {
                    System.out.println(h);
                }
            }
        }
    }
    //***************************************************************
    //
    //  Method:       displayHouseholdsBelowPovertyLevel
    // 
    //  Description:  Displays households that fall below the 2025
    //                poverty level using API data.
    //
    //  Parameters:   None
    //
    //  Returns:      None
    //
    //***************************************************************
    public void displayHouseholdsBelowPovertyLevel() {
        if (!households.isEmpty()) {
            Map<Integer, Double> povertyLevels = new HashMap<>();
            HttpClient client = HttpClient.newHttpClient();

            for (Household h : households) {
                int size = h.getMembers();
                if (!povertyLevels.containsKey(size)) {
                    try {
                        String uri = "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines/api/2025/us/" + size;
                        HttpRequest request = HttpRequest.newBuilder()
                                .uri(URI.create(uri))
                                .GET()
                                .build();

                        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                        if (response.statusCode() == 200) {
                            String body = response.body();
                            double level = extractPovertyLevelFromResponse(body);
                            povertyLevels.put(size, level);
                        } else {
                            System.out.println("Failed to fetch poverty level for size: " + size);
                        }

                    } catch (Exception e) {
                        System.out.println("Error fetching poverty level for household size: " + size);
                        e.printStackTrace();
                    }
                }
            }

            System.out.printf("%nHouseholds Below 2025 Poverty Level:%n");
            System.out.printf("%-6s %-12s %-14s %-8s %s%n", "ID", "Income", "Poverty Level", "Members", "State");
            System.out.println("-------------------------------------------------------------------");

            for (Household h : households) {
                int size = h.getMembers();
                double poverty = povertyLevels.getOrDefault(size, Double.MAX_VALUE);
                if (h.getIncome() < poverty) {
                    System.out.printf("%-6d %-12.2f %-14.2f %-8d %s%n",
                            h.getId(), h.getIncome(), poverty, h.getMembers(), h.getState());
                }
            }
        }
    }
    //***************************************************************
    //
    //  Method:       extractPovertyLevelFromResponse
    // 
    //  Description:  Extracts the income value from a JSON response.
    //
    //  Parameters:   String json - JSON string from API
    //
    //  Returns:      double - extracted income value
    //
    //***************************************************************
    private double extractPovertyLevelFromResponse(String json) {
        String key = "\"income\":";
        int index = json.indexOf(key);
        if (index == -1) return 0.0;

        int start = index + key.length();
        int end = json.indexOf(",", start);
        if (end == -1) {
            end = json.indexOf("}", start); // fallback: income is last item
        }

        if (end == -1) {
            return 0.0; // no valid end found
        }

        String value = json.substring(start, end).replaceAll("[^0-9.]", "").trim();
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            System.out.println("Error parsing poverty income value: " + value);
            return 0.0;
        }
    }
    //***************************************************************
    //
    //  Method:       displayPercentageBelowPovertyLevel
    // 
    //  Description:  Calculates and prints the percentage of households
    //                below the federal poverty level.
    //
    //  Parameters:   None
    //
    //  Returns:      None
    //
    //***************************************************************
    public void displayPercentageBelowPovertyLevel() {
        if (households.isEmpty()) {
            System.out.println("No household data available.");
            return;
        }

        Map<Integer, Double> povertyLevels = new HashMap<>();
        HttpClient client = HttpClient.newHttpClient();

        for (Household h : households) {
            int size = h.getMembers();
            if (!povertyLevels.containsKey(size)) {
                try {
                    String uri = "https://aspe.hhs.gov/topics/poverty-economic-mobility/poverty-guidelines/api/2025/us/" + size;
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(uri))
                            .GET()
                            .build();

                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    if (response.statusCode() == 200) {
                        String body = response.body();
                        double level = extractPovertyLevelFromResponse(body);
                        povertyLevels.put(size, level);
                    }
                } catch (Exception e) {
                    System.out.println("Error retrieving poverty level for size " + size);
                    e.printStackTrace();
                }
            }
        }

        int countBelow = 0;
        for (Household h : households) {
            double povertyLevel = povertyLevels.getOrDefault(h.getMembers(), Double.MAX_VALUE);
            if (h.getIncome() < povertyLevel) {
                countBelow++;
            }
        }

        double percentage = ((double) countBelow / households.size()) * 100;
        System.out.printf("%nPercentage of households below the 2025 Federal Poverty Level: %.2f%%%n", percentage);
    }
}
