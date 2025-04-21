# 📊 Household Data Analyzer (Program 3)

A Java program that analyzes U.S. household data for average income, poverty status, and Medicaid eligibility using both static data and live REST API calls from the U.S. Department of Health and Human Services.

---

## 🧠 Features

- Reads household data from a text file.
- Calculates:
  - Average household income
  - List of households above the average income
  - Households below the Federal Poverty Level (FPL)
  - Percentage of households below FPL
  - Percentage of households eligible for Medicaid (based on 138% of FPL)
- Uses **real-time data** via API calls to determine poverty levels by household size and region (Contiguous U.S., Alaska, Hawaii).

---

## 📂 Project Structure

| File | Description |
|------|-------------|
| `Program3.java` | Main driver program. Handles overall flow, file I/O, and output generation. |
| `Household.java` | Defines the `Household` object with ID, income, members, and state. |
| `HouseholdProcessor.java` | Loads data, performs statistical computations, and calls FPL APIs. |
| `Program3Data.txt` | Input file containing household records (ID, income, members, state). |
| `Program3-Output.txt` | Output file automatically generated with all results. |

---

## 🗃️ Sample Input (`Program3Data.txt`)

1000  31000     3 Texas
1042  12180.06  3 Texas
1062  13240.45  2 Texas
1200  36000     2 Maryland
1327  19800.56  2 Alaska
1483  22458.23  7 Texas
1900  17000.09  3 Washington
2000  16910.00  2 Vermont

ID — A unique household identifier

Income — Annual household income

Members — Number of people in the household

State — U.S. state where the household resides
