import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.Statement;
import org.jasypt.util.password.StrongPasswordEncryptor;



@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        PrintWriter out = response.getWriter();
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Login Connection established!\n");
            Statement statement = connection.createStatement();

            // perform the sql query to come up with a table with the emails and passwords
            String query = "SELECT email, password FROM customers WHERE email = " + '"' + username + '"';
            System.out.println(query);
            ResultSet result = statement.executeQuery(query);

            JsonObject responseJsonObject = new JsonObject();
            boolean hasResults = result.next();
            System.out.println(hasResults);
            boolean emailMatch = username.equals(result.getString("email"));
            System.out.println(emailMatch);
            boolean passwordMatch = new StrongPasswordEncryptor().checkPassword(password, result.getString("password"));
            System.out.println(passwordMatch);

            if (!hasResults || !emailMatch || !passwordMatch) {
                System.out.println("Login fails");
                // Login fail
                responseJsonObject.addProperty("status", "fail");
                // Log to localhost log
                request.getServletContext().log("Login failed");
                // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                responseJsonObject.addProperty("message", "username/password is incorrect.");

            } else {
                // Login success:
                System.out.println("Login success");
                // set this user into the session
                request.getSession().setAttribute("user", new User(username));
                responseJsonObject.addProperty("status", "success");
                responseJsonObject.addProperty("message", "success");

            }
            response.getWriter().write(responseJsonObject.toString());

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ERROR:", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}