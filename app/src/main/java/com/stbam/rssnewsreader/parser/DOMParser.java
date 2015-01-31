package com.stbam.rssnewsreader.parser;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Created by Esteban on 07-Oct-14.
 */

// Codigo basado en el tutorial de http://techiedreams.com/android-simple-rss-reader
// y obviamente adaptado a nuestras necesidades
public class DOMParser {

    private RSSFeed _feed = new RSSFeed();
    public RSSFeed parseXml(String xml, String nombre)
    {

        // se definen diferentes urls porque para leer articulos de TheVerge.com y Polygon.com
        // es necesario un metodo distinto
        String theverge_burner = "http://feeds.feedburner.com/theverge/MJyr";
        String polygon_burner = "http://feeds.feedburner.com/polygon/yhac";
        String theverge = "http://www.theverge.com/rss/frontpage";
        String polygon = "http://www.polygon.com/rss/index.xml";

        URL url = null;
        //System.out.println(xml);
        try {
            url = new URL(xml);
        } catch (MalformedURLException e1) {
            //e1.printStackTrace();
        }

        if (xml.equals(theverge) || xml.equals(theverge_burner) || xml.equals(polygon) || xml.equals(polygon_burner))
            leerPolygonYTheVerge(url, nombre);
        else if (nombre.equals("LifeHacker") || nombre.equals("PhoneArena") || nombre.equals("US Google News") || nombre.equals("ABC News") || nombre.equals("Android Central"))
            leerOtrosFeed(url, nombre);
        else
            leerTipoRaro(url, nombre);


        // Return the final feed once all the Items are added to the RSSFeed
        // Object(_feed).
        return _feed;
    }

    private static String getValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        Node node = (Node) nodeList.item(0);
        return node.getNodeValue();
    }

    // este y el siguiete metodo sirven para parsear el documento leido desde el URL
    // encuentra cada uno de los elementos (noticias) y saca por aparte
    // cada uno de sus atributos para asi asignarlos a una instancia del objeto RSSItem
    private void leerOtrosFeed(URL url, String nombre)
    {
        try {
            // Create required instances
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse the xml
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            // Get all <item> tags.
            NodeList nl = doc.getElementsByTagName("item");
            int length = nl.getLength();

            for (int i = 0; i < length; i++) {
                Node currentNode = nl.item(i);
                RSSItem _item = new RSSItem();

                NodeList nchild = currentNode.getChildNodes();
                int clength = nchild.getLength();

                // Get the required elements from each Item
                for (int j = 0; j < clength; j = j + 1) {

                    Node thisNode = nchild.item(j);
                    String theString = null;
                    String nodeName = thisNode.getNodeName();

                    _item.set_source_page(nombre);

                    theString = nchild.item(j).getFirstChild().getNodeValue();

                    if (theString != null) {
                        if ("title".equals(nodeName)) {
                            // Node name is equals to 'title' so set the Node
                            // value to the Title in the RSSItem.
                            _item.setTitle(theString);
                        }

                        else if ("description".equals(nodeName)) {
                            _item.setDescription(theString);

                            // Parse the html description to get the image url
                            String html = theString;

                            org.jsoup.nodes.Document docHtml = Jsoup.parse(html);
                            Elements imgEle = docHtml.select("img");

                            if (nombre.equals("PhoneArena") || nombre.equals("US Google News"))
                                _item.setImage("http:" + imgEle.attr("src"));
                            else
                                _item.setImage(imgEle.attr("src"));
                        }

                        else if ("link".equals(nodeName)) {
                            _item.setLink(theString);
                        }

                        else if ("pubDate".equals(nodeName)) {

                            // We replace the plus and zero's in the date with
                            // empty string
                            String formatedDate = theString.replace(" +0000",
                                    "");
                            _item.setDate(formatedDate);
                        }

                    }
                }

                // add item to the list
                _feed.addItem(_item);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // este y el siguiete metodo sirven para parsear el documento leido desde el URL
    // encuentra cada uno de los elementos (noticias) y saca por aparte
    // cada uno de sus atributos para asi asignarlos a una instancia del objeto RSSItem
    private void leerPolygonYTheVerge(URL url, String nombre)
    {
        try {
            // Create required instances
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse the xml
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            // Get all <item> tags.
            NodeList nl = doc.getElementsByTagName("entry");
            int largo = nl.getLength();


            for (int i = 0; i < largo; i++)
            {
                Node entrada = nl.item(i);

                if (entrada.getNodeType() == Node.ELEMENT_NODE) {

                    Element element2 = (Element) entrada;

                    RSSItem entry = new RSSItem();

                    entry.setTitle(getValue("title", element2));
                    entry.setDescription(getValue("content", element2));
                    entry.setDate(getValue("published", element2));
                    entry.setLink(getValue("id", element2));

                    // esto sirve para sacar el link de la imagen

                    String html = entry.getDescription();
                    org.jsoup.nodes.Document docHtml = Jsoup
                            .parse(html);
                    Elements imgEle = docHtml.select("img");
                    entry.setImage("http:" + imgEle.attr("src"));
                    //System.out.println(entry.getImage());
                    entry.set_source_page(nombre);


                    // se agrega el item recien creado a la lista para devolverla
                    _feed.addItem(entry);

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // este y el siguiete metodo sirven para parsear el documento leido desde el URL
    // encuentra cada uno de los elementos (noticias) y saca por aparte
    // cada uno de sus atributos para asi asignarlos a una instancia del objeto RSSItem
    private void leerTipoRaro(URL url, String nombre)
    {
        //System.out.println("Parseando el feed source de nombre: " + nombre);
        try {
            // Create required instances
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse the xml
            Document doc = db.parse(new InputSource(url.openStream()));

            // Get all <item> tags.
            NodeList nl = doc.getElementsByTagName("item");
            int largo = nl.getLength();

            for (int i = 0; i < largo; i++)
            {
                Node entrada = nl.item(i);

                if (entrada.getNodeType() == Node.ELEMENT_NODE) {

                    Element element2 = (Element) entrada;

                    RSSItem entry = new RSSItem();

                    entry.setTitle(getValue("title", element2));

                    if (nombre.equals("FoodSpin") || nombre.equals("All Music") || nombre.equals("New Release Tuesday"))
                        entry.setDescription(getValue("description", element2));
                    else
                        entry.setDescription(getValue("content:encoded", element2));

                    entry.setLink(getValue("link", element2));

                    // esto sirve para sacar el link de la imagen

                    String html = entry.getDescription();
                    org.jsoup.nodes.Document docHtml = Jsoup
                            .parse(html);
                    Elements imgEle = docHtml.select("img");
                    entry.setImage(imgEle.attr("src"));
                    //System.out.println(entry.getImage());
                    entry.set_source_page(nombre);


                    // se agrega el item recien creado a la lista para devolverla
                    _feed.addItem(entry);

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
