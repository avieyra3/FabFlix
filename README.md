CS 122B  
Team Name: s23-122b-web_dev  
  
Team members: Khoi Pham and Alfonso Vieyra  
  
I. Project 1:
- Khoi: Movie List Page, Single Star Page, AWS, Report/README  
- Alfonso: Database, Movie List Page, Single Movie Page, Jump Func, Extra Credit  
- Demo Video URL: https://youtu.be/fNJNbm95sd0

II. Project 2:  
- Khoi: Main Page, Searching, Movie List first 3 genres & stars, Single Pages, Shopping Cart/Payment, Submission  
- Alfonso: Login Page, Browse, Sorting, Prev/Next, Jump Func, Beautify with CSS  
- Demo Video URL: https://youtu.be/MoIh1h9q5MA
- Substring Matching Design: only use LIKE '%%', which means the search term can be any substring and doesn't need to be at the beginning or ending. Also, no typo is allowed
  
III. Project 3:
- Khoi: reCaptcha, HTTPS, Encrypt Password, Dashboard Login, Import XML
- Alfonso: PreparedStatement, Dashboard and Stored Procedure
- Demo Video URL: **Coming Soon**
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