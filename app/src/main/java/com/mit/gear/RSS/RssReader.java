package com.mit.gear.RSS;

/**
 * Created by najlaalghofaily on 6/29/16.
 */
import java.lang.Exception;import java.lang.String;import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Class reads RSS data.
 * @author ITCuties
 */
public class RssReader {
    private String rssUrl;

    public RssReader(String rssUrl) {
        this.rssUrl = rssUrl;
    }
    /**
     * Get RSS items. This method will be called to get the parsing process result.
     * @return
     */
    public List<RssArticle> getItems() throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        RssParseHandler handler = new RssParseHandler();
        saxParser.parse(rssUrl, handler);
        // The result of the parsing process is being stored in the handler object
        return handler.getItems();
    }
}
