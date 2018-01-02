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
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.sql.DataSource;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;
import com.ibm.cicsdev.employee.jdbc.impl.DbOperations;

/**
 * Bean used to implement the function of the main view page.
 * 
 * This bean provides all of the required methods for populating the
 * page, as well as controlling the edit/delete buttons and calling
 * into the database methods class.
 * 
 * @author Michael Jones
 */

@ManagedBean(name = "employeeList")
@SessionScoped
public class EmpListBean
{
    /*
     * Instance fields.
     */    
	
    /**
     * The JNDI name used to lookup the JDBC DataSource instance.
     */
    public static final String DATABASE_JNDI = "jdbc/sample";
	
    
   /**
     * Stores current target employee for an update or delete operation.
     */
    private Employee employee;
    
    /**
     * DataSource instance for connecting to the database using JDBC
     * Use of Resource injection required for container mgd security
     */          
    @Resource(authenticationType=AuthenticationType.CONTAINER,name=DATABASE_JNDI)
    private DataSource ds;
    
    /**
     * Stores the last value used as the search criteria.
     */
    private String searchString;
    
    /**
     * Stores the results of any search.
     */
    private List<Employee> allResults = new ArrayList<>();
    
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
    private boolean useJta = true;
    
    
    /*
     * Constructor.
     */
    
    /**
     * No args constructor for this bean - JSF will call it when the page is first loaded. 
     *
     */ 

    public EmpListBean() {
    } 

    
    /*
     * Action methods.
     */
    
    /**
     * Called by JSF when the user clicks the "Add new employee" button.
     * 
     * Navigates the user to the correct page to add an employee.
     * 
     * @return the name of the JSF file for rendering next.
     */
    public String goToAddScreen() {
        return "addEmp.xhtml";
    }
    
    /** 
     * Called by JSF when the user clicks the "Toggle JTA" button.
     * 
     * Toggles the state of the JTA flag.
     */
    public void toggleUseJta() {
        useJta = ! useJta;
    }
    
    /**
     * Called by JSF when the user clicks the "Edit" button for a row. 
     * 
     * Updates the editable flag for a row. When this flag is set to true, the
     * fields become input fields rather than just text boxes.
     * 
     * @see Employee#setCanEdit(boolean)
     */
    public void setCanEdit() {
        employee.setCanEdit(true);
    }
    
    /**
     * Called by JSF when the user clicks the "Save" button for a row.
     * 
     * This method will run the update function using the new values, updating the record
     * in the database. Will also clear the editable flag for the current record.
     * 
     * @see DbOperations#updateEmployee(DataSource, Employee, boolean)
     */
    public void saveUpdates() {
        
        try {
            // Call our utility routine to update the database
            DbOperations.updateEmployee(ds, employee, useJta);
        }
        catch (Exception e) {
            message = "ERROR: Please check stderr.";
            e.printStackTrace();
        }

        // Clear the flag that says we can edit this row
        employee.setCanEdit(false);
    }
    
    /** 
     * Called by JSF when the user presses the Search button.
     * 
     * This method will call the database search method, passing the supplied search string.
     * 
     * @return The name of the page to navigate to, which will contain the results.
     * 
     * @see DbOperations#findEmployeeByLastName(DataSource, String)
     */
    public String search() {
        
        try {
            // Search the database for this string
            allResults = DbOperations.findEmployeeByLastName(ds, searchString);
            
            // Message if no results are found
            if ( allResults.size() < 1 ) {
                message = "NO RESULTS FOUND.";
            }
            else {
                message = "";
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            message = "ERROR: Please see stderr";
        }
        
        // Redirect back to main page
        return "master.xhtml";
    }
    
    /**
     * Called by JSF when the user clicks the "Delete" button for a record.
     * 
     * This method will update the deletable flag for the record in question,
     * which will have the effect of showing the "Confirm" button.
     */
    public void confirmDel() {
        employee.setCanDelete(true);
    }
    
    /**
     * Called by JSF when the user clicks the "Confirm" button for a record.
     * 
     * Will take the relevant employee and use it as the basis for the delete command.
     * It will try to detect the error that informs the user they don't have
     * the permission to delete the record. If that occurs, it will update the
     * status message.
     * 
     * @return The next page to display in the user interaction
     * 
     * @see DbOperations#deleteEmployee(DataSource, Employee, boolean)
     */
    public String deleteEmployee() {
        
        try {
            // Call the delete function for this employee
            DbOperations.deleteEmployee(ds, employee, useJta);
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
    
    public List<Employee> getallResults() {
        return allResults;
    }
    
    public void setAllResults(ArrayList<Employee> allResults) {
        this.allResults = allResults;
    }
    
    public Employee getEmployee() {
        return employee;
    }
    
    public void setEmployee(Employee emp) {
        this.employee = emp;
    }  

    
    public boolean getUseJta() {
        return useJta;
    }    
}
