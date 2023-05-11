import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class MainParser {
    Document dom;

    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each movie element and create a Movie object
        parseDocument();

        // iterate through the list and print the data
        //printData();

    }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("util/XMLParser/mains243.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        // get the document root Element
        Element documentElement = dom.getDocumentElement(); //the root is <movies>

        // get a nodelist of movie Elements, parse each into Movie object
        NodeList directorFilmsList = documentElement.getElementsByTagName("directorfilms");
        NodeList filmsList = documentElement.getElementsByTagName("films");
        for (int i = 0; i < directorFilmsList.getLength(); i++) {

            try {
                Element directorFilms = (Element) directorFilmsList.item(i);
                Element director = (Element) directorFilms.getElementsByTagName("director").item(0);
                Element directorName = (Element) director.getElementsByTagName("dirname").item(0);
                System.out.println(directorName.getFirstChild().getNodeValue());
                Element films = (Element) directorFilms.getElementsByTagName("films").item(0);
                parseMovie(films);
            } catch (Exception e) {
                System.out.println("this is an error");
                e.printStackTrace();
            }
//            // get the directorfilms element
//            Element element = (Element) directorFilmsList.item(i);
//
//            // get the directorfilms object
//            Movie movie = parseMovie(element);
//
//            // add it to list
//            movies.add(movie);
        }
    }

    private void parseMovie(Element films) {
        NodeList filmList = films.getElementsByTagName("film");
        for (int i = 0; i < filmList.getLength(); i++) {
            try {
                Element film = (Element) filmList.item(i);
                Element title = (Element) film.getElementsByTagName("t").item(0);
                System.out.println(" - " + title.getFirstChild().getNodeValue());
                Element year = (Element) film.getElementsByTagName("year").item(0);
                System.out.println("    - " + year.getFirstChild().getNodeValue());

            } catch (Exception e) {
                System.out.println("this is an error");
                e.printStackTrace();
            }
        }
    }
}
