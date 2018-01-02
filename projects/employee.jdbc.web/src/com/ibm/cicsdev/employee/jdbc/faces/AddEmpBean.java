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

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.sql.DataSource;

import com.ibm.cicsdev.employee.jdbc.beans.Employee;
import com.ibm.cicsdev.employee.jdbc.impl.DbOperations;

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
public class AddEmpBean
{    
    /*
     * Instance fields.
     */    
    
	
    /**
     * The JNDI name used to lookup the JDBC DataSource instance.
     */
    public static final String DATABASE_JNDI = "jdbc/sample";
	
	
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
     * DataSource instance for connecting to the database using JDBC
     * Use of Resource injection required for container mgd security
     */          
    @Resource(authenticationType=AuthenticationType.CONTAINER,name=DATABASE_JNDI)
    private DataSource ds;
    
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
    public AddEmpBean() {       
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
        message = "";
        return "master.xhtml";
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
            DbOperations.createEmployee(ds, employee, useJta);
            
            // Update the message
            message = "SUCCESSFULLY ADDED EMPLOYEE";
            
            // Clear the input data, ready for next request
            empNo = "";
            firstName = "";
            lastName = "";
            gender = "";
            job = "";
        }
        catch (Exception e) {
            
            // Explicit test for duplicate values error
            if (e.getMessage().contains("DUPLICATE VALUES")) {
                message = "ERROR Employee number already in use";
            }
            else {
                message = "ERROR Please consult stderr";
            }
            
            // Dump to output for debug purposes
            e.printStackTrace();
        }
    }
    
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public boolean getUseJta() {
        return useJta;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getEmpNo() {
        return empNo;
    }
    
    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
