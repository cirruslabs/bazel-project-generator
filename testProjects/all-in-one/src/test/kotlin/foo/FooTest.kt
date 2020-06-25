package foo

import org.junit.Assert.assertEquals
import org.junit.Test

class FooTest {
    @Test
    fun testName() {
        assertEquals("Name", Foo.fooName.name)
    }
}