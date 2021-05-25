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

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import com.ibm.cics.server.CicsConditionException;
import com.ibm.cics.server.TSQ;
import com.ibm.cicsdev.employee.jdbc.beans.Employee;

/**
 * This class contains all of the database interaction code for our application.
 * 
 * Data is passed in from either {@link EmployeeListManager} or {@link AddEmployeeManager} to one
 * of the update methods. Those methods will then attempt to perform the requested
 * operation. Once complete they will return control to the caller, which will
 * print out a message to screen.
 * 
 * @author Michael Jones
 */
@ManagedBean(name = "databaseOperations")
@ApplicationScoped
public class DatabaseOperationsManager
{
    /**
     * Name of CICS TSQ used to log activity.
     */
    private static final String TSQ_NAME = "DB2LOG";

    /**
     * DataSource instance for connecting to the database using JDBC
     * Use of Resource injection required for container managed security
     */          
    @Resource(authenticationType = AuthenticationType.CONTAINER, name = "jdbc/sample")
    private DataSource ds;    
    
    /**
     * Uses a specified last name to find a matching employee in the database table.
     * 
     * Used by the search function on main.xhtml page
     * 
     * @param lastName - the search argument to be applied to the lastName field.
     * 
     * @return a list of {@link Employee} instances
     * 
     * @throws SQLException All SQL exceptions are propagated from this method.
     */
    public List<Employee> findEmployeeByLastName(String lastName) throws SQLException
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
            conn = this.ds.getConnection();
            
            // This is only a search - for this example we are not updating any resources or require any locks
            conn.setAutoCommit(true);
            
            // Prepare the statement - uppercase lastname and set as first query value
            statement = conn.prepareStatement(sqlCmd);
            statement.setString(1, lastName.toUpperCase() + "%");
            
            // Perform the SELECT operation
            ResultSet rs = statement.executeQuery();
            
            // Store any results in the Employee bean list
            List<Employee> results = new ArrayList<>();
            while ( rs.next() ) {
                results.add( createEmployeeBean(rs) );
            }
            
            // Return the full list
            return results;
        }
        finally {
            
            // Any exceptions will be propagated
            
            // No transaction rollback needed - all operations read-only
            
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
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than the CICS unit of work support
     * 
     * @throws NamingException if the JNDI lookup of the UserTransaction fails
     * @throws SQLException if a JDBC error occurs
     * @throws CicsConditionException if a CICS error occurs
     * @throws NotSupportedException propagated from {@link UserTransaction#begin()}
     * @throws RollbackException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicMixedException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicRollbackException propagated from {@link UserTransaction#commit()} 
     * @throws SystemException propagated from {@link UserTransaction#begin()} and {@link UserTransaction#commit()}
     */
    public void createEmployee(Employee employee, final boolean useJta)
            throws NamingException, SQLException, CicsConditionException,
            NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException 
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        // The JTA transaction, if we're using one
        UserTransaction utx = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
            }
            else {
                // JTA not required so null user transaction
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
            conn = this.ds.getConnection();
            conn.setAutoCommit(false);
            
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
            
            if ( utx != null ) {
                
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else if ( conn != null ) {
                
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        catch (Throwable t) {
            
            // Make sure we rollback the transaction
            if ( utx != null ) {
                
                // Use the JTA API to rollback the changes
                utx.rollback();
            }
            else if ( conn != null ) {
                
                // Use the connection to rollback the changes
                conn.rollback();
            }
            
            // Rethrow out to the caller
            throw t;
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
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than CICS
     * 
     * @throws NamingException if the JNDI lookup of the UserTransaction fails
     * @throws SQLException if a JDBC error occurs
     * @throws CicsConditionException if a CICS error occurs
     * @throws NotSupportedException propagated from {@link UserTransaction#begin()}
     * @throws RollbackException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicMixedException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicRollbackException propagated from {@link UserTransaction#commit()} 
     * @throws SystemException propagated from {@link UserTransaction#begin()} and {@link UserTransaction#commit()}
     */
    public void deleteEmployee(Employee employee, final boolean useJta)
            throws NamingException, SQLException, CicsConditionException,
            NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException     
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        // The JTA transaction, if we're using one
        UserTransaction utx = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
            }
            else {
                // JTA not required so null user transaction
                utx = null;
            }
            
            
            /*
             * Update the database.
             */
            
            // Get the DB connection
            conn = this.ds.getConnection();
            conn.setAutoCommit(false);
            
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
            
            if ( utx != null ) {
                
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else if ( conn != null ) {
                
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        catch (Throwable t) {
            
            // Make sure we rollback the transaction
            if ( utx != null ) {
                
                // Use the JTA API to rollback the changes
                utx.rollback();
            }
            else if ( conn != null ) {
                
                // Use the connection to rollback the changes
                conn.rollback();
            }
            
            // Rethrow out to the caller
            throw t;
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
     * This method is called when a user presses the 'Edit' then 'Save' button
     * next to an employee row.
     * 
     * It will use the employee number in the bean to fill in an UPDATE statement
     * and update the associated record from the DB
     * 
     * @param employee - The employee object populated
     * @param useJta - use JTA to provide unit of work support, rather than CICS
     * 
     * @throws NamingException if the JNDI lookup of the UserTransaction fails
     * @throws SQLException if a JDBC error occurs
     * @throws CicsConditionException if a CICS error occurs
     * @throws NotSupportedException propagated from {@link UserTransaction#begin()}
     * @throws RollbackException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicMixedException propagated from {@link UserTransaction#commit()}
     * @throws HeuristicRollbackException propagated from {@link UserTransaction#commit()} 
     * @throws SystemException propagated from {@link UserTransaction#begin()} and {@link UserTransaction#commit()}
     */
    public void updateEmployee(Employee employee, final boolean useJta)
            throws NamingException, SQLException, CicsConditionException,
                   NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException, SystemException 
    {
        // Instances of JDBC objects
        Connection conn = null;
        PreparedStatement statement = null;
        
        // The JTA transaction, if we're using one
        UserTransaction utx = null;
        
        try {

            /*
             * Setup the transaction, based on whether JTA has been requested.
             */
            
            // Transactions are started implictly in CICS, explicitly in JTA
            if ( useJta ) {
                // Get a new user transaction for this piece of work and start it
                utx = (UserTransaction) InitialContext.doLookup("java:comp/UserTransaction");
                utx.begin();
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
            conn = this.ds.getConnection();
            conn.setAutoCommit(false);

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
           
            
            if ( utx != null ) {
                
                // Use the JTA API to commit the changes
                utx.commit();
            }
            else if ( conn != null ) {
                
                // Use the connection to commit the changes
                conn.commit();
            }
        }
        catch (Throwable t) {
            
            // Make sure we rollback the transaction
            if ( utx != null ) {
                
                // Use the JTA API to rollback the changes
                utx.rollback();
            }
            else if ( conn != null ) {
                
                // Use the connection to rollback the changes
                conn.rollback();
            }
            
            // Rethrow out to the caller
            throw t;
        }
        finally {
            
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
