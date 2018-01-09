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

import java.math.BigDecimal;

import javax.annotation.PostConstruct;
import javax.faces.application.Application;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;

/**
 * The bean class that handles the data and actions used by the Add Employee page. 
 * 
 * It includes data fields used to take user input, as well as methods for sending
 * that information to the database and returning to previous pages.
 * 
 * @author Michael Jones
 *
 */
@ManagedBean(name = "addEmployee")
@SessionScoped
public class AddEmployeeManager
{    
    /*
     * Instance fields.
     */    
    
    /**
     * The employee number specified on the form.
     */
    private String empNo;
    
    /**
     * The first name value specified on the form.
     */
    private String firstName;
    
    /**
     * The last name value specified on the form.
     */
    private String lastName;
    
    /**
     * The gender value specified on the form.
     */
    private String gender;
    
    /**
     * The job value specified on the form.
     */
    private String job;
    
    /**
     * Current error message for display.
     */
    private String message = "";

    /**
     * Field used to access the DB manipulation methods.
     */
    private DatabaseOperationsManager dbOperations;

    /**
     * Flag to indicate we will use JTA for unit of work support.
     */
    private boolean useJta = true;

    
    /*
     * Constructor.
     */
    
    /**
     * Default constructor called by JSF.
     * 
     */
    public AddEmployeeManager() {       
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
     * Called by JSF when the user clicks the "Go back" button.
     * 
     * This will clear any error message and send the user back to the master page.
     * 
     * @return The name of the page to navigate to
     */
    public String goBack() {
        this.message = "";
        return "master";
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
     * Called by JSF when the user clicks the "Add employee" button.
     * 
     * The method will attempt to add the new employee to the database,
     * displaying messages where appropriate.
     */
    public void create() {
        
        // Create a new instance to store the data
        Employee employee = new Employee();
        
        // Set our default values - we don't display these, so we don't care about them
        employee.setMidInit("R");
        employee.setPhoneNo("1111");
        employee.setBonus( new BigDecimal(1_000_000) );
        employee.setComm( new BigDecimal(1_000_000) );
        employee.setSalary( new BigDecimal(1_000_000) );        
        employee.setEdLevel((short) 1);
        
        // Now add in the user input
        employee.setEmpNo(this.empNo.toUpperCase());
        employee.setFirstName(this.firstName.toUpperCase());
        employee.setLastName(this.lastName.toUpperCase());
        employee.setGender(this.gender.toUpperCase());        
        employee.setJob(this.job.toUpperCase());

        try {
            // Attempt to create the new employee record in the DB
            this.dbOperations.createEmployee(employee, this.useJta);
            
            // Update the message
            this.message = "Successfully added employee";
            
            // Clear the input data, ready for next request
            this.empNo = "";
            this.firstName = "";
            this.lastName = "";
            this.gender = "";
            this.job = "";
        }
        catch (Exception e) {
            
            // The database access class will have already rolledback our transaction
            
            // Explicit test for duplicate values error
            if (e.getMessage().contains("DUPLICATE VALUES")) {
                this.message = "Error: Employee number already in use";
            }
            else {
                this.message = "An error occurred: see error log";
            }
            
            // Dump to output for debug purposes
            e.printStackTrace(System.out);
        }
    }
    
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public boolean getUseJta() {
        return this.useJta;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public String getEmpNo() {
        return this.empNo;
    }
    
    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getFirstName() {
        return this.firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return this.lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getGender() {
        return this.gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getJob() {
        return this.job;
    }
    
    public void setJob(String job) {
        this.job = job;
    }    

    public boolean isDatabaseAvailable() {
        return this.dbOperations != null;
    }
}
