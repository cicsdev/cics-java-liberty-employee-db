/* Licensed Materials - Property of IBM                                   */
/*                                                                        */
/* SAMPLE                                                                 */
/*                                                                        */
/* (c) Copyright IBM Corp. 2017 All Rights Reserved                       */
/*                                                                        */
/* US Government Users Restricted Rights - Use, duplication or disclosure */
/* restricted by GSA ADP Schedule Contract with IBM Corp                  */
/*                                                                        */
package employee.jdbc.beans;

import java.io.Serializable;


/**
 * The persistent class for the DEPARTMENT database table.
 * 
 */
public class Department implements Serializable {

	private static final long serialVersionUID = 1L;
	
	public Department(String departmentNumber) {
		deptno = departmentNumber;
	}

	private String deptno;

	private String admrdept;

	private String deptname;

	private String location;

	private String mgrno;

	public Department() {
	}

	public String getDeptno() {
		return this.deptno;
	}

	public void setDeptno(String deptno) {
		this.deptno = deptno;
	}

	public String getAdmrdept() {
		return this.admrdept;
	}

	public void setAdmrdept(String admrdept) {
		this.admrdept = admrdept;
	}

	public String getDeptname() {
		return this.deptname;
	}

	public void setDeptname(String deptname) {
		this.deptname = deptname;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getMgrno() {
		return this.mgrno;
	}

	public void setMgrno(String mgrno) {
		this.mgrno = mgrno;
	}

}
