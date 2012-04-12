/*************************************************************
 * This file is part of CB2XML.  
 * See the file "LICENSE" for copyright information and the
 * terms and conditions for copying, distribution and
 * modification of CB2XML.
 *************************************************************
 */

package net.sf.cb2xml;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.cb2xml.sablecc.analysis.DepthFirstAdapter;
import net.sf.cb2xml.sablecc.node.*;
import net.sf.cb2xml.sablecc.parser.Parser;
import net.sf.cb2xml.util.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Main logic for translating the parse tree of SableCC into XML.
 * Currently the XML element and attribute names are hardcoded.
 * 
 * All the inA* methods are fired when the corresponding node is "visited".
 * Each node name corresponds to each "production" etc. within the grammar file.
 *
 * The "tree walking" approach generates the XML DOM in a very inituitive manner.
 *
 * @author Peter Thomas
 *
 * @version .91a Bruce Martin
 * <p>
 * <ol compact>
 * <li>Change ---... to ===... in comments
 * <li>Increment Position when initial level>01
 * <li>Convert Picture to uppercase (unlike java, Cobol is not case sensitive)
 * <li>Added storage-length (actual field length in bytes) based on Mainframe
 *     sizes
 * </ul>
 * @version .92a Jean-Francois Gagnon
 * <p>
 * <ol compact>
 * <li>Implemented Bruce Martin changes in version 0.92
 * <li>Added a sign-position=("trailing"|"leading") attribute
 * <li>Fixed the Sign Separate size computation (changes the storage length only)
 * <li>Made the CopyBookAnalyzer class constructor public
 * </ul>
 */

public class CopyBookAnalyzer extends DepthFirstAdapter {

	public CopyBookAnalyzer(String copyBookName, Parser parser) {
		this.copyBookName = copyBookName;
		this.parser = parser;
	}
	
	private Parser parser;
	private String copyBookName;
	private Document document;
	private Item prevItem, curItem;	

	// our internal representation of a copybook "item" node
	class Item {
		int level;
		Element element;
		// constructor
		Item(int level, String name) {
			this.level = level;
			element = document.createElement("item");
			element.setAttribute("level", new DecimalFormat("00").format(level));		
			element.setAttribute("name", name);			
		}
		// default constructor
		Item() {			
		}
	}
	
	// getter for XML document
	public Document getDocument() {
		return document;
	}	

	// enter copybook, set up XML DOM and root element	
	public void inARecordDescription(ARecordDescription node) {
	    document = XmlUtils.getNewXmlDocument();
	    Element root = document.createElement("copybook");
	    root.setAttribute("filename", copyBookName);
	    document.appendChild(root);
	}
	
