# REST Endpoint Data Streaming Demonstration

The purpose of this project is to demonstrate streaming a large dataset over HTTP.

## Running the Application

This application relies on a database you can configure and run with the files in the
[db](../db) directory. The [db-interaction](../db-interaction) project has code that will 
load gobs of data this application can stream.

The main function for this application is defined in the
[WebStreamApplication](./src/main/java/dev/darrencodes/pcloadletter/webstream/WebStreamApplication.java)
class.

To stream the data, you can run the following command: `curl "localhost:8080"`. This will stream
a large dataset in a JSON format. Alternatively, you can run the command 
`curl "localhost:8080?format=csv"` to stream the same dataset in a CSV format.

## What it Shows

When the amount of data you want to send across the wire is large, loading it all into memory and
then writing it is not the best way to get this done. It's better to write the data as you produce
it and limit what is in memory. This application uses the facilities of Spring's JdcbTemplate 
and the Jackson library in a way people may be unfamiliar with to hook up the querying of a dataset
and writing it in various formats to an HTTP response.