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
@ManagedBean(name = "addEmpBean")
@SessionScoped
public class AddEmpBean {
    
    /** Used to store information ready to be added to the DB */
    Employee employee;
    
    /** The Employee number specified on the form */
    public String empno;
    
    /** The first name value specified on addEmp.xhtml */
    public String firstname;
    
    /** The last name value specified on addEmp.xhtml */
    public String lastname;
    
    /** The gender value specified on addEmp.xhtml */
    public String gender;
    
    /** The job value specified on addEmp.xhtml */
    public String job;
    
    /** Holds the result message of our submission **/
    public String resultMessage = "";
    
    /** Object for our Liberty data source **/
    DataSource ds;
    
    /** Stores the current state of the JTA flag **/
    public boolean jtatoggle = true;
        
    /**
     * Default constructor called by JSF. This constructor
     * will attempt to set up the connection to the database.
     */
    public AddEmpBean() {
        employee = new Employee();
        
        try {
            ds = (DataSource)InitialContext.doLookup("jdbc/sample");
        } catch(NamingException e) {
            resultMessage = "NO DATABASE AVAILABLE";
            e.printStackTrace();
        }
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
    
    /**
     * Provides the action of the "Go back" button in
     * addEmp.xhtml. This will send the user back to 
     * the master page.
     * 
     * @return
     */
    public String goBack() {
        resultMessage = "";
        return "master.xhtml";
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
        
        // Set the employee object up ready for submission
        setDefaults(employee);
        
        // Attempt to create the new employee record in the DB
        try {
            DbOperations.createEmployee(ds, employee, jtatoggle);
            resultMessage = "SUCCESSFULLY ADDED EMPLOYEE";
        } catch (Exception e) {
            resultMessage = "ERROR ";
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
    
    /**
     * This method will fill in an Employee bean for
     * the createEmployee method to use.
     * 
     * Some values are not provided by the user, so need
     * to be defaulted.
     * 
     * @param emp - A populated Employee bean
     */
    private void setDefaults(Employee emp) {
        
        // Set our default values. We don't display
        // these so we don't care about them.
        emp.setBonus(new BigDecimal(1000000));
        emp.setComm(new BigDecimal(1000000));
        emp.setMidInit("R");
        emp.setPhoneNo("1111");
        emp.setSalary(new BigDecimal(1000000));        
        short s = 1; emp.setEdLevel(s);
        
        // Now add in the user input
        emp.setEmpNo(this.empno.toUpperCase());
        emp.setFirstName(firstname.toUpperCase());
        emp.setJob(job.toUpperCase());
        emp.setLastName(lastname.toUpperCase());
        emp.setSex(gender.toUpperCase());        
        
    }
    
    /**
     * Used by JSF to check the value of the JTA toggle and
     * display it on the addEmp.xhtml page
     * 
     * @return - the current toggle value
     */
    public boolean getjtatoggle() {
        return jtatoggle;
    }
    
    /** 
     * Provides the action for the 'Toggle JTA' button in
     * master.xhtml. 
     * 
     * If JTA is on it will switch it off, and vice versa.
     */
    public String toggleJta() {
        if(jtatoggle) {
            jtatoggle = false;
        } else {
            jtatoggle = true;
        }
        
        return "addEmp.xhtml";
    }
}
