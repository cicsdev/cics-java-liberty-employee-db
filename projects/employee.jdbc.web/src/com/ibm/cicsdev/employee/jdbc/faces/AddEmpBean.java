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

import java.math.BigDecimal;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;
import com.ibm.cicsdev.employee.jdbc.impl.DbOperations;

/**
 * The bean class that handles the data and actions used
 * by the addEmp.xhtml page.
 * 
 * It includes data fields used to take user input, as well
 * as methods for sending that information to the database
 * and returning to previous pages.
 * 
 * @author Michael Jones
 *
 */
@ManagedBean(name = "addEmployee")
@SessionScoped
public class AddEmpBean
{    
    /*
     * Instance fields.
     */    
    
    /**
     * The employee number specified on the form.
     */
    private String empno;
    
    /**
     * The first name value specified on the form.
     */
    private String firstname;
    
    /**
     * The last name value specified on the form.
     */
    private String lastname;
    
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
    private String resultMessage = "";
    
    /**
     * Object for our Liberty data source.
     */
    private DataSource ds;
    
    /**
     * Flag to indicate we will use JTA for unit of work support.
     */
    private boolean jta = true;

    
    /*
     * Constructor.
     */
    
    /**
     * Default constructor called by JSF. This constructor
     * will attempt to set up the connection to the database.
     */
    public AddEmpBean() {
        
        try {
            // Attempt to lookup the configured DataSource instance
            this.ds = (DataSource) InitialContext.doLookup(DbOperations.DATABASE_JNDI);
        }
        catch (NamingException e) {
            // Flag the error and write out to the log
            this.resultMessage = "NO DATASOURCE CONNECTION";
            e.printStackTrace();
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
        this.resultMessage = "";
        return "master.xhtml";
    }
    
    /** 
     * Called by JSF when the user clicks the "Toggle JTA" button.
     * 
     * Toggles the state of the JTA flag.
     */
    public void toggleJta() {
        this.jta = ! this.jta;
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
        employee.setEmpNo(this.empno.toUpperCase());
        employee.setFirstName(this.firstname.toUpperCase());
        employee.setLastName(this.lastname.toUpperCase());
        employee.setSex(this.gender.toUpperCase());        
        employee.setJob(this.job.toUpperCase());

        try {
            // Attempt to create the new employee record in the DB
            DbOperations.createEmployee(this.ds, employee, this.jta);
            
            // Update the message
            this.resultMessage = "SUCCESSFULLY ADDED EMPLOYEE";
            
            // Clear the input data, ready for next request
            this.empno = "";
            this.firstname = "";
            this.lastname = "";
            this.gender = "";
            this.job = "";
        }
        catch (Exception e) {
            
            // Explicit test for duplicate values error
            if (e.getMessage().contains("DUPLICATE VALUES")) {
                this.resultMessage = "ERROR Employee number already in use";
            }
            else {
                this.resultMessage = "ERROR Please consult stderr";
            }
            
            // Dump to output for debug purposes
            e.printStackTrace();
        }
    }
    
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public boolean isJta() {
        return jta;
    }
    
    public String getMessage() {
        return this.resultMessage;
    }
    
    public String getEmpNo() {
        return empno;
    }
    
    public void setEmpNo(String empno) {
        this.empno = empno;
    }

    public String getFirstName() {
        return firstname;
    }
    
    public void setFirstName(String firstname) {
        this.firstname = firstname;
    }
    
    public String getLastName() {
        return lastname;
    }
    
    public void setLastName(String lastname) {
        this.lastname = lastname;
    }
    
    public String getGender() {
        return gender;
    }
    
    public void setGender(String gender) {
        this.gender = gender;
    }
    
    public String getJob() {
        return job;
    }
    
    public void setJob(String job) {
        this.job = job;
    }    
}
