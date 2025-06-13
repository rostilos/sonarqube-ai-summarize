package org.perpectiveteam.plugins.aisummarize.classloader;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProviderTypeTest {

    @Test
    void testFromName() {
        assertEquals(ProviderType.CLASS_REFERENCE, ProviderType.fromName("CLASS_REFERENCE"));
        assertEquals(ProviderType.REFLECTIVE, ProviderType.fromName("REFLECTIVE"));
        assertThrows(IllegalStateException.class, () -> ProviderType.fromName("INVALID"));
    }

    @Test
    void testValues() {
        ProviderType[] values = ProviderType.values();
        assertEquals(2, values.length);
        assertArrayEquals(new ProviderType[]{ProviderType.CLASS_REFERENCE, ProviderType.REFLECTIVE}, values);
    }
}
