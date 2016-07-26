package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 6/29/16.
 */


import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX tag handler. The Class contains a list of RssItems which is being filled while the parser is working
 * @author ITCuties
 */
public class RssParseHandler extends DefaultHandler  {
    // List of items parsed
    private List<RssArticle> rssArticle;
    // We have a local reference to an object which is constructed while parser is working on an item tag
    // Used to reference item while parsing
    private RssArticle currentItem;
    // We have two indicators which are used to differentiate whether a tag title or link is being processed by the parser
    // Parsing title indicator
    private boolean parsingTitle;
    // Parsing link indicator
    private boolean parsingLink;
    // Parsing category indicator
    private boolean parsingCategory;

    private boolean parsingContent;
    //public String content;
    StringBuilder buf=new StringBuilder();


    public RssParseHandler() {
        rssArticle = new ArrayList();
    }
    // We have an access method which returns a list of items that are read from the RSS feed. This method will be called when parsing is done.
    public List<RssArticle> getItems() {
        return rssArticle;
    }
    // The StartElement method creates an empty RssItem object when an item start tag is being processed. When a title or link tag are being processed appropriate indicators are set to true.
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("item".equals(qName)) {
            currentItem = new RssArticle();
        } else if ("title".equals(qName)) {
            parsingTitle = true;
        } else if ("link".equals(qName)) {
            parsingLink = true;
        }else if("category".equals(qName)){
            parsingCategory=true;
        }
        buf.setLength(0);
    }
    // The EndElement method adds the  current RssItem to the list when a closing item tag is processed. It sets appropriate indicators to false -  when title and link closing tags are processed
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if ("item".equals(qName)) {
            rssArticle.add(currentItem);
            currentItem = null;
        }
        else if (parsingTitle) {
            if (currentItem != null)
                    currentItem.setTitle(buf.toString().trim());
            parsingTitle = false;
        } else if (parsingLink) {
            if (currentItem != null) {
                currentItem.setLink(buf.toString().trim());
            }
            parsingLink = false;
        }else if (parsingCategory) {
            if (currentItem != null) {
                currentItem.setCategory(buf.toString().trim());
            }
            parsingCategory = false;
        }
        buf.setLength(0);

    }
    // Characters method fills current RssItem object with data when title and link tag content is being processed
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (buf!=null) {
            for (int i=start; i<start+length; i++) {
                buf.append(ch[i]);
            }
        }
    }
}
