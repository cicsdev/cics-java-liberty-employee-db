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
@ManagedBean(name = "empListBean")
@SessionScoped
public class EmpListBean
{
    /**
     * Stores current target employee for update/delete.
     */
    private Employee emp;
    /**
     * Allows JSF to retrieve the emp field, which contains
     * the specific employee being edited.
     * FIXME Funny behaviour when you select delete/edit for
     * more than one record
     * @return
     */
    /**
     * Allows JSF to store a specific employee in the emp field.
     * This is used to track which one is being modified with
     * either edit or delete.
     * 
     * @param emp
     */
    
    /**
     * Object for our Liberty data source.
     */
    private DataSource ds;
    
    /**
     * Stores the last name used as the search criteria.
     */
    private String lastName;
    
    /**
     * Stores the results of any search. Used by JSF to display
     * the results in a table.
     */
    private ArrayList<Employee> allResults = new ArrayList<Employee>();
    /**
     * Returns the full list of results. Used by JSF
     * to populate a table in empList.xhtml.
     * @return
     */
    /**
     * Sets the allResults field. Not used by application
     * but must be present for JSF to function.
     * @param allResults
     */
    
    /**
     * Used to indicate the index of the first result displayed.
     */
    private int firstRow = 0;
    
    /**
     * Used to limit the number of rows displayed by the application.
     */
    private int lastRow = 15;
    
    /**
     * Currently used to store error messages.
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
            ds = (DataSource)InitialContext.doLookup("jdbc/sample");
            databaseAvailable = true;
        } catch(NamingException e) {
            message = "NO DATASOURCE CONNECTION";
            e.printStackTrace();
        }
        
    }
    /** 
     * Performs the actual search function. This will be called by
     * JSF when the user presses the search button.
     * 
     * @return - The empList.xhtml page, with results included
     */
    public String search() {
        
        try {
            allResults = DbOperations.findEmployeeByLastName(ds, lastName);
            if(allResults.size() < 1) {
                message = "NO RESULTS FOUND.";
            }else {
                message = "";
            }
        } catch(Exception e) {
            e.printStackTrace();
            message = "ERROR: Please see stderr";
        }
        return "master.xhtml";
    }
    
    /**
     * Updates the canEdit flag for a row. When this flag
     * is set to true, the fields become input fields rather
     * than just text boxes
     */
    public void setCanEdit() {
        emp.setCanEdit(true);
    }
    
    /**
     * Saves any edits made by the user in the form after the edit button
     * has been clicked.
     * 
     * It will run the update function using the new values, updating the
     * record in the DB.
     * 
     * Will also re-enable the edit button, hiding the save button
     */
    public void saveUpdates() {
        
        try {
            DbOperations.updateEmployee(ds, emp, jta);
        } catch(Exception e) {
            message = "ERROR: Please check stderr.";
            e.printStackTrace();
        }
        
        
        emp.setCanEdit(false);
    }
    
    /**
     * Allows JSF to check the canDel flag on an employee.
     * 
     * This flag enables or disabled the delete function.
     */
    public void confirmDel() {
        emp.setCanDel(true);
    }
    
    /** 
     * Provides the action for the 'Toggle JTA' button in
     * master.xhtml. 
     * 
     * If JTA is on it'll switch it off, and vice versa.
     * It will also update the status message.
     */
    public void toggleJta() {
        this.jta = ! this.jta;
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
    public String deleteEmp() {
        
        try {
            // call the delete employee function for this employee
            DbOperations.deleteEmployee(ds, emp, jta);
        } catch(Exception e) {
        
            // Check for the delete permissions error
            // If found, returns the user to the same page
            if(e.getMessage().contains("RESTRICTS THE DELETION")){
                message = "ERROR: You cannot delete this record.";
                emp.setCanDel(false);
                return "master.xhtml";
            }
            
            // If we can't find the permission error, report the problem
            message = "ERROR: See stdout for details";
            e.printStackTrace();
            emp.setCanDel(false);
            return "master.xhtml";
            
        }    
        
        // Call the search function, refreshing the view
        // of the rows, removing the deleted record
        return search();
    }
    
    /**
     * Provides the function for the 'Add employee' button
     * that directs to the addEmp.xhtml page.
     * 
     * @return
     */
    public String goToAddScreen() {
        return "addEmp.xhtml";
    }
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public String getlastName() {
        return lastName;
    }
    
    public void setlastName(String lastName) {
        this.lastName = lastName;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public int getlastRow() {
        return lastRow;
    }
    
    public int getfirstRow() {
        return firstRow;
    }
    
    public ArrayList<Employee> getallResults() {
        return allResults;
    }
    
    public void setAllResults(ArrayList<Employee> allResults) {
        this.allResults = allResults;
    }
    
    public Employee getemp() {
        return emp;
    }
    
    public void setemp(Employee emp) {
        this.emp = emp;
    }
    
    public boolean isDatabaseAvailable() {
        return databaseAvailable;
    }
    
    public boolean isJta() {
        return jta;
    }    
}
