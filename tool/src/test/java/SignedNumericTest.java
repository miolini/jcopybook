import net.sf.cb2xml.convert.MainframeToXml;
import net.sf.cb2xml.util.FileUtils;
import net.sf.cb2xml.util.XmlUtils;
import org.jcopybook.JCopybookException;
import org.jcopybook.Marshaller;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.FileNotFoundException;

public class SignedNumericTest extends Assert {
    private static final String FILE_LAYOUT = "signed-numeric-layout.xml";
    private static final String FILE_DATA_ASCII = "signed-numeric-data.txt";
    private static final String FILE_DATA_XML = "signed-numeric-data.xml";
    
    private Document layout;
    private Document dataXml;
    private String dataAscii;
    
    @Before
    public void init() throws FileNotFoundException {
        layout = loadLayout();
        dataXml = loadDataXml();
        dataAscii = loadDataAscii();
    }

    @Test
    public void ascii2xml() throws Exception {
        MainframeToXml m2xml = new MainframeToXml();
        Document resultXml = m2xml.convert(dataAscii, layout);
		NodeList nodeList = dataXml.getElementsByTagName("MY-COPYBOOK").item(0).getChildNodes();
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) continue;
			Node resultNode = resultXml.getElementsByTagName(node.getNodeName()).item(0);
			String valueData = node.getChildNodes().item(0).getNodeValue();
			String valueResult = resultNode.getChildNodes().item(0).getNodeValue();
			assertEquals(valueData, valueResult);
		}
    }

    @Test
    public void xml2ascii() throws JCopybookException {
        Marshaller marshaller = new Marshaller();
        marshaller.setLayout(layout);
        String resultAscii = marshaller.process(dataXml);
		assertEquals(dataAscii, resultAscii);
    }

    private Document loadDataXml() {
        return XmlUtils.fileToDom(FILE_DATA_XML);
    }

    private Document loadLayout() {
        return XmlUtils.fileToDom(FILE_LAYOUT);
    }

    private String loadDataAscii() throws FileNotFoundException {
        return FileUtils.readFile(FILE_DATA_ASCII).toString();
    }
}
