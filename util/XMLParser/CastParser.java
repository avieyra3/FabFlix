import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class CastParser {
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
            dom = documentBuilder.parse("util/XMLParser/casts124.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        Element casts = dom.getDocumentElement();

        NodeList dirFilmsList = casts.getElementsByTagName("dirfilms");
        for (int i = 0; i < dirFilmsList.getLength(); i++) {
            try {
                NodeList filmcList = ((Element) dirFilmsList.item(i)).getElementsByTagName("filmc");
                parseFilmCasts(filmcList);
            } catch (Exception e) {
                System.out.println("there is an error");
                e.printStackTrace();
            }
        }
    }
    private void parseFilmCasts(NodeList filmcList) {

        for (int i = 0; i < filmcList.getLength(); i++) {
            NodeList castList = ((Element) filmcList.item(i)).getElementsByTagName("m");
            for (int j = 0; j < castList.getLength(); j++) {
                try {
                    Element cast = (Element) castList.item(j);
                    Element movieTitle = (Element) cast.getElementsByTagName("t").item(0);
                    Element actorName = (Element) cast.getElementsByTagName("a").item(0);
                    System.out.println(movieTitle.getFirstChild().getNodeValue() + " - " + actorName.getFirstChild().getNodeValue());
                } catch (Exception e) {
                    System.out.println("there is an error");
                    e.printStackTrace();
                }
            }
        }
    }
}
