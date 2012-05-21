package org.jcopybook;

import java.util.HashMap;
import java.util.Map;

public class SignedNumericMappingTable {
    public static final Map<String, String> encodeMap = new HashMap<String, String>();
    public static final Map<String, String> decodeMap = new HashMap<String, String>();
    
    static {
        addPair("+0", "{");
        addPair("+1", "A");
        addPair("+2", "B");
        addPair("+3", "C");
        addPair("+4", "D");
        addPair("+5", "E");
        addPair("+6", "F");
        addPair("+7", "G");
        addPair("+8", "H");
        addPair("+9", "I");
        addPair("-0", "}");
        addPair("-1", "J");
        addPair("-2", "K");
        addPair("-3", "L");
        addPair("-4", "M");
        addPair("-5", "N");
        addPair("-6", "O");
        addPair("-7", "P");
        addPair("-8", "Q");
        addPair("-9", "R");
    }
    
    private static void addPair(String digit, String symb) {
        encodeMap.put(digit, symb);
        decodeMap.put(symb, digit);
    }
}
