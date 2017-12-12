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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * This class contains all of the database interaction
 * code for our application.
 * 
 * Data is passed in from either EmpListBean or AddEmpBean
 * to one of the update methods.
 * 
 * Those methods will then attempt to perform the methods.
 * Once complete they will return control to the caller,
 * which will print out a message to screen.
 * 
 * @author Michael Jones
 *
 */
public class DbOperations {
    
    /**
     * Name of CICS TSQ used to log activity.
     */
    private static final String TSQ_NAME = "DB2LOG"; 

    /**
     * Uses a specified last name to find a matching employee
     * in the database table.
     * 
     * Used by the search function on master.xhtml page
     * 
     * @return
     */
    public static ArrayList<Employee> findEmployeeByLastName(DataSource ds, String lastName) throws SQLException{
        
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {
            // The SQL command used to find our employees list
            String sqlCmd = "SELECT BIRTHDATE, BONUS, COMM, EDLEVEL, EMPNO, FIRSTNME, HIREDATE, JOB, LASTNAME, MIDINIT, PHONENO, "
                    + "SALARY, SEX, WORKDEPT FROM EMP "
                    + "WHERE LASTNAME LIKE ? ORDER BY LASTNAME, EMPNO";
            
            // Connect to the data source
            conn = ds.getConnection();
            
            // Prepare our query, then send to the DB
            statement = conn.prepareStatement(sqlCmd);
            // Uppercase lastname and set as 1st query value
            statement.setString(1, lastName.toUpperCase() + "%");
            ResultSet rs = statement.executeQuery();
            
            // Store any results in the Employee bean list
            ArrayList<Employee> results = new ArrayList<Employee>();
            while(rs.next()) {
                results.add(createEmployeeBean(rs));
            }
            
            // Close the connection to the DB
            statement.close();
            conn.close();
            
            // Return the full list
            return results;
            
        } catch(SQLException e) {
            // Close the connection to the DB
            if(statement !=null) {
                statement.close();
            }
            if(conn !=null) {
                conn.close();
            }
            
            // Propagate the exception
            throw e;
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
    public static void createEmployee(DataSource ds, Employee employee, final boolean useJta) throws Exception {
        
        // Otherwise prepare objects without a user transaction
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
            String sqlCmd = "INSERT INTO EMP (BIRTHDATE, BONUS, COMM, EDLEVEL, EMPNO, FIRSTNME, HIREDATE, JOB, LASTNAME, MIDINIT, PHONENO, SALARY, SEX, WORKDEPT) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // Start the DB connection
            conn = ds.getConnection();
            
            // Prepare the query to send to the DB
            statement = conn.prepareStatement(sqlCmd);
            statement = populateStatement(employee, statement);
            statement.executeUpdate();
            
            
            /*
             * Update a CICS resource.
             */
            
            // Update a TSQ, including it in the transaction
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            tsq.writeString("Added " + employee.getEmpno() + " with last name: " + employee.getLastname());

            
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
     * @throws Exception
     */
    public static void deleteEmployee(DataSource ds, Employee employee, final boolean useJta) throws Exception {
        
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
            
            // Set up the connection to the DB and execute the delete SQL statement
            conn = ds.getConnection();            
            statement = conn.prepareStatement("DELETE FROM EMP WHERE EMPNO = ?");
            statement.setString(1, employee.getEmpno());
            statement.execute();


            /*
             * Update a CICS resource.
             */
            
            // Write some basic information about the deleted record to a TSQ
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            tsq.writeString("Deleted " + employee.getEmpno() + " with last name: " + employee.getLastname());

            
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
     * Uses JTA to commit a update an employee in the database.
     * 
     * This method is called when a user presses the 'Edit' then 'Save' button
     * next to an employee row when the JTA toggle is true.
     * 
     * It will use the employee number in the bean to fill in an UPDATE statement
     * and update the associated record from the DB
     * 
     * @param ds - The target data source
     * @param employee - The employee object populated
     * @throws Exception
     */
    public static void updateEmployeeWithJta(DataSource ds, Employee employee) throws Exception {
        
        // Prepare the connection objects and a user transaction
        Connection conn = null;
        PreparedStatement statement = null;
        UserTransaction utx = null;
        
        try {
            
            // Get a new user transaction for this piece of work and start it
            utx = (UserTransaction)InitialContext.doLookup("java:comp/UserTransaction");
            utx.begin();
            
            // The update command template used to execute the query 
            String sqlCmd = "UPDATE EMP SET BIRTHDATE = ?, BONUS = ?, COMM = ?, EDLEVEL = ?, EMPNO = ?, FIRSTNME = ?, HIREDATE = ?, JOB = ?, LASTNAME = ?, "
                    + "MIDINIT = ?, PHONENO = ?, SALARY = ?, SEX = ?, WORKDEPT = ? WHERE EMPNO = ?";
            
            // Connect to the database
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlCmd);
            
            // Uppercase the Gender
            employee.setSex(employee.getSex().toUpperCase());
            
            // Format the statement using the 14 values from the employee bean
            populateStatement(employee, statement);
            // Set the 15th value as the employee number 
            statement.setString(15, employee.getEmpno());

            statement.execute();
            
            // Write some basic information about the update to a TSQ
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            tsq.writeString("Updated " + employee.getEmpno() + " with last name: " + employee.getLastname());
            
            // Commit the changes and close the connection
            utx.commit();            
            statement.close();
            conn.close();
            
        }catch(Exception e) {
            
            // Close the connection to the DB
            if(statement !=null) {
                statement.close();
            }
            if(conn !=null) {
                conn.close();
            }
            
            // Propagate the exception
            throw e;
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
     * @throws Exception
     */
    public static void updateEmployee(DataSource ds, Employee employee, boolean useJta) throws Exception {
        
        // If we want to use JTA, call the right method
        if(useJta) {
            updateEmployeeWithJta(ds, employee);
            return;
        }
        
        // If not, prepare the objects with out a user tranasction
        Connection conn = null;
        PreparedStatement statement = null;
        
        try {
            
            // The update command template used for the operation
            String sqlCmd = "UPDATE EMP SET BIRTHDATE = ?, BONUS = ?, COMM = ?, EDLEVEL = ?, EMPNO = ?, FIRSTNME = ?, HIREDATE = ?, JOB = ?, LASTNAME = ?, "
                    + "MIDINIT = ?, PHONENO = ?, SALARY = ?, SEX = ?, WORKDEPT = ? WHERE EMPNO = ?";

            // Set up the connection to the database
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlCmd);            
            
            // Uppercase the Gender
            employee.setSex(employee.getSex().toUpperCase());

            // Format the SQL statement using the 14 values from the employee bean
            populateStatement(employee, statement);
            // Set the 15th value as the employee number 
            statement.setString(15, employee.getEmpno());
            statement.execute();
            
            // Write some basic information about the updated record to a TSQ
            TSQ tsq = new TSQ();
            tsq.setName(TSQ_NAME);
            tsq.writeString("Updated " + employee.getEmpno() + " with last name: " + employee.getLastname());
            
            // Commit the changes and close the connection
            conn.commit();            
            statement.close();
            conn.close();
            
        }catch(Exception e) {
            
            // Close the connection to the DB
            if(statement !=null) {
                statement.close();
            }
            if(conn !=null) {
                conn.close();
            }
            
            // Propagate the exception
            throw e;
        }
    }
    
    /**
     * Takes a ResultSet with a set pointer, and extracts
     * the Employee information from the row, storing it in a
     * new Employee bean.
     * 
     * @param currentResult - ResultSet with pointer
     * 
     * @return - Populated Employee bean
     */
    private static Employee createEmployeeBean(ResultSet currentResult) throws SQLException{
        
        Employee employee = new Employee();
        
        // Gather the employee information from the DB and set up the bean
        employee.setBirthdate(currentResult.getDate("BIRTHDATE"));
        employee.setBonus(currentResult.getBigDecimal("BONUS"));
        employee.setComm(currentResult.getBigDecimal("COMM"));
        employee.setEdlevel(currentResult.getObject("EDLEVEL") == null ? 0 : (short)currentResult.getShort("EDLEVEL"));
        employee.setEmpno(currentResult.getString("EMPNO"));
        employee.setFirstname(currentResult.getString("FIRSTNME"));
        employee.setHiredate(currentResult.getDate("HIREDATE"));
        employee.setJob(currentResult.getString("JOB"));
        employee.setLastname(currentResult.getString("LASTNAME"));
        employee.setMidinit(currentResult.getString("MIDINIT"));
        employee.setPhoneno(currentResult.getString("PHONENO"));
        employee.setSalary(currentResult.getBigDecimal("SALARY"));
        employee.setSex(currentResult.getString("SEX"));
        
        
        return employee;
    }
    
    
    /**
     * Populates a CREATE statement with values, taken from an employee bean.
     * 
     * @param employee - The employee you wish to use values from
     * @param statement - The statement you want to populate
     * @return - A populated statement
     * @throws SQLException
     */
    private static PreparedStatement populateStatement(Employee employee, PreparedStatement statement) throws SQLException {
        
        // Set a null department, as not sed for the application
        String deptno = null;
    
        // Check for the values on date fields.
        Date bDate = employee.getBirthdate() == null ? null : (new Date(employee.getBirthdate().getTime()));
        Date hDate = employee.getHiredate() == null ? null : (new Date(employee.getHiredate().getTime()));
        
        // Fill in the rest of the fields
        statement.setDate(1, bDate);
        statement.setBigDecimal(2, employee.getBonus());
        statement.setBigDecimal(3, employee.getComm());
        statement.setShort(4, employee.getEdlevel());
        statement.setString(5, employee.getEmpno());
        statement.setString(6,  employee.getFirstname());
        statement.setDate(7, hDate);
        statement.setString(8,  employee.getJob());
        statement.setString(9,  employee.getLastname());
        statement.setString(10,  employee.getMidinit());
        statement.setString(11,  employee.getPhoneno());
        statement.setBigDecimal(12, employee.getSalary());
        statement.setString(13,  employee.getSex());
        statement.setString(14,  deptno);
        
        return statement;
    }
    
    
}
