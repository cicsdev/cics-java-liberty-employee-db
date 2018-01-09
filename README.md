cics-java-liberty-employee
=====================

Sample web application that uses JDBC to access the sample Db2 EMP table.

## Repository structure

* [`projects/`](projects) - Eclipse web project and CICS bundle project suitable for importing into a CICS Explorer or z/OS Explorer environment. 
* [`etc/`](etc) - Liberty server configuration files and CICS RDO DFHCSDUP input.

## Project overview

* [`employee.jdbc.web`](projects/employee.jdbc.web) - Dynamic web project
* [`employee.jdbc.cicsbundle`](projects/employee.jdbc.cicsbundle) - CICS bundle project 

## Pre-requisites
* CICS TS V5.3 with [APAR PI67640](www.ibm.com/support/docview.wss?uid=swg1PI67640) and [APAR PI58375](http://www-01.ibm.com/support/docview.wss?uid=swg1PI58375), or CICS TS V5.4
* Java SE 7 or later on the z/OS system
* CICS Explorer V5.4 with the IBM CICS SDK for Java EE and Liberty feature installed [available here](https://developer.ibm.com/mainframe/products/downloads)
* IBM Db2 for z/OS 

## Configuration
The sample code can be deployed as a WAR file into a CICS Liberty JVM server. CICS Liberty can be configured to use either a local Db2 database with 
JDBC type 2 connectivity,  or a remote database with a JDBC type 4 connectivity. 


### To import the samples into Eclipse
1. Import the projects into CICS Explorer using **File -> Import -> General -> Existing projects into workspace**
1. Resolve the build path errors on the Dynamic web project using the following menu from the web project:
 **Build Path -> Configure Build Path -> Libraries -> Add Library -> CICS with Java EE and Liberty** and select the version of
 CICS TS for deployment (either CICS TS V5.3 or CICS TS V5.4)
1. Export the web project 

### To configure CICS Liberty for JDBC type 2 connectivity 
1. Update the CICS STEPLIB with the Db2 SDSNLOAD and SDSNLOD2 libraries
1. Configure CICS URIMAP, DB2CONN, DB2TRAN and DB2ENTRY resource definitions as described in
 [How you can define the CICS DB2 connection](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.4.0/configuring/databases/dfhtk2c.html)
1. Bind the Db2 plan that is specified in the CICS DB2CONN or DB2ENTRY definition with a PKLIST of NULLID.* 
1. Create a Liberty JVM server called DFHWLP as described in
 [4 easy steps](https://developer.ibm.com/cics/2015/06/04/starting-a-cics-liberty-jvm-server-in-4-easy-steps/)
1. Add the following Liberty features to the `featureManger` list in server.xml: `jsf-2.2`, `jndi-1.0`, `jdbc-4.1` 
1. Add a library definition to the Liberty server.xml that references the Db2 JCC libraries
1. Add a data source definition to the Liberty server.xml, a template server.xml is provided in [type-2-server.xml](etc/Liberty/type-2-server.xml) 


### To configure CICS Liberty for JDBC type 4 connectivity to Db2
1. Create a Liberty JVM server called DFHWLP as described in
 [4 easy steps](https://developer.ibm.com/cics/2015/06/04/starting-a-cics-liberty-jvm-server-in-4-easy-steps/)
1. Add the following Liberty features to the `featureManger` list in server.xml: `jsf-2.2`, `jndi-1.0`, `jdbc-4.1` 
1. Add a library definition to the Liberty server.xml that references the Db2 JCC libraries
1. Add a data source definition to the Liberty server.xml, a template server.xml is provided in [type-4-server.xml](etc/Liberty/type-4-server.xml) 

## Deploying the sample

To deploy the sample you will need to import the projects into Eclipse. 

To install the sample as a CICS bundle:

1. Export the CICS bundle project **employee.jdbc.cicsbundle** from Eclipse using the menu **Export Bundle Project to z/OS UNIX File System**.
1. Define and install a CICS BUNDLE defintion that references the zFS directory from step 1.

To install the sample through Liberty configuration:

1. Export the dynamic Web project **employee.jdbc.web** from Eclipse using the menu **File** > **Export** > **WAR file** > **Finish**.
1. Copy the exported WAR file in binary to zFS.
1. Add an application element to the Liberty server.xml configuration file that refernces the WAR file using [employee.xml](etc/Liberty/employee.xml) as a basis.

To define the DB2LOG CICS temporary storage queue as recoverable, you will need to create a CICS TSMODEL resource definition with the ```Recovery``` attribute set to ```Yes```.
A sample DFHCSDUP input file is provided in [DFHCSD.txt](etc/RDO/DFHCSD.txt) 

## Running the sample
The application is accessed with the following URL: [http://host:port/employee.jdbc.web/](http://host:port/employee.jdbc.web/)
and allows the user to perform create, read, update and delete operations on employees listed in the Db2 EMP table.


## Reference
*  CICS Knowledge Center [Configuring a Liberty JVM server](https://www.ibm.com/support/knowledgecenter/SSGMCP_5.4.0/configuring/java/config_jvmserver_liberty.html)
*  CICS Knowledge Center [Configuring a JVM server to support DB2](https://www.ibm.com/support/knowledgecenter/en/SSGMCP_5.4.0/applications/developing/database/dfhtk4b.html)

## License
This project is licensed under [Apache License Version 2.0](LICENSE).
