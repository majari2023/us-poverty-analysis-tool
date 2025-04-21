//********************************************************************
//
//  Developer:     Mauricio Rivas
//
//  Program #:     Three
//
//  File Name:     Household.java
//
//  Course:        COSC 4301 Modern Programming
//
//  Due Date:      4/14/2025
//
//  Instructor:    Prof. Fred Kumi 
//
//  Java Version:  11
//
//  Description:   This class defines a Household object that stores
//                 the household's ID, income, number of members,
//                 and the state where the household resides.
//********************************************************************

public class Household {
    private int id;
    private double income;
    private int members;
    private String state;
    
    //***************************************************************
    //
    //  Method:       Household (Constructor)
    // 
    //  Description:  Initializes the Household object with values
    //                for ID, income, members, and state.
    //
    //  Parameters:   int id         - Household ID
    //                double income  - Annual income
    //                int members    - Number of household members
    //                String state   - State of residence
    //
    //  Returns:      None
    //
    //***************************************************************
    
    public Household(int id, double income, int members, String state) {
        this.id = id;
        this.income = income;
        this.members = members;
        this.state = state;
    }
    //***************************************************************
    //
    //  Method:       getId
    // 
    //  Description:  Returns the household ID.
    //
    //  Parameters:   None
    //
    //  Returns:      int - Household ID
    //
    //***************************************************************
    public int getId() {
        return id;
    }
    //***************************************************************
    //
    //  Method:       getIncome
    // 
    //  Description:  Returns the household's income.
    //
    //  Parameters:   None
    //
    //  Returns:      double - Income amount
    //
    //***************************************************************
    public double getIncome() {
        return income;
    }
    //***************************************************************
    //
    //  Method:       getMembers
    // 
    //  Description:  Returns the number of people in the household.
    //
    //  Parameters:   None
    //
    //  Returns:      int - Number of household members
    //
    //***************************************************************
    public int getMembers() {
        return members;
    }
    //***************************************************************
    //
    //  Method:       getState
    // 
    //  Description:  Returns the state of residence of the household.
    //
    //  Parameters:   None
    //
    //  Returns:      String - State name
    //
    //***************************************************************
    public String getState() {
        return state;
    }
    //***************************************************************
    //
    //  Method:       toString
    // 
    //  Description:  Returns a formatted string with household data.
    //
    //  Parameters:   None
    //
    //  Returns:      String - Formatted household information
    //
    //***************************************************************
    @Override
    public String toString() {
        return String.format("%-6d %-12.2f %-8d %s", id, income, members, state);
    }
}