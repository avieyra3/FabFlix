- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
        - [context.xml](../WebContent/META-INF/context.xml),
          [web.xml](../WebContent/WEB-INF/web.xml),
          [AddMovieServlet.java](../src/AddMovieServlet.java), 
          [AddStarServlet.java](../src/AddStarServlet.java), 
          [BrowseGenreServlet.java](../src/BrowseGenreServlet.java), 
          [EmployeeMetaDataServlet.java](../src/EmployeeMetaDataServlet.java), 
          [LoginEmployeeServlet.java](../src/LoginEmployeeServlet.java),
          [LoginServlet.java](../src/LoginServlet.java)
          [MovieListServlet.java](../src/MovieListServlet.java)
          [MovieSuggestionServlet.java](../src/MovieSuggestionServlet.java)
          [PaymentPageServlet.java](../src/PaymentPageServlet.java)
          [PurchaseConfirmServlet.java](../src/PurchaseConfirmServlet.java)
          [ShoppingCartServlet.java](../src/ShoppingCartServlet.java)
          [SingleMovieServlet.java](../src/SingleMovieServlet.java)
          [SingleStarServlet.java](../src/SingleStarServlet.java)
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
        - In [context.xml](../WebContent/META-INF/context.xml), two data sources are defined with Tomcat pooling enabled
            using the `factory`, `maxTotal`, `maxIdle`, and `maxWaitMillis` tags
        - For each of the Java Servlets that need to talk to the database, instead of using DriverManager to talk to the 
            database which open and close a connection for every query, it uses DataSource which open a pool of 
            connections to the database so that each request can take a connection from the pool, run the query, and
            return the connection back to the pool
    - #### Explain how Connection Pooling works with two backend SQL.
        - Having two backend SQL or other different numbers of backend SQL doesn't affect how Connection Pooling work.
            For each data source defined as a certain SQL database instance, if Connection Pooling is enable, Tomcat will create a pool
            of open connections between the web server and that database instance. Anytime a query is executed, Tomcat 
            will take a connection from the pool, run the query using that connection, and return the connection back to
            the pool. This method eliminates the overhead of opening and closing connections from running queries.

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
        - Routed to master: 
          [AddMovieServlet.java](../src/AddMovieServlet.java),
          [AddStarServlet.java](../src/AddStarServlet.java),
          [PaymentPageServlet.java](../src/PaymentPageServlet.java)
        - Routed to the local MySQL database instance:
          [AddMovieServlet.java](../src/AddMovieServlet.java),
          [BrowseGenreServlet.java](../src/BrowseGenreServlet.java),
          [EmployeeMetaDataServlet.java](../src/EmployeeMetaDataServlet.java),
          [LoginEmployeeServlet.java](../src/LoginEmployeeServlet.java),
          [LoginServlet.java](../src/LoginServlet.java)
          [MovieListServlet.java](../src/MovieListServlet.java)
          [MovieSuggestionServlet.java](../src/MovieSuggestionServlet.java)
          [PaymentPageServlet.java](../src/PaymentPageServlet.java)
          [PurchaseConfirmServlet.java](../src/PurchaseConfirmServlet.java)
          [ShoppingCartServlet.java](../src/ShoppingCartServlet.java)
          [SingleMovieServlet.java](../src/SingleMovieServlet.java)
          [SingleStarServlet.java](../src/SingleStarServlet.java)
    - #### How read/write requests were routed to Master/Slave SQL?
        - For servlets that serve read requests, each has a DataSource object that connects to the MySQL instance in its 
          own AWS server instance, no matter if the MySQL instance is master or slave. 
        - For servlets that serve write requests, each has a DataSource object that connects to master MySQL instance.
        - For servlets that serve both types of requests, each has both types of DataSource object.

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.
        - ```
          sudo su
          cd /var/lib/tomcat10/webapps/s23-122b-web_dev
          cp timelog /home/ubuntu/s23-122b-web_dev/
          exit
          cd s23-122b-web_dev
          sudo python3 log_processing.py
          ```

- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot**                        | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis**                                                                                                                                                                                                                                                                       |
|------------------------------------------------|-----------------------------------------------------|----------------------------|-------------------------------------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Case 1: HTTP/1 thread                          | ![](img/nonscaled-1thrds-graph-http-pooling.png)    | 206                        | 48                                  | 46                        | The values show that the Average query time is greater than both the Average Search Servlet time and the JDBC time. This is expected given average query constitutes the whole entire execution. By contrast, the servlet and jdbc execution time is relative small by comparison. |
| Case 2: HTTP/10 threads                        | ![](img/nonscaled-10thrds-grapth-http-pooling.png)  | 480                        | 234                                 | 233                       | We can see that the execution rate is slower with 10 threads because of the additional workload serving multiple session onto a single server.                                                                                                                                     |
| Case 3: HTTPS/10 threads                       | ![](img/nonscaled-10thrds-grapth-https-pooling.png) | 528                        | 367                                 | 363                       | With https in the requests, we can see how the additional security measures (encryption - decryption) add to the load required to process a request. This is evident in comparison to http which was faster.                                                                       |
| Case 4: HTTP/10 threads/No connection pooling  | ![](img/nonscaled-10thrds-grapth-http.png)          | 465                        | 317                                 | 307                       | We get a roughly similar performance to 10 threads with the connection pooling because we still only have one server managing the requests.                                                                                                                                        |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot**                   | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](img/scaled-1thrds-graph-http.png)          | 207                        | 48                                  | 46                        | ??           |
| Case 2: HTTP/10 threads                        | ![](img/scaled-10thrds-graph-http-pooling.png) | 283                        | 108                                 | 107                       | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](img/)                                      | ??                         | ??                                  | ??                        | ??           |