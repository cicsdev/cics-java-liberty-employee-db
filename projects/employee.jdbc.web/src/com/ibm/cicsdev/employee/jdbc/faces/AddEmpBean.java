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
     * Provides the action of the "Go back" button in
     * addEmp.xhtml. This will send the user back to 
     * the master page.
     * 
     * @return
     */
    public String goBack() {
        this.resultMessage = "";
        return "master.xhtml";
    }
    
    /** 
     * Provides the action for the 'Toggle JTA' button in
     * master.xhtml. 
     * 
     * If JTA is on it will switch it off, and vice versa. No return value as we
     * want to stay on the same page.
     */
    public void toggleJta() {
        this.jta = ! this.jta;
    }

    /**
     * Submits the entered employee to the database.
     * 
     * This method is called by the 'Add employee' button on the
     * addEmp.xhtml page.
     * 
     * The method will attempt to add the new employee, displaying
     * messages if possible.
     * 
     * @return - Sends you back to addEmp.xhtml
     */
    public String createInDb() {
        
        // Create a new instance to store the data
        Employee employee = new Employee();
        
        // Set our default values - we don't display these, so we don't care about them
        employee.setBonus( new BigDecimal(1_000_000) );
        employee.setComm(new BigDecimal(1000000));
        employee.setMidInit("R");
        employee.setPhoneNo("1111");
        employee.setSalary(new BigDecimal(1000000));        
        short s = 1;
        employee.setEdLevel(s);
        
        // Now add in the user input
        employee.setEmpNo(this.empno.toUpperCase());
        employee.setFirstName(firstname.toUpperCase());
        employee.setJob(job.toUpperCase());
        employee.setLastName(lastname.toUpperCase());
        employee.setSex(gender.toUpperCase());        

        
        // Attempt to create the new employee record in the DB
        try {
            DbOperations.createEmployee(this.ds, employee, this.jta);
            this.resultMessage = "SUCCESSFULLY ADDED EMPLOYEE";
        } catch (Exception e) {
            this.resultMessage = "ERROR ";
            if(e.getMessage().contains("DUPLICATE VALUES")) {
                resultMessage += "Employee number already in use";
            } else {
                resultMessage += "Please consult stderr.";
            }
            e.printStackTrace();
        }
        
        // Refresh the page
        return "addEmp.xhtml";
    }
    
    
    /*
     * Attribute accessor methods used by JSF.
     */
    
    public boolean isJta() {
        return jta;
    }
    
    /**
     * Allows JSF to retrieve the value of the
     * resultMessage field, used to inform the user
     * of the result of their submission.
     * 
     * @return
     */
    public String getresultMessage() {
        return this.resultMessage;
    }
    
    /**
     * Allows the employee number to be retrieved
     * by JSF 
     * @return
     */
    public String getempno() {
        return empno;
    }
    
    /**
     * Allows JSF to set the employee number based
     * on the data provided by the user on addEmp.xhtml
     * 
     * @param empno
     */
    public void setempno(String empno) {
        this.empno = empno;
    }

    /**
     * Allows JSF to retrieve the first name field.
     * @return
     */
    public String getfirstname() {
        return firstname;
    }
    
    /**
     * Allows JSF to set the value of firstname based
     * on the user data provided on addEmp.xhtml
     * 
     * @param firstname
     */
    public void setfirstname(String firstname) {
        this.firstname = firstname;
    }
    
    /** 
     * Allows JSF to retrieve the value of lastname
     * field. 
     * @return
     */
    public String getlastname() {
        return lastname;
    }
    
    /**
     * Allows JSF to set the value of the lastname
     * field based on the information provided by the
     * user on addEmp.xhtml
     * 
     * @param lastname
     */
    public void setlastname(String lastname) {
        this.lastname = lastname;
    }
    
    /**
     * Allows JSF to get the value of the gender field.
     * @return
     */
    public String getgender() {
        return gender;
    }
    
    /**
     * Allows JSF to set the value of the gender field
     * based on the value the user provides in addEmp.xhtml.
     * @param gender
     */
    public void setgender(String gender) {
        this.gender = gender;
    }
    
    /**
     * Allows JSF to retrieve the value of job
     * @return
     */
    public String getjob() {
        return job;
    }
    
    /**
     * Allows JSF to set the value of the job field
     * based on the data the user provides in addEmp.xhtml.
     * 
     * @param job
     */
    public void setjob(String job) {
        this.job = job;
    }
    
}
