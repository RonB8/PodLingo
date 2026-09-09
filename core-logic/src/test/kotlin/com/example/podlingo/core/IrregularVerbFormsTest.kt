package com.example.podlingo.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class IrregularVerbFormsTest {

    @Test
    fun `maps common irregular past tense and participle forms to their base verb`() {
        assertEquals("go", IrregularVerbForms.baseFormOf("gone"))
        assertEquals("go", IrregularVerbForms.baseFormOf("went"))
        assertEquals("see", IrregularVerbForms.baseFormOf("seen"))
        assertEquals("take", IrregularVerbForms.baseFormOf("took"))
        assertEquals("take", IrregularVerbForms.baseFormOf("taken"))
        assertEquals("write", IrregularVerbForms.baseFormOf("wrote"))
        assertEquals("write", IrregularVerbForms.baseFormOf("written"))
    }

    @Test
    fun `returns null for base forms and unrelated words`() {
        assertNull(IrregularVerbForms.baseFormOf("go"))
        assertNull(IrregularVerbForms.baseFormOf("cat"))
    }
}
