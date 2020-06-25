package utils;

import junit.framework.TestCase;

public class TextUtilsTest extends TestCase {
    public void testCapitalize() {
        assertEquals("Test", TextUtils.capitalize("test"));
    }
}