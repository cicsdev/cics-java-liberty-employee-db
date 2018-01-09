/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2018 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.employee.jdbc.faces;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;

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
public class EmployeeListManager
{
    /*
     * Instance fields.
     */    

    /**
     * Stores current target employee for an update or delete operation.
     */
    private Employee employee;

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
    
    /**
     * Field used to access the DB manipulation methods.
     */
    private DatabaseOperationsManager dbOperations;

    
    /*
     * Constructor.
     */
    
    /**
     * No args constructor for this bean - JSF will call it when the page is first loaded. 
     *
     */ 

    public EmployeeListManager() {
        
    } 

    
    /*
     * Lifecycle methods.
     */
    
    /**
     * Performs initialisation of the bean after the constructor has been called.
     * 
     * This method finds the application-scoped instance of the {@link DatabaseOperationsManager}
     * bean in here, rather than using injection. We do that this way to allow us to trap
     * any problems with instantiating the class in a try ... catch block. This enables the
     * ability to provide a "DB down" message on the main page, without a huge amount of
     * error-handling logic at the backend.
     */
    @PostConstruct
    public void init() {
        
        // Get the current faces & application context
        FacesContext ctxt = FacesContext.getCurrentInstance();
        Application app = ctxt.getApplication();
        
        try {
            // Get an instance of the DatabaseOperationsManager bean  
            this.dbOperations = app.evaluateExpressionGet(ctxt, "#{databaseOperations}", DatabaseOperationsManager.class);
        }
        catch (Exception e) {
            
            // Assume all exceptions from the lookup are fatal
            this.message = "Database connection unavailable: see error log";
            
            // Dump the exception to the log
            e.printStackTrace(System.out);
        }
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
        return "addEmployee";
    }
    
    /** 
     * Called by JSF when the user clicks the "Toggle JTA" button.
     * 
     * Toggles the state of the JTA flag.
     */
    public void toggleUseJta() {
        this.useJta = ! this.useJta;
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
        this.employee.setCanEdit(true);
    }
    
    /**
     * Called by JSF when the user clicks the "Save" button for a row.
     * 
     * This method will run the update function using the new values, updating the record
     * in the database. Will also clear the editable flag for the current record.
     * 
     * @see DatabaseOperationsManager#updateEmployee(Employee, boolean)
     */
    public void saveUpdates() throws Exception {
        
        try {
            // Call our utility routine to update the database
            this.dbOperations.updateEmployee(this.employee, this.useJta);
        }
        catch (Exception e) {
            // The database access class will have already rolledback our transaction
            this.message = "An error occurred: see error log";
            e.printStackTrace(System.out);
        }

        // Clear the flag that says we can edit this row
        this.employee.setCanEdit(false);
    }
    
    /** 
     * Called by JSF when the user presses the Search button.
     * 
     * This method will call the database search method, passing the supplied search string.
     * 
     * @return The name of the page to navigate to, which will contain the results.
     * 
     * @see DatabaseOperationsManager#findEmployeeByLastName(String)
     */
    public String search() {
        
        try {
            // Search the database for this string
            this.allResults = this.dbOperations.findEmployeeByLastName(this.searchString);
            
            // Message if no results are found
            if ( this.allResults.size() < 1 ) {
                this.message = "No results found";
            }
            else {
                this.message = "";
            }
        }
        catch (Exception e) {
            // The database access class will have already rolledback our transaction
            this.message = "An error occurred: see error log";
            e.printStackTrace(System.out);
        }
        
        // Redirect back to main page
        return "master";
    }
    
    /**
     * Called by JSF when the user clicks the "Delete" button for a record.
     * 
     * This method will update the deletable flag for the record in question,
     * which will have the effect of showing the "Confirm" button.
     */
    public void confirmDel() {
        this.employee.setCanDelete(true);
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
     * @see DatabaseOperationsManager#deleteEmployee(Employee, boolean)
     */
    public String deleteEmployee() {
        
        try {
            // Call the delete function for this employee
            this.dbOperations.deleteEmployee(this.employee, this.useJta);
        }
        catch (Exception e) {
        
            // Check for the delete permissions error
            if ( e.getMessage().contains("RESTRICTS THE DELETION") ) {
                // Not allowed to delete the record
                this.message = "ERROR: You cannot delete this record.";
            }
            else {
                // If we can't find the permission error, report the problem
                this.message = "An error occurred: see error log";
                e.printStackTrace(System.out);
            }

            // Clear the flag
            this.employee.setCanDelete(false);

            // Redirect back to main page
            return "master";
        }
        
        // Successful: call the search function, refreshing the view
        return search();
    }
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public String getSearchString() {
        return this.searchString;
    }
    
    public void setSearchString(String ss) {
        this.searchString = ss;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public int getFirstRow() {
        return this.firstRow;
    }
    
    public int getLastRow() {
        return this.lastRow;
    }
    
    public List<Employee> getallResults() {
        // Shallow clone so JSF can update the Employee instances
        return new ArrayList<>(this.allResults);
    }
    
    public void setAllResults(List<Employee> allResults) {
        // Shallow clone so JSF can update the Employee instances
        this.allResults = new ArrayList<>(allResults);
    }
    
    public Employee getEmployee() {
        return this.employee;
    }
    
    public void setEmployee(Employee emp) {
        this.employee = emp;
    }  

    public boolean getUseJta() {
        return this.useJta;
    }

    public boolean isDatabaseAvailable() {
        return this.dbOperations != null;
    }
}
