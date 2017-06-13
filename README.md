## Client and REST Api for ADS-B Output

This project includes:

* a client that processes the BaseStation output from NoElec Smart SDR on port 30003
* A REST Api to:
    * Create API Keys - /key
    * Poll for all sightings since last poll - /poll/[key]
     
The project is a regular gradle project using:

* [MapDB](http://www.mapdb.org) to persist the polling state
* [Spark-java](http://sparkjava.com/) for the REST Api
* [Guava](https://github.com/google/guava/wiki/EventBusExplained)'s EventBus