import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.io.IOException;

@WebFilter(filterName = "TimeLogFilter", urlPatterns = "/api/movielist")
public class TimeLogFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        System.out.println("TimeLogFilter Established");
        // start logging time
        long startTime = System.nanoTime();

        // Call the next filter or target servlet in the chain
        chain.doFilter(request, response);

        // end the log
        long endTime = System.nanoTime();
        long elapsedTime = endTime - startTime;

        // Log the time taken in nanoseconds
        String contextPath = request.getServletContext().getRealPath("/");
        String logFilePath = contextPath + "timelog";
        System.out.println("Writing TS log to: " + logFilePath);
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        try (FileWriter writer = new FileWriter(logFile, true)) {
            System.out.println("TS log: " + elapsedTime + "ns");
            writer.write(" TS " + elapsedTime + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
