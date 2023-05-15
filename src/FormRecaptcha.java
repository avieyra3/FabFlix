import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
//import java.sql.Statement;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("\n-------FormRecaptcha doGet EXECUTING");
        response.setContentType("application/json"); // Response mime type

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        PrintWriter out = response.getWriter();
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            jsonObject.addProperty("recaptcha-success", true);
            jsonArray.add(jsonObject);
        } catch (Exception e) {
            //recaptcha verification error
            jsonObject.addProperty("recaptcha-success", false);
            jsonArray.add(jsonObject);
            return;
        }

        System.out.println(jsonArray.toString());
        out.write(jsonArray.toString());
        out.close();

        System.out.println("-------FormRecaptcha doGet Done!\n");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
