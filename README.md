# CS 122B  
### Team Name: s23-122b-web_dev
### Team members: Khoi Pham and Alfonso Vieyra  
  
### I. Project 1:
- Khoi: Movie List Page, Single Star Page, AWS, Report/README  
- Alfonso: Database, Movie List Page, Single Movie Page, Jump Func, Extra Credit  
- Demo Video URL: https://youtu.be/fNJNbm95sd0

### II. Project 2:  
- Khoi: Main Page, Searching, Movie List first 3 genres & stars, Single Pages, Shopping Cart/Payment, Submission  
- Alfonso: Login Page, Browse, Sorting, Prev/Next, Jump Func, Beautify with CSS  
- Demo Video URL: https://youtu.be/MoIh1h9q5MA
- Substring Matching Design: only use LIKE '%%', which means the search term can be any substring and doesn't need to be at the beginning or ending. Also, no typo is allowed

### III. Project 3:
- Khoi: reCaptcha, HTTPS, Encrypt Password, Dashboard Login, Import XML
- Alfonso: PreparedStatement, Dashboard and Stored Procedure
- Demo Video URL: https://youtu.be/B_m9mxBzXlY
- PreparedStatement files: 
  - AddMovieListServlet.java,
    AddStarServlet.java,
    BrowseGenreServlet.java,
    EmployeeMetaDataServlet.java,
    LoginEmployeeServlet.java,
    LoginServlet.java,
    MovieListServlet.java,
    PaymentPageServlet.java,
    PurchaseConfirmServlet.java,
    ShoppingCartServlet.java,
    SingleMovieServlet.java,
    SingleStarServlet.java,
    UpdateSecurePasswordCustomer.java,
    UpdateSecurePasswordEmployee.java,
    DomParser.java
- XML parsing time optimization strategies:
  - Using batch insert to execute large amount of SQL statements all at once instead of one-by-one to reduce the overhead of communication
  - Check for movie and star duplications by query all movie titles and star names into memory and store them in a hash table for quick lookups 
- Parsing report:
  - ```
      Insert into movies table...  
      Inserted 11305 movies. Rejected 1 no titles, 252 duplicates.  
      Insert into stars table...  
      Inserted 16629 stars. Rejected 0 no names, 2053 duplicates.  
      Insert into genres table...  
      Inserted 124 genres. Rejected 1 nulls, 12 duplicates.  
      Insert into stars_in_movies table...  
      Inserted 52339 stars_in_movies. Rejected 0 nulls.  
      Insert into genres_in_movies table...  
      Inserted 9840 genres_in_movies. Rejected 7 nulls.

### IV. Project 4:
- Khoi: Android, Submission
- Alfonso: Full Text Search, Autocomplete, Fuzzy Search
- Demo Video URL: https://youtu.be/OBhlZz_Vsv4
- Fuzzy Search Implementation: Our Fuzzy Search implementation involved the application of the flamingo library which contained a set of user-defined-functions. In particular, we made use of the edth user defined function. This function utlized a dynamic programming algorithm to calculate the levenshtein distance between two terms with a maximum edit distance. To achieve a good scope of relevant results, we normalized each search result by taking the length of the query string and calculating the number of characters that could differ by no more than 30% of its length. For example, if a query string was the length of 10 characters, then any movie title being compared could differ by 3 characters. We performed a UNION of this search with mysql's full text search along with mysql's pattern matching to generate reliable results for the user.

### V. Project 5:
- Khoi:
- Alfonso:
- Demo Video URL: **coming soon**
- **[>>REPORT README<<](project5/README.md)**
- Instruction of deployment:
  1. Single instance:
     - Run createtable.sql, movie-data.sql, and stored-procedure.sql to populate the database
     - Run UpdateSecurePasswordCustomers.java and UpdateSecurePasswordEmployees.java to encrypt the password
     - Run DomParser.java to add more movies from XML files to the database
     - Build a war file of the webapp and deploy it on the Tomcat web server 
  2. Scaled instances:
     1. Load balancer:
        - Install Apache2 and related packages
        - Configure files to connect to the two backend instances
        - Modify security groups and open ports if needed
        - Enable sticky session
     2. Master:
        - Same as single instance, but additionally...
        - Create a MySQL user for remote access to the database
     3. Slave:
        - Same as single instance