	// exit root element, save XML as file
	public void outARecordDescription(ARecordDescription node) {
        Element el;
        int lastPos = 1;
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			org.w3c.dom.Node testNode = nodeList.item(i);
			if (testNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                el = (Element) nodeList.item(i);

                if ("01".equals(el.getAttribute("level"))) {
                    postProcessNode(el, 1);
                } else {
                    lastPos += postProcessNode(el, lastPos);
                }
			}
		}
	}

	// check for comments before these Tokens and add to XML
	public void caseTNumberNot88(TNumberNot88 node) {		
		checkForComments(node);
	}
	
	public void caseTNumber88(TNumber88 node) {
		checkForComments(node);
	}
	
	public void checkForComments(Token node) {
		List list = (List) parser.ignoredTokens.getIn(node);
		if (list != null) {
			Iterator i = list.iterator();
			while (i.hasNext()) {
				String s = i.next().toString().trim();
				if (s.length() > 1) {
                    curItem.element.getParentNode().insertBefore(
                            document.createComment(correctForMinus(s)),
                            curItem.element);
				}
			}
		}		
	}


    /**
     * Replace '-' chars with '=' to aviod invalid XML comments
     *
     * @param s input string Comment
     * @return corrected comment
     */
    private String correctForMinus(String s) {
        int start = s.indexOf("--");
        if (start >= 0){
            int i=start;
            StringBuffer buf = new StringBuffer(s);

            while (i < s.length() && buf.charAt(i) == '-') {
                buf.replace(i, i+1, "=");
                i += 1;
            }
            s = buf.toString();
        }

        return s;
    }


    // main elementary item
	
	// enter item, set up Item object	
	public void inAItem(AItem node) {
		int level = Integer.parseInt(node.getNumberNot88().toString().trim());
		String name = node.getDataNameOrFiller().toString().trim();
		curItem = new Item(level, name);
		if (level <= 77) {
			if (prevItem == null) {
				document.getDocumentElement().appendChild(curItem.element);
			} else if (curItem.level > prevItem.level) {
				prevItem.element.appendChild(curItem.element);
			} else if (curItem.level == prevItem.level) {
				prevItem.element.getParentNode().appendChild(curItem.element);
			} else if (curItem.level < prevItem.level) {
				Element tempElement = prevItem.element;
				while (true) {
					tempElement = (Element) tempElement.getParentNode();
					String tempLevel = tempElement.getAttribute("level");
					if ("".equals(tempLevel)) { 
						// we reached the root / document element!
						// start of a separate record structure, append to root as top level
						tempElement.appendChild(curItem.element);
						break;
					}
					int tempLevelNumber = Integer.parseInt(tempLevel);
					if (tempLevelNumber == curItem.level){ // sibling
						tempElement.getParentNode().appendChild(curItem.element);
						break;
					} else if (tempLevelNumber < curItem.level) {
						tempElement.appendChild(curItem.element);						
						break;
					}
				}
			}
			prevItem = curItem;
		}		
	}
	
	public void inARedefinesClause(ARedefinesClause node) {
		String dataName = node.getDataName().getText();
		//curItem.element.setAttribute("redefines", getJavaName(dataName));
		curItem.element.setAttribute("redefines", dataName);
	}

	public void inAFixedOccursFixedOrVariable(AFixedOccursFixedOrVariable node) {
		curItem.element.setAttribute("occurs", node.getNumber().toString().trim());
	}

	public void inAVariableOccursFixedOrVariable(
			AVariableOccursFixedOrVariable node) {
		curItem.element.setAttribute("occurs", node.getNumber().toString().trim());
		curItem.element.setAttribute("depending-on", node.getDataName().getText());
	}

	public void inAOccursTo(AOccursTo node) {
		curItem.element.setAttribute("occurs-min", node.getNumber().toString()
				.trim());
	}

	//============================= PICTURE CLAUSE ===================================
	
	public void inAPictureClause(APictureClause node) {
		String characterString = removeChars(node.getCharacterString().toString()," ");
		curItem.element.setAttribute("picture", characterString);
		if (characterString.charAt(0) == 'S') {
			curItem.element.setAttribute("signed", "true");
			characterString = characterString.substring(1);
		}
		int displayLength = 0, storageLength = 0;
            /* change "length" to "display-length" - bm  ??*/
		if (curItem.element.hasAttribute("display-length")) {
			displayLength = Integer.parseInt(curItem.element.getAttribute("display-length"));
		}
		if (curItem.element.hasAttribute("storage-length")) {
			storageLength = Integer.parseInt(curItem.element.getAttribute("storage-length"));
		}
		int decimalPos = -1;
		boolean isNumeric = false;
		boolean isFirstCurrencySymbol = true;
        String ucCharacterString = characterString.toUpperCase();
		for (int i = 0; i < characterString.length(); i++) {
			char c = ucCharacterString.charAt(i);
			switch (c) {
			case 'A':
			case 'B':
			case 'E':
				storageLength++;
			case 'G':
			case 'N':
				storageLength++;
				displayLength++;
				break;
			//==========================================
			case '.':
				displayLength++;
			case 'V':
				isNumeric = true;
				decimalPos = displayLength;
				break;
			//==========================================
			case 'P':
				if (characterString.charAt(0) == 'P') {
					decimalPos = 0;
				}
				isNumeric = true;
				displayLength++;
				break;
			//==========================================
			case '$':
				if (isFirstCurrencySymbol) {
					isFirstCurrencySymbol = false;
					isNumeric = true;
				} else {
					displayLength++;
				}
				break;
			//==========================================
			case 'C': // CR
			case 'D': // DR
				i++;  // skip R
			case 'Z':
			case '9':
			case '0':
			case '+':
			case '-':
			case '*':				
				isNumeric = true;
			case '/':
			case ',':			
			case 'X':
				displayLength++;
				break;
			case '(':
				int endParenPos = characterString.indexOf(')', i + 1);
				int count = Integer.parseInt(characterString.substring(i + 1,
						endParenPos));
				i = endParenPos;
				displayLength = displayLength + count - 1;
			}
		}

        setLength(curItem.element, displayLength);
		//curItem.element.setAttribute("display-length", displayLength + "");
		//curItem.element.setAttribute("bytes", bytes + "");
		if (decimalPos != -1) {
			curItem.element.setAttribute("scale", displayLength - decimalPos + "");
			if (characterString.indexOf('.') != -1) {
				curItem.element.setAttribute("insert-decimal-point", "true");
			}
		}
		if (isNumeric) {
			curItem.element.setAttribute("numeric", "true");
		}
	}

	public void inASignClause(ASignClause node) {
		if (node.getSeparateCharacter() != null) {
			curItem.element.setAttribute("sign-separate", "true");
			// No need to change the display length for the sign clause
			// As for the storage length, it is only computed in one place. JFG
			//int length = 1, bytes = 1;
			//if (curItem.element.hasAttribute("display-length")) {
			//	length = Integer.parseInt(curItem.element.getAttribute("display-length"))
			//			+ length;
			//}
			//curItem.element.setAttribute("display-length", length + "");
			//if (curItem.element.hasAttribute("bytes")) {
			//	bytes = Integer.parseInt(curItem.element.getAttribute("bytes"))
			//			+ bytes;
			//}
			// curItem.element.setAttribute("bytes", bytes + "");
		}
	}

	// Added the processing to capture the sign position JFG 
    public void inALeadingLeadingOrTrailing(ALeadingLeadingOrTrailing node)
    {
        curItem.element.setAttribute("sign-position", "leading");
    }

    public void inATrailimngLeadingOrTrailing(ALeadingLeadingOrTrailing node)
    {
        curItem.element.setAttribute("sign-position", "trailing");
    }

	//======================= USAGE CLAUSE ==========================
	
	public void inABinaryUsagePhrase(ABinaryUsagePhrase node) {
		curItem.element.setAttribute("usage", "binary");
	}

	public void inACompUsagePhrase(ACompUsagePhrase node) {
		curItem.element.setAttribute("usage", "computational");
	}

	public void inAComp1UsagePhrase(AComp1UsagePhrase node) {
		curItem.element.setAttribute("usage", "computational-1");
	}

	public void inAComp2UsagePhrase(AComp2UsagePhrase node) {
		curItem.element.setAttribute("usage", "computational-2");
	}

	public void inAComp3UsagePhrase(AComp3UsagePhrase node) {
		curItem.element.setAttribute("usage", "computational-3");
	}

	public void inAComp4UsagePhrase(AComp4UsagePhrase node) {
		curItem.element.setAttribute("usage", "computational-4");
	}
	
	public void inAComp5UsagePhrase(AComp5UsagePhrase node) {
		curItem.element.setAttribute("usage", "computational-5");
	}

	public void inADisplayUsagePhrase(ADisplayUsagePhrase node) {
		curItem.element.setAttribute("usage", "display");
	}

	public void inADisplay1UsagePhrase(ADisplay1UsagePhrase node) {
		curItem.element.setAttribute("usage", "display-1");
	}

	public void inAIndexUsagePhrase(AIndexUsagePhrase node) {
		curItem.element.setAttribute("usage", "index");
	}

	public void inANationalUsagePhrase(ANationalUsagePhrase node) {
		curItem.element.setAttribute("usage", "national");
	}

	public void inAObjectReferencePhrase(AObjectReferencePhrase node) {
		curItem.element.setAttribute("object-reference", node.getDataName().getText());
	}
	
	public void inAPackedDecimalUsagePhrase(APackedDecimalUsagePhrase node) {
		curItem.element.setAttribute("usage", "packed-decimal");
	}

	public void inAPointerUsagePhrase(APointerUsagePhrase node) {
		curItem.element.setAttribute("usage", "pointer");
	}

	public void inAProcedurePointerUsagePhrase(AProcedurePointerUsagePhrase node) {
		curItem.element.setAttribute("usage", "procedure-pointer");
	}
	
	public void inAFunctionPointerUsagePhrase(AFunctionPointerUsagePhrase node) {
		curItem.element.setAttribute("usage", "function-pointer");
	}
	
	//	======================= 88 / VALUE CLAUSE ==========================
	
	public void caseTZeros(TZeros node) {
		node.setText("zeros");
	}
	
	public void caseTSpaces(TSpaces node) {
		node.setText("spaces");
	}
	
	public void caseTHighValues(THighValues node) {
		node.setText("high-values");
	}
	
	public void caseTLowValues(TLowValues node) {
		node.setText("low-values");
	}
	
	public void caseTQuotes(TQuotes node) {
		node.setText("quotes");
	}
	
	public void caseTNulls(TNulls node) {
		node.setText("nulls");
	}
	
	public void caseTAlphanumericLiteral(TAlphanumericLiteral node) {
		String nodeText = node.getText();
		if (nodeText.indexOf("\"") != -1) {
			node.setText(removeChars(nodeText, "\""));
		} else {
			node.setText(removeChars(nodeText, "'"));
		}
	}
	
	public void outAValueClause(AValueClause node) {
		curItem.element.setAttribute("value", node.getLiteral().toString().trim());
	}
	
	// 88 LEVEL CONDITION NODE
	public void inAValueItem(AValueItem node) {
		String name = node.getDataName().getText();
		curItem = new Item();
		curItem.element = document.createElement("condition");
		// curItem.element.setAttribute("level", "88");
		curItem.element.setAttribute("name", name);
		prevItem.element.appendChild(curItem.element);
	}
	
	public void outASingleLiteralSequence(ASingleLiteralSequence node) {
		if (node.getAll() != null) {
			curItem.element.setAttribute("all", "true");
		}
		Element element = document.createElement("condition");
		element.setAttribute("value", node.getLiteral().toString().trim());
		curItem.element.appendChild(element);		
	}
	
	public void outASequenceLiteralSequence(ASequenceLiteralSequence node) {
		Element element = document.createElement("condition");
		element.setAttribute("value", node.getLiteral().toString().trim());
		curItem.element.appendChild(element);			
	}

	public void outAThroughSingleLiteralSequence(AThroughSingleLiteralSequence node) {
		Element element = document.createElement("condition");
		element.setAttribute("value", node.getFrom().toString().trim());
		element.setAttribute("through", node.getTo().toString().trim());
		curItem.element.appendChild(element);			
	}

	public void outAThroughSequenceLiteralSequence(AThroughSequenceLiteralSequence node) {
		Element element = document.createElement("condition");
		element.setAttribute("value", node.getFrom().toString().trim());
		element.setAttribute("through", node.getTo().toString().trim());
		curItem.element.appendChild(element);			
	}	
	
	//===============================================================================

	private String removeChars(String s, String charToRemove) {
		StringTokenizer st = new StringTokenizer(s, charToRemove, false);
		StringBuffer b = new StringBuffer();
		while (st.hasMoreElements()) {
			b.append(st.nextElement());
		}
		return b.toString();
	}

	/**
	 * This is for DOM post-processing of the XML before saving to resolve the field lengths
	 * of each node and also calculate the start position of the data field in the
	 * raw copybook buffer (mainframe equivalent)
	 * recursive traversal.  note that REDEFINES declarations are taken care of
	 * as well as the OCCURS declarations
	 */

	private int postProcessNode(Element element, int startPos) {
        int actualLength = 0;
        int displayLength = 0;
		if (element.hasAttribute("redefines")) {
			String redefinedName = element.getAttribute("redefines");
			Element redefinedElement = null;
			// NodeList nodeList = ((Element) element.getParentNode()).getElementsByTagName("item");
			NodeList nodeList = document.getDocumentElement().getElementsByTagName("item");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Element testElement = (Element) nodeList.item(i);
				if (testElement.getAttribute("name").equals(redefinedName)) {
					redefinedElement = testElement;
					break;
				}
			}
			startPos = Integer.parseInt(redefinedElement.getAttribute("position"));
			redefinedElement.setAttribute("redefined", "true");
		}
		element.setAttribute("position", startPos + "");
		if (element.hasAttribute("display-length")) {
			displayLength = Integer.parseInt(element.getAttribute("display-length"));			
		} else {
			NodeList nodeList = element.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {			
				org.w3c.dom.Node testNode = nodeList.item(i);			
				if (testNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
					Element childElement = (Element) testNode;
					if (!childElement.getTagName().equals("condition")) {
						int childElementLength = postProcessNode(childElement, startPos);			
						startPos += childElementLength;
						displayLength += childElementLength;
					}
				}
			}
			//element.setAttribute("display-length", displayLength + "");
		}
        actualLength = setLength(element, displayLength);
		if (element.hasAttribute("occurs")) {
			actualLength *= Integer.parseInt(element.getAttribute("occurs"));	
		}
		if (element.hasAttribute("redefines")) {
			actualLength = 0;	
		}		
		return actualLength;
	}


    /**
     * Assigning display and actual length to current element
     *
     */
    private int setLength(Element element, int displayLength) {
        int storageLength = displayLength;

        if (element.hasAttribute("sign-separate")) {
            storageLength += 1; 
        }

        if (element.hasAttribute("usage")) {
            String usage = element.getAttribute("usage");

            if ("computational-3".equals(usage)) {
                storageLength = (displayLength) / 2 + 1;
            }

            if ("computational".equals(usage) || "computational-5".equals(usage)) {
                storageLength = 8;
                if (displayLength < 5) {
                    storageLength = 2 ;
                } else if (displayLength < 10) {
                    storageLength = 4 ;
                }
            }
        }

        element.setAttribute("display-length", displayLength + "");
        element.setAttribute("storage-length", storageLength + "");
        return storageLength;
    }


}