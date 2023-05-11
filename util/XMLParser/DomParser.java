import java.util.ArrayList;
import java.util.List;

public class DomParser {

    List<Movie> moviesList = new ArrayList<>();



    public static void main(String[] args) {
        // create an instance
        MainParser mainParser = new MainParser();

        // call run example
        mainParser.run();

        ActorParser actorParser = new ActorParser();

        actorParser.run();
    }

}
