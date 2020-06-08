# OpenTSDB

OpenTSDB  is a scalable timeseries database which stores and serves massive amounts of timeseries data without losing granularity

  - writes data with milli second precision
  - scales to millions of write per second
  - can **read** the data by  using HTTP API from it.

## Installation

There should be hbase pre-installed on the machine and some [pre-requisties](http://opentsdb.net/docs/build/html/installation.html).Please follow the below steps to install the OpenTSDB.

```bash
git clone git://github.com/OpenTSDB/opentsdb.git
cd opentsdb
./build.sh
```

After successful installation of the setup.Start the hbase standalone or the production cluster , if not already up and running.
## Running the OpenTSDB database

```bash
#start the hbase daemons
start-hbase.sh
#start the hbase shell
hbase shell
#Go to the respective OpenTSDB installation,and create the tables in the hbase by  executing
env COMPRESSION=NONE HBASE_HOME=path/to/hbase-0.94.X ./src/create_table.sh
#start the OpenTSDB Time Series Daemon(TSD)
./build/tsdb tsd
#we can the auto-metrics flag to true or can create the metric 
./build/tsdb mkmetric population.generated
```

At this point you can access the TSD's web interface through http://127.0.0.1:4242 (if it's running on your local machine).
 
Download the [datapop.txt](https://github.com/jayanthbaswa/OpentsDb/blob/master/datapop.txt)
We can ingest the many ways 
- With Cli
```bash
#to insert one record
put population.generated 1590534000 3 country=usa city=newyork
#./build/tsdb import [file location]
./build/tsdb import /home/jay/Documents/datapop.txt
```
- By executing the Java  Program [here](https://github.com/jayanthbaswa/OpentsDb/tree/master/Opentsdb/src/main/java/writing).



To visualize the data OpenTSDB comes with built-in GUI.A much nicer GUI can be found in the form of the open source [Grafana](https://grafana.com/)(I was having issues with built-in GUI so majorly using grafana, which functions almost similarly).
After successfully installing and  setting up the Grafana with the OpenTSDB data source.We can create and read with different scenarios as follows:

- Timeseries data with same metric but with different tags.
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/noneofmetrics.png)

- Timeseries datawith aggregating(sum) over each time series point(T0,T0+5m,T0+10m,..)
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/sumofmetric.png)

- Timeseries data with aggregating over country with data sampling of 10 minutes
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/sum_dwn_india_10m.png)

- Timeseries data with aggregating over country with data sampling of 1 day
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/sum_dwn_1day_usa.png)

- Timeseries data with aggregating over city=mumbai with data sampling of 1 day
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/sum_dwn_mumbai_1day.png)



OpenTSDB also comes with HTTP api to perform various queries,where some of them are below:
- Timeseries data with sum over each time series with HTTP API(T0,T0+5,...)
![](https://github.com/jayanthbaswa/OpentsDb/blob/master/examples/http.png)


For further reference,please check the OpenTSDB community [here](http://opentsdb.net/).

