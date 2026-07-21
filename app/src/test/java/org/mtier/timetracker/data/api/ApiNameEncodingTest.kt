package org.mtier.timetracker.data.api

import org.junit.Assert.assertEquals
import org.junit.Test

class ApiNameEncodingTest {
    @Test
    fun `a space is percent-encoded on the second pass, not left as a literal plus`() {
        // once: "no+description" -> twice: the literal "+" itself gets re-encoded.
        assertEquals("no%2Bdescription", doubleEncodeName("no description"))
    }

    @Test
    fun `special characters are encoded twice`() {
        // "a/b" -> once: "a%2Fb" -> twice: "a%252Fb"
        assertEquals("a%252Fb", doubleEncodeName("a/b"))
    }

    @Test
    fun `unicode name survives round trip through double encoding`() {
        val encoded = doubleEncodeName("café ☕")
        val onceDecoded = java.net.URLDecoder.decode(encoded, "UTF-8")
        val twiceDecoded = java.net.URLDecoder.decode(onceDecoded, "UTF-8")
        assertEquals("café ☕", twiceDecoded)
    }
}
