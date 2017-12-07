cics-java-liberty-employee
=====================

Sample Java EE web application demonstrating how to use JDBC and to control data base commits from a CICS Java application.

## Repository structure

* [`projects/`](projects) - Eclipse project suitable for importing into a CICS Explorer or z/OS Explorer environment. 
* [`etc/`](etc) - Liberty server configuration files

## Samples overview

* `employee.jdbc.web` - Dynamic web project. 

## Pre-requisites
* CICS TS V5.3 with APAR PI67640 and APAR PI58375, or CICS TS V5.4
* Java SE 7 or later on the z/OS system
* CICS Explorer V5.4 with the IBM CICS SDK for Java EE and Liberty feature installed [available here](https://developer.ibm.com/mainframe/products/downloads)
* IBM Db2 for z/OS 

## Configuration
The sample code can be deployed as a WAR file into a CICS Liberty JVM server. CICS Liberty can be configured to use either a local DB2 database with 
JDBC type 2 connectivity,  or a remote database with a JDBC type 4 connectivity. 


### To import the samples into Eclipse
1. Import the projects into CICS Explorer using **File -> Import -> General -> Existing projects into workspace**
1. Resolve the build path errors on the Dynamic web project using the following menu from the web project: **Build Path -> Configure Build Path -> Libraries -> Add Library -> CICS with Java EE and Liberty** and select the version of CICS TS for deployment (either CICS TS V5.3 or CICS TS V5.4)

### To configure CICS for JDBC type 2 connectivity to DB2
1. Create a Liberty JVM server as described in [4 easy steps](https://developer.ibm.com/cics/2015/06/04/starting-a-cics-liberty-jvm-server-in-4-easy-steps/)
1. Update the CICS STEPLIB with the DB2 SDSNLOAD and SDSNLOD2 libraries
1. Configure CICS DB2CONN, DB2TRAN and DB2ENTRY resource definitions as described in [How you can define the CICS DB2 connection](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.4.0/configuring/databases/dfhtk2c.html)
1. Bind the DB2 plan that is specified in the CICS DB2CONN or DB2ENTRY definition with a PKLIST of NULLID.* 
1. Add the following properties in the JVM profile to set the location of the DB2 drivers to allow CICS to automatically configure the default DataSource 

```
-Dcom.ibm.cics.jvmserver.wlp.autoconfigure=true
-Dcom.ibm.cics.jvmserver.wlp.jdbc.driver.location=/usr/lpp/db2v12/jdbc
```
Where  ```/usr/lpp/db2v12/jdbc``` is the location of the DB2 JDBC driver

An example Liberty server configuration of a DataSource with a type 2 connection is supplied in [etc/config/type-2-server.xml](etc/config/type-2-server.xml). Configuration with DataSource and a type 4 connection is in [etc/config/type-4-server.xml](etc/config/type-4-server.xml)

### To deploy the sample into a CICS region 

## Running the sample
* The servlet is accessed with the following URL:
[http://host:port/employee.jdbc.web/](http://host:port/employee.jdbc.web/)  


## Reference
*  CICS Knowledge Center [Configuring a Liberty JVM server](https://www.ibm.com/support/knowledgecenter/SSGMCP_5.4.0/configuring/java/config_jvmserver_liberty.html)
*  CICS Knowledge Center [Configuring a JVM server to support DB2](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.4.0/applications/developing/database/dfhtk4b.html)

## License
This project is licensed under [Apache License Version 2.0](LICENSE).
