import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;

public class ActorParser {
    private Document dom;
    private ArrayList<ArrayList> dataForStarsTable = new ArrayList<ArrayList>();

    public void run() {

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each movie element and create a Movie object
        parseDocument();

        // iterate through the list and print the data
        System.out.println(dataForStarsTable.toString());

    }

    public ArrayList<ArrayList> getDataForStarsTable() { return dataForStarsTable; }

    private void parseXmlFile() {
        // get the factory
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

        try {

            // using factory get an instance of document builder
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = documentBuilder.parse("util/XMLParser/actors63.xml");

        } catch (ParserConfigurationException | SAXException | IOException error) {
            error.printStackTrace();
        }
    }

    private void parseDocument() {
        Element actors = dom.getDocumentElement();

        NodeList actorList = actors.getElementsByTagName("actor");
        for (int i = 0; i < actorList.getLength(); i++) {
            ArrayList<Object> rowStarsTable = new ArrayList<Object>();
            Element actor = (Element) actorList.item(i);
            Element stageName = (Element) actor.getElementsByTagName("stagename").item(0);
            try {
                System.out.println(stageName.getFirstChild().getNodeValue());
                rowStarsTable.add(stageName.getFirstChild().getNodeValue());
            } catch (Exception e) {
                System.out.println("actor-name-missing");
                rowStarsTable.add(null);
                e.printStackTrace();
            }

            Element dob = (Element) actor.getElementsByTagName("dob").item(0);
            try {
                System.out.println(" - " + Integer.parseInt(dob.getFirstChild().getNodeValue()));
                rowStarsTable.add(Integer.parseInt(dob.getFirstChild().getNodeValue()));
            } catch (Exception e) {
                System.out.println(" - actor-dob-missing");
                rowStarsTable.add(null);
                e.printStackTrace();
            }
            dataForStarsTable.add(rowStarsTable);
        }

    }
}