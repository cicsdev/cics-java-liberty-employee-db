/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2017 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.employee.jdbc.impl;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

import com.ibm.cics.server.TSQ;
import com.ibm.cicsdev.employee.jdbc.beans.Employee;
import com.ibm.cicsdev.employee.jdbc.faces.AddEmpBean;
import com.ibm.cicsdev.employee.jdbc.faces.EmpListBean;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class contains all of the database interaction code for our application.
 * 
 * Data is passed in from either {@link EmpListBean} or {@link AddEmpBean} to one
 * of the update methods. Those methods will then attempt to perform the requested
 * operation. Once complete they will return control to the caller, which will
 * print out a message to screen.
 * 
 * @author Michael Jones
 */
public class DbOperations {
    
    /**
     * Name of CICS TSQ used to log activity.
     */
    private static final String TSQ_NAME = "DB2LOG"; 

    /**
     * The JNDI name used to lookup the JDBC DataSource instance.
     */
    public static final String DATABASE_JNDI = "jdbc/sample";
    
    /**
     * Uses a specified last name to find a matching employee in the database table.
     * 
     * Used by the search function on master.xhtml page
     * 
     * @param ds - the DataSource used to connect to the database.
     * @param lastName - the search argument to be applied to the lastName field.
     * 
     * @return a list of {@link Employee} instances
     * 
     * @throws Exception All exceptions are propagated from this method.
     */
    public static ArrayList<Employee> findEmployeeByLastName(DataSource ds, String lastName) throws SQLException
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {
            // The SQL command used to find our employees list
            String sqlCmd = "SELECT " +
                                "BIRTHDATE, BONUS, COMM, EDLEVEL, EMPNO, " +
                                "FIRSTNME, HIREDATE, JOB, LASTNAME, MIDINIT, " +
                                "PHONENO, SALARY, SEX, WORKDEPT " +
                            "FROM EMP  WHERE LASTNAME LIKE ? ORDER BY LASTNAME, EMPNO";
            
            // Get the DB connection
            conn = ds.getConnection();
            
            // Prepare the statement - uppercase lastname and set as first query value
            statement = conn.prepareStatement(sqlCmd);
            statement.setString(1, lastName.toUpperCase() + "%");
            
            // Perform the SELECT operation
            ResultSet rs = statement.executeQuery();
            
            // Store any results in the Employee bean list
            ArrayList<Employee> results = new ArrayList<Employee>();
            while ( rs.next() ) {
                results.add(createEmployeeBean(rs));
            }
            
            // Return the full list
            return results;
        }
        finally {
            
            // Any exceptions will be propagated
            
            // Close database objects, regardless of what happened
            if ( statement != null ) {
                statement.close();
            }
            if ( conn != null ) {
                conn.close();
            }
        }
    }
    
    /**
     * Writes a new employee to the database.
     * 
     * This method is called when a user presses 'Add employee' button.
     * It will add the employee based on the values provided in the already-populated bean
     * 
     * @param ds - The target data source
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than the
     * CICS unit of work support
     * 
     * @throws Exception All exceptions are propagated from this method.
     */
    public static void createEmployee(DataSource ds, Employee employee, final boolean useJta) throws Exception
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // The JTA transaction, if we're using one
            UserTransaction utx;
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
            }
            else {
                // A unit of work is already provided for this CICS transaction: no-op
                // Compiler not smart enough to work out utx won't be used again
                utx = null;
            }
            
            
            /*
             * Update the database.
             */
            
            // Our INSERT command for the DB
            String sqlCmd = "INSERT INTO EMP (" +
                                "BIRTHDATE, BONUS,    COMM, EDLEVEL,  EMPNO, " +
                                "FIRSTNME,  HIREDATE, JOB,  LASTNAME, MIDINIT, " +
                                "PHONENO,   SALARY,   SEX,  WORKDEPT) " +
                            "VALUES (" +
                                "?, ?, ?, ?, ?, " +
                                "?, ?, ?, ?, ?, " + 
                                "?, ?, ?, ?)";
            
            // Get the DB connection
            conn = ds.getConnection();
            
            // Prepare the statement and populate with data
            statement = conn.prepareStatement(sqlCmd);
            statement = populateStatement(statement, employee);
            
            // Perform the INSERT operation
            statement.executeUpdate();
            
            
            /*
             * Update a CICS resource.
             */
            
            // Update a TSQ, including it in the transaction
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            String msg = String.format("Added %s with last name: %s", employee.getEmpNo(), employee.getLastName());
            tsq.writeString(msg);

            
            /*
             * Commit the transaction.
             */
            
            // Handle JTA and non-JTA commits differently
            if ( useJta ) {
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else {
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        finally {
        
            // Any exceptions will be propagated
            
            // Close database objects, regardless of what happened
            if ( statement != null ) {
                statement.close();
            }
            if ( conn != null ) {
                conn.close();
            }
        }
    }
    
    /**
     * Deletes an employee from the database.
     * 
     * This method is called when a user presses the 'Delete' button next to an employee row.
     * 
     * It will use the employee number in the bean to fill in an delete statement
     * and remove the associated record from the DB.
     * 
     * @param ds - The target data source
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than CICS
     * 
     * @throws Exception All exceptions are propagated from this method.
     */
    public static void deleteEmployee(DataSource ds, Employee employee, final boolean useJta) throws Exception
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // The JTA transaction, if we're using one
            UserTransaction utx;
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
            }
            else {
                // A unit of work is already provided for this CICS transaction: no-op
                // Compiler not smart enough to work out utx won't be used again
                utx = null;
            }
            
            
            /*
             * Update the database.
             */
            
            // Get the DB connection
            conn = ds.getConnection();
            
            // Prepare the statement and add the specified employee number
            statement = conn.prepareStatement("DELETE FROM EMP WHERE EMPNO = ?");
            statement.setString(1, employee.getEmpNo());
            
            // Perform the DELETE operation
            statement.execute();


            /*
             * Update a CICS resource.
             */
            
            // Write some basic information about the deleted record to a TSQ
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            String msg = String.format("Deleted %s with last name: %s", employee.getEmpNo(), employee.getLastName());
            tsq.writeString(msg);

            
            /*
             * Commit the transaction.
             */
            
            // Handle JTA and non-JTA commits differently
            if ( useJta ) {
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else {
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        finally {
            
            // Any exceptions will be propagated
            
            // Close database objects, regardless of what happened
            if ( statement != null ) {
                statement.close();
            }
            if ( conn != null ) {
                conn.close();
            }
        }
    }
    
    /**
     * Uses connection.commit to commit a update an employee in the database.
     * 
     * This method is called when a user presses the 'Edit' then 'Save' button
     * next to an employee row when the JTA toggle is false.
     * 
     * It will use the employee number in the bean to fill in an UPDATE statement
     * and update the associated record from the DB
     * 
     * @param ds - The target data source
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than CICS
     * 
     * @throws Exception All exceptions are propagated from this method.
     */
    public static void updateEmployee(DataSource ds, Employee employee, final boolean useJta) throws Exception
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // The JTA transaction, if we're using one
            UserTransaction utx;
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
            }
            else {
                // A unit of work is already provided for this CICS transaction: no-op
                // Compiler not smart enough to work out utx won't be used again
                utx = null;
            }

            
            /*
             * Clean some of the data before passing to the database.
             */

            // Uppercase the gender
            employee.setGender(employee.getGender().toUpperCase());

            
            /*
             * Update the database.
             */
            
            // The update command template used for the operation
            String sqlCmd = "UPDATE EMP SET " +
                                "BIRTHDATE = ?, BONUS = ?, COMM = ?, EDLEVEL = ?, EMPNO = ?, " +
                                "FIRSTNME = ?, HIREDATE = ?, JOB = ?, LASTNAME = ?, MIDINIT = ?, " +
                                "PHONENO = ?, SALARY = ?, SEX = ?, WORKDEPT = ? " +
                            "WHERE EMPNO = ?";

            // Get the DB connection
            conn = ds.getConnection();

            // Prepare the statement and populate with data
            statement = conn.prepareStatement(sqlCmd);
            populateStatement(statement, employee);
            statement.setString(15, employee.getEmpNo());
            
            // Perform the UPDATE operation
            statement.execute();


            /*
             * Update a CICS resource.
             */
            
            // Write some basic information about the updated record to a TSQ
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            String msg = String.format("Updated %s with last name: %s", employee.getEmpNo(), employee.getLastName());
            tsq.writeString(msg);

            
            /*
             * Commit the transaction.
             */
            
            // Handle JTA and non-JTA commits differently
            if ( useJta ) {
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else {
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        finally {
            
            // Any exceptions will be propagated
            
            // Close database objects, regardless of what happened
            if ( statement != null ) {
                statement.close();
            }
            if ( conn != null ) {
                conn.close();
            }
        }
    }

    
    /**
     * Takes a ResultSet with a set pointer, and extracts
     * the Employee information from the row, storing it in a
     * new Employee bean.
     * 
     * @param rs - ResultSet with pointer
     * 
     * @return A populated Employee bean
     * 
     * @throws Exception All exceptions are propagated from this method.
     */
    private static Employee createEmployeeBean(ResultSet rs) throws SQLException
    {
        // Create a new instance
        Employee employee = new Employee();
        
        // Gather the employee information from the current row of the ResultSet and set up the bean
        employee.setBirthDate(rs.getDate("BIRTHDATE"));
        employee.setBonus(rs.getBigDecimal("BONUS"));
        employee.setComm(rs.getBigDecimal("COMM"));
        employee.setEdLevel(rs.getObject("EDLEVEL") == null ? 0 : (short) rs.getShort("EDLEVEL"));
        employee.setEmpNo(rs.getString("EMPNO"));
        employee.setFirstName(rs.getString("FIRSTNME"));
        employee.setHireDate(rs.getDate("HIREDATE"));
        employee.setJob(rs.getString("JOB"));
        employee.setLastName(rs.getString("LASTNAME"));
        employee.setMidInit(rs.getString("MIDINIT"));
        employee.setPhoneNo(rs.getString("PHONENO"));
        employee.setSalary(rs.getBigDecimal("SALARY"));
        employee.setGender(rs.getString("SEX"));
        
        // Return the constructed instance
        return employee;
    }
    
    
    /**
     * Populates a CREATE statement with values, taken from an employee bean.
     * 
     * @param statement - The statement you want to populate
     * @param employee - The employee you wish to use values from
     * 
     * @return A populated statement
     * 
     * @throws SQLException if any JDBC errors are encountered when updating the statement.
     */
    private static PreparedStatement populateStatement(PreparedStatement statement, Employee employee) throws SQLException
    {
        // Check for non-null value on birth date field
        Date bDate = employee.getBirthDate() == null ? null : new Date(employee.getBirthDate().getTime());
        statement.setDate(1, bDate);
        
        // Check for non-null value on hire date field
        Date hDate = employee.getHireDate() == null ? null : new Date(employee.getHireDate().getTime());
        statement.setDate(7, hDate);
                
        // Set a null department, as not set for the application
        statement.setString(14, null);
        
        // Fill in the rest of the fields
        statement.setBigDecimal(2, employee.getBonus());
        statement.setBigDecimal(3, employee.getComm());
        statement.setShort(4, employee.getEdLevel());
        statement.setString(5, employee.getEmpNo());
        statement.setString(6, employee.getFirstName());
        statement.setString(8, employee.getJob());
        statement.setString(9, employee.getLastName());
        statement.setString(10, employee.getMidInit());
        statement.setString(11, employee.getPhoneNo());
        statement.setBigDecimal(12, employee.getSalary());
        statement.setString(13, employee.getGender());
        
        // Return the populated statement
        return statement;
    }
}
