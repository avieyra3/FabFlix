import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;

public class ActorParser {
    private Document dom;
    private HashMap<String, Integer> dataForStarsTable = new HashMap<String, Integer>();

    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each movie element and create a Movie object
        parseDocument();
        dom = null;
        // iterate through the list and print the data
        //System.out.println(dataForStarsTable.toString());

    }

    public HashMap<String, Integer> getDataForStarsTable() { return dataForStarsTable; }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("src/util/XMLParser/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        Element actors = dom.getDocumentElement();

        NodeList actorList = actors.getElementsByTagName("actor");
        for (int i = 0; i < actorList.getLength(); i++) {
            Element actor = (Element) actorList.item(i);
            Element stageName = (Element) actor.getElementsByTagName("stagename").item(0);
            String actorName = null;
            Integer actorDOB = null;
            try {
                //System.out.println(stageName.getFirstChild().getNodeValue());
                actorName = stageName.getFirstChild().getNodeValue();
            } catch (Exception e) {
                //System.out.println("actor-name-missing");
                //e.printStackTrace();
            }

            Element dob = (Element) actor.getElementsByTagName("dob").item(0);
            try {
                //System.out.println(" - " + Integer.parseInt(dob.getFirstChild().getNodeValue()));
                actorDOB = Integer.parseInt(dob.getFirstChild().getNodeValue());
            } catch (Exception e) {
                //System.out.println(" - actor-dob-missing");
                //e.printStackTrace();
            }
            dataForStarsTable.put(actorName, actorDOB);
        }

    }
}