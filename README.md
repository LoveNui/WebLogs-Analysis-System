# HBase Actual Data Analysis System
  
## instructions

### 1. Database design

####LogData

- This table is used to store the data after data cleaning and transformation
- Database type: HBase
- Table Structure

  Rowkey|prop|
  ----|:------------------------------:|
  | rowkey | IP / BYTES / URL / DATES / METHOD / FYDM / BYTES|
- RowKey structure design description
> RowKey is divided into date + last three digits of website code + six digit ID
> Each part is described as follows:

  Field | Explanation | Example
----| ----- |----
Date |The date when the log file was generated (pure numbers, without spaces and -) | 20170808
Company code | The last three digits of the company code |200
ID | Six digits starting from 100000, used to uniquely mark data and align | 100001
> complete example
> 201708082001000000 means a request made by 200-point company on 2017-08-08

- Create table statement
> create "LogData", "prop"

-

#### LogAna
- This table is used to store the analyzed data
- Database type: HBase
- Table Structure 

RowKey | IP | URL | BYTES | MTHOD_STATE |REQ
-------|----|-----|-------|-------------|---
rowkey |IPSumVal IPTotalNum IPList |URLList MaxURL | BytesSecList BytesHourList / TotalBytes | MethodList StateList | ReqHourList ReqSecList ReqSum
- field description

Field | Explanation | Example
----| ----- |----
IPTotalNum| The total number of IPs, excluding duplicates | 100 means that 100 IPs visited the website that day
IPSumVal | Total number of IPs, including duplicates | 100 indicates that 100 IPs visit the website, and IPs can be repeated
IPList | Ranking of IP and corresponding visits, the structure is a JSON file converted from mutable.Map[String, Int] | {"190.1.1.1": 1000} means that the IP of 190.1.1.1 generated 1000 requests on the website in total)
URLList | The 10 most requested URLs, the structure is Json | {"test.aj":100, "test2.aj":90, ...}
MaxURL | The URL with the most requests (now the front end has given up using this field) |{"test.aj": 100}
BytesSecList | Statistical traffic generated per second, the unit is Byte, but converted to MB when the front-end display | {"2017-08-08 01:00:00":9000, "2017-08-08 01:00:00" : 500, ...}
BytesHourList | Count the traffic generated every hour in a day, the unit is Byte, but it will be converted to MB when displayed on the front end | {"08": 9000, "09": 500, ...}, 08 means within 8 o'clock to 9 o'clock generated traffic
TotalBytes | The total traffic size generated in one day, the unit is Byte, but it is converted to MB when displayed on the front end | 3000, indicating that the traffic of 3000b bytes is generated on that day
MethodList | Appeared request method statistics | {"POST":3446,"OPTIONS":5,"HEAD":4}
StateList | Appeared request state intermediate | {"501":8,"302":801,"404":1,"200":14738,"400":2,"405":4}
ReqHourList | Count the number of requests by hour | {"15":2350,"09":3503,"00":690,"11":1903}
ReqSecList | Count the number of requests by second | {"2017-08-08 10:44:08":1,"2017-08-08 09:45:05":4,"2017-08-08 10:06:58 ":4}
ReqSum | The total number of requests in a day | 1000, indicating that there are 1000 requests in the day

- RowKey structure design description
> RowKey is divided into date + last three digits of company code
> Each part is described as follows:

Field | Explanation | Example
----| ----- |----
Date |The date when the log file was generated (pure numbers, without spaces and -) | 20170808
Company code | The last three digits of the company code | 200, it should be noted that 000 means all website data of the day

> example:
20170808200 means all the data of Tianjin High Court on 2017-08-08
20170808000 means all courts at point 2017-08-08 all data

- Create table statement
> create "LogAna", "IP", "URL", "BYTES", "METHOD_STATE", "REQ"


### 2. Project code description
- This project is divided into three sub-projects, including data acquisition, data storage and display, and data offline analysis

#### data collection

- Project name: CollectTomcatLogs
- Function Description:   

> Collect tomcat logs under the specified path
> Upload to HDFS or FTP server after renaming the file
> Save the log to record whether the upload is successful
 
- Deployment instructions: Deploy on each server that needs to collect logs, specify the company code and log path in my.properties
- Configuration management: maven
- Main technologies: Java FTPClient, HDFS
- Test case description: mainly used to test whether the renamed file is normal
- File renaming: Add the court code before the localhost_XXXXX.txt file to distinguish the data of each company

#### Data storage and display
- Project name: RestoreData
- Function Description:

> Data preprocessing: including data analysis, cleaning and transformation
> Data storage: save the converted data in a List and insert them into the HBase database in batches
> Front-end display: display the analyzed data
> Data query: Query corresponding data according to various input conditions
- Development environment:
> JDK 8
> Hadoop 2.7
> Hbase 1.2
> tomcat 8
- Deployment instructions: Configure various data in my.properties, pay attention to the compatibility of JDK and Hadoop versions
- Configuration management: maven
- Main technology: Spring MVC / Hadoop / JSP
- Test case description:
> HbaseBatchInsertTest.java: for testing batch insertion
> HbaseConnectionTest.java: used to test whether the Hbase connection is normal
> ParseLogTest.java: for testing log parsing
> ListBean.java: Print all beans, used to cope with @Autowried failure
- Front end part:
> #### code section
> index.jsp: The page is loaded by default, and the data will be requested after loading, showing all the website data of the previous day
> index.js: used to process various requests and data analysis in index.jsp

> ----------
> queryData.jsp: Used to query the data of various websites, the input is date + website, multiple selection is supported
> queryData.js: used to process various requests and data analysis in queryData.jsp (to be completed)

> ---------
> dataGrid.jsp: display data in form of table (to be completed)

> --------
> myCharts.js: Use echarts to draw various charts (note that the initialization of dom is done externally)
> inputCheck.js: Check if the input is legal

>---------
> mystyle.css: Customize various styles
>####Third party library
> Bootstrap: mainly with its grid system
> Bootstrap-select: Implementation of multiple selection boxes
> BootstrapDatepickr: date input
> echarts: draw various charts
> jQuery: frame
> font-awesome: various small icons


#### Data offline analysis

- Project name: ScalaReadAndWrite
- Function Description:

> Offline analysis of various data, a total of 13 indicators, see the database table LogAna design for details

- Development environment:
> Scala 2.11
> Spark 1.52
> Hadoop 2.7
- Special Note:
> There are only two implementations of global variables in spark, broadcast variables or accumulators, this project uses accumulators
> When customizing the accumulator, it is very important to pay attention to the correct input and output types
> Be sure to implement all six overloaded functions
> An accumulator can only pass one kind of variable, which can be a complex object
> Failure to do so will invalidate the accumulator!
- Deployment instructions: None
- Configuration management: maven
- Main technology: Spark
- Description of project structure:
> Accumulator: accumulator, including various custom accumulators
> analysis: main analysis code
> DAO: parse the entity class and store it in HBase
> Entity: two entity classes
> util: various tools

#### 3. Project screenshot:

- Hbase database screenshot
![image](https://github.com/LoveNui/WebLogs-Analysis-System/blob/master/image/p2.png)

- Data display interface
![image](https://github.com/LoveNui/WebLogs-Analysis-System/blob/master/image/p1.png)

- Data display interface
![image](https://github.com/LoveNui/WebLogs-Analysis-System/blob/master/image/p3.png)