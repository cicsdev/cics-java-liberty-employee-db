/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2017 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.employee.jdbc.faces;

import java.util.ArrayList;

import javax.sql.DataSource;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;
import com.ibm.cicsdev.employee.jdbc.impl.DbOperations;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * Bean used to implement the function of the master.xhtml page.

 * 
 * This bean provides all of the required methods for populating the
 * page, as well as controlling the edit/delete buttons and calling
 * into the database methods class.
 * 
 * @author Michael Jones
 *
 */
@ManagedBean(name = "employeeList")
@SessionScoped
public class EmpListBean
{
    /**
     * Stores current target employee for an update or delete operation.
     * FIXME Funny behaviour when you select delete/edit for more than one record.
     */
    private Employee employee;
    
    /**
     * Object for our Liberty data source.
     */
    private DataSource ds;
    
    /**
     * Stores the last value used as the search criteria.
     */
    private String searchString;
    
    /**
     * Stores the results of any search.
     * Used by JSF to display the results in a table.
     */
    private ArrayList<Employee> allResults = new ArrayList<>();
    
    /**
     * Used to indicate the index of the first result displayed.
     */
    private int firstRow = 0;
    
    /**
     * Used to limit the number of rows displayed by the application.
     */
    private int lastRow = 15;
    
    /**
     * Current error message for display.
     */
    private String message = "";
    
    /**
     * Flag to indicate we will use JTA for unit of work support.
     */
    private boolean jta = true;
    
    /**
     * Flag to indicate the connection to the database is available.
     */
    private boolean databaseAvailable = false;

    
    /*
     * Constructor.
     */
    
    /**
     * No args constructor for this bean. JSF will call it when
     * the page is first loaded. 
     * 
     * This constructor will attempt to create a connection to
     * the database. If it can't it will show a message and hide
     * the command buttons.
     */
    public EmpListBean() {
        
        try {
            // Attempt to lookup the configured DataSource instance
            this.ds = (DataSource) InitialContext.doLookup("jdbc/sample");
            this.databaseAvailable = true;
        }
        catch (NamingException e) {
            // Flag the error and write out to the log
            message = "NO DATASOURCE CONNECTION";
            e.printStackTrace();
        }
    }

    
    /*
     * Action methods.
     */
    
    /**
     * Navigates the user to the correct page to add an employee.
     * 
     * @return the name of the JSF file for rendering next.
     */
    public String goToAddScreen() {
        return "addEmp.xhtml";
    }
    
    /** 
     * Toggles the state of the JTA flag.
     */
    public void toggleJta() {
        this.jta = ! this.jta;
    }
    
    /**
     * Updates the canEdit flag for a row. When this flag
     * is set to true, the fields become input fields rather
     * than just text boxes
     */
    public void setCanEdit() {
        System.out.println(String.format("setCanEdit on employee %#08X", employee == null ? 0 : employee.hashCode()));
        employee.setCanEdit(true);
    }
    
    /**
     * Saves any edits made by the user in the form after the edit button
     * has been clicked.
     * 
     * It will run the update function using the new values, updating the
     * record in the database. Will also clear the canEdit flag for the
     * current record.
     */
    public void saveUpdates() {
        
        System.out.println(String.format("Save updates on employee %#08X", employee == null ? 0 : employee.hashCode()));
        
        try {
            // Call our utility routine to update the database
            DbOperations.updateEmployee(ds, employee, jta);
        }
        catch (Exception e) {
            message = "ERROR: Please check stderr.";
            e.printStackTrace();
        }

        // Clear the flag that says we can edit this row
        employee.setCanEdit(false);
    }
    
    
    
    /** 
     * Performs the actual search function. This will be called by
     * JSF when the user presses the search button.
     * 
     * @return - The empList.xhtml page, with results included
     */
    public String search() {
        
        try {
            // Search the database for this string
            allResults = DbOperations.findEmployeeByLastName(ds, searchString);
            
            // Message if no results are found
            if (allResults.size() < 1) {
                message = "NO RESULTS FOUND.";
            }
            else {
                message = "";
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            message = "ERROR: Please see stderr";
        }
        
        // Redirect back to main page
        return "master.xhtml";
    }
    
    /**
     * Allows JSF to check the canDel flag on an employee.
     * 
     * This flag enables or disabled the delete function.
     */
    public void confirmDel() {
        employee.setCanDelete(true);
    }
    
    /**
     * Provides the action for the confirm deletion button on master.xhtml.
     * 
     * Will take the employee it's been selected against and use it as the
     * basis for the delete command
     * 
     * It will try to detect the error that informs the user they don't have
     * the permission to delete the record. If that occurs, it'll update the
     * status message.
     * 
     * @return - Calls the search function, refreshing the rows. Removing the record
     */
    public String deleteEmployee() {
        
        try {
            // Call the delete function for this employee
            DbOperations.deleteEmployee(ds, employee, jta);
        }
        catch (Exception e) {
        
            // Check for the delete permissions error
            if ( e.getMessage().contains("RESTRICTS THE DELETION") ) {
                // Not allowed to delete the record
                message = "ERROR: You cannot delete this record.";
            }
            else {
                // If we can't find the permission error, report the problem
                message = "ERROR: See stdout for details";
                e.printStackTrace();
            }

            // Clear the flag
            employee.setCanDelete(false);

            // Redirect back to main page
            return "master.xhtml";
        }    
        
        // Successful: call the search function, refreshing the view
        return search();
    }
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public String getSearchString() {
        return searchString;
    }
    
    public void setSearchString(String ss) {
        this.searchString = ss;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getFirstRow() {
        return firstRow;
    }
    
    public int getLastRow() {
        return lastRow;
    }
    
    public ArrayList<Employee> getallResults() {
        return allResults;
    }
    
    public void setAllResults(ArrayList<Employee> allResults) {
        this.allResults = allResults;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee emp) {
        System.out.println(String.format("Set to employee %#08X (%s)", emp == null ? 0 : emp.hashCode(), emp == null ? "null" : emp.getLastName()));
        System.out.println(String.format("   Was employee %#08X (%s)", employee == null ? 0 : employee.hashCode(), employee == null ? "null" : employee.getLastName()));        
        this.employee = emp;
    }
    
    public boolean isDatabaseAvailable() {
        return databaseAvailable;
    }
    
    public boolean isJta() {
        return jta;
    }    
}
