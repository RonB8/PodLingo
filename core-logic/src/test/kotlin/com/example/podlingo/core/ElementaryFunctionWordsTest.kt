package com.example.podlingo.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ElementaryFunctionWordsTest {

    @Test
    fun `recognizes irregular be-do-have forms no suffix stemmer could derive`() {
        for (word in listOf("is", "am", "are", "was", "were", "been", "being", "has", "had", "does", "did")) {
            assertTrue(word, ElementaryFunctionWords.contains(word))
        }
    }

    @Test
    fun `recognizes the article missing from the Oxford list`() {
        assertTrue(ElementaryFunctionWords.contains("the"))
    }

    @Test
    fun `recognizes contracted spellings no stemmer or irregular-verb table could derive`() {
        for (word in listOf("wouldn't", "can't", "isn't", "don't", "won't", "i'll", "they've", "it's")) {
            assertTrue(word, ElementaryFunctionWords.contains(word))
        }
    }

    @Test
    fun `does not claim genuine content words`() {
        for (word in listOf("freedom", "religion", "consistent", "abrogate", "number")) {
            assertFalse(word, ElementaryFunctionWords.contains(word))
        }
    }
}
