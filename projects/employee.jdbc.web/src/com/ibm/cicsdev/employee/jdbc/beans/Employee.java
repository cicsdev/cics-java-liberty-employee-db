/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2018 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.employee.jdbc.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * A bean class used to store all of the data fields associated with the
 * sample Db2 EMP table.
 * 
 * The fields will be populated by reading on-screen values, or from the
 * database, before being used as the basis of displays or SQL commands.
 *
 * @author Michael Jones
 * 
 */
public class Employee implements Serializable
{
    private static final long serialVersionUID = -5094938829089545370L;
    
    private String empNo;

    private Date birthDate;

    private BigDecimal bonus;

    private BigDecimal comm;

    private short edLevel;

    private String firstName;

    private Date hireDate;

    private String job;

    private String lastName;

    private String midInit;

    private String phoneNo;

    private BigDecimal salary;

    private String gender;

    private boolean canEdit;
    
    private boolean canDelete;
    
    public Employee() {
        this.canEdit = false;
        this.canDelete = false;
    }
    
    public String getEmpNo() {
        return this.empNo;
    }

    public void setEmpNo(String empNo) {
        this.empNo = empNo;
    }

    public Date getBirthDate() {
        return this.birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public BigDecimal getBonus() {
        return this.bonus;
    }

    public void setBonus(BigDecimal bonus) {
        this.bonus = bonus;
    }

    public BigDecimal getComm() {
        return this.comm;
    }

    public void setComm(BigDecimal comm) {
        this.comm = comm;
    }

    public short getEdLevel() {
        return this.edLevel;
    }

    public void setEdLevel(short edLevel) {
        this.edLevel = edLevel;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Date getHireDate() {
        return this.hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public String getJob() {
        return this.job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMidInit() {
        return this.midInit;
    }

    public void setMidInit(String midInit) {
        this.midInit = midInit;
    }

    public String getPhoneNo() {
        return this.phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public BigDecimal getSalary() {
        return this.salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getGender() {
        return this.gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public boolean isCanEdit() {
        return this.canEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.canEdit = canEdit;
    }
    
    public boolean isCanDelete() {
        return this.canDelete;
    }

    public void setCanDelete(boolean canDel) {
        this.canDelete = canDel;
    }
}