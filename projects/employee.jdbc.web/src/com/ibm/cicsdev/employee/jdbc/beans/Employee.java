/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2017 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package com.ibm.cicsdev.employee.jdbc.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * A bean class used to store all of the data fields
 * associated with the EMPLOYEE table in the sample
 * Db2 table.
 * 
 * The fields will be populated by reading on screen values
 * or from the database, before being used as the basis of displays
 * or SQL commands
 *
 * @author Michael Jones
 * 
 */
public class Employee implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String empno;

	private Date birthdate;

	private BigDecimal bonus;

	private BigDecimal comm;

	private short edlevel;

	private String firstname;

	private Date hiredate;

	private String job;

	private String lastname;

	private String midinit;

	private String phoneno;

	private BigDecimal salary;

	private String sex;

	private boolean canEdit;
	
	private boolean canDel;
	
	public Employee() {
		canEdit = false;
		canDel = false;
	}
	
	public String getEmpno() {
		return this.empno;
	}

	public void setEmpno(String empno) {
		this.empno = empno;
	}

	public Date getBirthdate() {
		return this.birthdate;
	}

	public void setBirthdate(Date birthdate) {
		this.birthdate = birthdate;
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

	public short getEdlevel() {
		return this.edlevel;
	}

	public void setEdlevel(short edlevel) {
		this.edlevel = edlevel;
	}

	public String getFirstname() {
		return this.firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public Date getHiredate() {
		return this.hiredate;
	}

	public void setHiredate(Date hiredate) {
		this.hiredate = hiredate;
	}

	public String getJob() {
		return this.job;
	}

	public void setJob(String job) {
		this.job = job;
	}

	public String getLastname() {
		return this.lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getMidinit() {
		return this.midinit;
	}

	public void setMidinit(String midinit) {
		this.midinit = midinit;
	}

	public String getPhoneno() {
		return this.phoneno;
	}

	public void setPhoneno(String phoneno) {
		this.phoneno = phoneno;
	}

	public BigDecimal getSalary() {
		return this.salary;
	}

	public void setSalary(BigDecimal salary) {
		this.salary = salary;
	}

	public String getSex() {
		return this.sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public boolean isCanEdit() {
	      return canEdit;
	}

	public void setCanEdit(boolean canEdit) {
	      this.canEdit = canEdit;
	}
	
	public boolean isCanDel() {
	      return canDel;
	}

	public void setCanDel(boolean canDel) {
	      this.canDel = canDel;
	}

}