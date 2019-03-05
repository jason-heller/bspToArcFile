/*
 ** 2012 Februar 24
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.bsplib.app;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.io.LineIterator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Application database SAX handler.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SourceAppHandler extends DefaultHandler {
    
    private static final Logger L = Logger.getLogger(SourceAppHandler.class.getName());
    
    private List<SourceApp> appList = new ArrayList<SourceApp>();
    private SourceApp currentApp;
    private String currentElement;
    private StringBuilder entitiesBuf = new StringBuilder(4096);
    
    @Override
    public void startDocument() throws SAXException {
        appList.clear();
        currentApp = null;
        currentElement = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentElement = qName;
        
        if (qName.equals("app")) {
            String name = attributes.getValue("name");
            String id = attributes.getValue("id");
            
            if (name == null) {
                L.warning("Ignoring app with missing attribute \"name\"");
            } else if (id == null) {
                L.warning("Ignoring app with missing attribute \"id\"");
            } else {
                currentApp = new SourceApp(name, Integer.valueOf(id));
            }
        }
        
        if (currentApp == null) {
            return;
        }
        
        if (qName.equals("version")) {
            String minVers = attributes.getValue("min");
            String maxVers = attributes.getValue("max");
            
            if (minVers != null) {
                currentApp.setVersionMin(Integer.valueOf(minVers));
            }
            if (maxVers != null) {
                currentApp.setVersionMax(Integer.valueOf(maxVers));
            }
        } else if (qName.equals("entities")) {
            String points = attributes.getValue("points");
            
            if (points != null) {
                currentApp.setPointsEntities(Float.valueOf(points));
            }
        } else if (qName.equals("files")) {
            String points = attributes.getValue("points");
            
            if (points != null) {
                currentApp.setPointsFilePattern(Float.valueOf(points));
            }
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("app")) {
            appList.add(currentApp);
            currentApp = null;
        }
        
        if (currentApp == null) {
            return;
        }
        
        if (qName.equals("entities")) {
            LineIterator it = new LineIterator(new StringReader(entitiesBuf.toString()));
            
            while (it.hasNext()) {
                String entity = it.next().trim();
                if (!entity.isEmpty()) {
                    currentApp.getEntities().add(entity);
                }
            }
            
            // clear buffer without re-allocating it
            entitiesBuf.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (currentApp == null) {
            return;
        }
        
        if (currentElement.equals("entities")) {
            entitiesBuf.append(ch, start, length);
        } else if (currentElement.equals("files")) {
            String content = new String(ch, start, length);
            if (!content.trim().isEmpty()) {
                currentApp.setFilePattern(content);
            }
        }
    }

    public List<SourceApp> getAppList() {
        return appList;
    }
}
