import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainParser {
    private Document dom;
    private HashMap<String, String> catCodes = new HashMap<String, String>();
    private ArrayList<ArrayList> dataForMoviesTable = new ArrayList<ArrayList>();
    private ArrayList<ArrayList> dataForGenresTable = new ArrayList<ArrayList>();

    public void run() {
        initCatCodes();

        // parse the xml file and get the dom object
        parseXmlFile();

        // get each movie element and create a Movie object
        parseDocument();

        // iterate through the list and print the data
        System.out.println(dataForMoviesTable.toString());
        System.out.println(dataForGenresTable.toString());
    }

    public ArrayList<ArrayList> getDataForMoviesTable() { return dataForMoviesTable; }
    public ArrayList<ArrayList> getDataForGenresTable() { return dataForGenresTable; }

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
            Element directorFilms = (Element) directorFilmsList.item(i);
            Element director = (Element) directorFilms.getElementsByTagName("director").item(0);
            Element directorName = (Element) director.getElementsByTagName("dirname").item(0);
            String strDirectorName = "";
            try {
                System.out.println(directorName.getFirstChild().getNodeValue());
                strDirectorName = directorName.getFirstChild().getNodeValue();
            } catch (Exception e) {
                System.out.println("director-name-empty");
                strDirectorName = null;
                e.printStackTrace();
            }
            Element films = (Element) directorFilms.getElementsByTagName("films").item(0);
            parseMovie(films, strDirectorName);
        }
    }

    private void parseMovie(Element films, String strDirectorName) {
        NodeList filmList = films.getElementsByTagName("film");
        for (int i = 0; i < filmList.getLength(); i++) {
            ArrayList<Object> rowMovieTable = new ArrayList<Object>();

            Element film = (Element) filmList.item(i);
            Element title = (Element) film.getElementsByTagName("t").item(0);
            try {
                if (title.getFirstChild().getNodeValue().equals("NKT"))
                    throw new Exception("Unknown film title");
                System.out.println(" - " + title.getFirstChild().getNodeValue());
                rowMovieTable.add(title.getFirstChild().getNodeValue());
            } catch (Exception e) {
                System.out.println(" - movie-title-empty");
                rowMovieTable.add(null);
                e.printStackTrace();
            }

            Element year = (Element) film.getElementsByTagName("year").item(0);
            try {
                System.out.println("    - " + Integer.parseInt(year.getFirstChild().getNodeValue()));
                rowMovieTable.add(Integer.parseInt(year.getFirstChild().getNodeValue()));
            } catch (Exception e) {
                System.out.println(" - movie-year-empty");
                rowMovieTable.add(null);
                e.printStackTrace();
            }

            Element cats = (Element) film.getElementsByTagName("cats").item(0);
            parseGenres(cats, (String) rowMovieTable.get(0));

            rowMovieTable.add(strDirectorName);
            dataForMoviesTable.add(rowMovieTable);
        }
    }

    private void parseGenres(Element cats, String movieTitle) {
        try {
            NodeList catList = cats.getElementsByTagName("cat");
            for (int i = 0; i < catList.getLength(); i++) {
                Element cat = (Element) catList.item(i);
                try {
                    ArrayList<String> rowGenresTable = new ArrayList<String>();
                    rowGenresTable.add(movieTitle);
                    if (catCodes.get(cat.getFirstChild().getNodeValue()) == null) {
                        System.out.println("       - WRONGCAT " + cat.getFirstChild().getNodeValue());
                        rowGenresTable.add(cat.getFirstChild().getNodeValue());
                    } else {
                        System.out.println("       - " + catCodes.get(cat.getFirstChild().getNodeValue()));
                        rowGenresTable.add(catCodes.get(cat.getFirstChild().getNodeValue()));
                    }
                    dataForGenresTable.add(rowGenresTable);
                } catch (Exception e) {
                    System.out.println("       - movie-genre-empty");
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println("       - movie-genres-empty");
            e.printStackTrace();
        }

    }

    private void initCatCodes() {
        catCodes.put("Susp", "Thriller");
        catCodes.put("CnR", "Cops and Robbers");
        catCodes.put("Dram", "Drama");
        catCodes.put("West", "Western");
        catCodes.put("Myst", "Mystery");
        catCodes.put("S.F.", "Sci-Fi");
        catCodes.put("Advt", "Adventure");
        catCodes.put("Horr", "Horror");
        catCodes.put("Romt", "Romance");
        catCodes.put("Comd", "Comedy");
        catCodes.put("Musc", "Musical");
        catCodes.put("Docu", "Documentary");
        catCodes.put("Porn", "Pornography (including soft)");
        catCodes.put("Noir", "Black");
        catCodes.put("BioP", "Biography");
        catCodes.put("TV", "TV Show");
        catCodes.put("TVs", "TV Series");
        catCodes.put("TVm", "TV Miniseries");
    }
}
