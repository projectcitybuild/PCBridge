package com.projectcitybuild.pcbridge.paper.support.kotlin

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TrieTest {
    @Test
    fun `inserts and finds exact match`() {
        val trie = Trie().apply {
            insert("test")
            insert("foo bar")
        }
        assertTrue(trie.containsExact("test"))
        assertFalse(trie.containsExact("tes"))
        assertFalse(trie.containsExact("testing"))

        assertTrue(trie.containsExact("foo bar"))
        assertFalse(trie.containsExact("foobar"))
        assertFalse(trie.containsExact("foo bars"))
        assertFalse(trie.containsExact("foos bar"))
    }

    @Test
    fun `can insert word with same prefix as another`() {
        val trie = Trie().apply {
            insert("testing")
            insert("test")
        }
        assertTrue(trie.containsExact("testing"))
        assertTrue(trie.containsExact("test"))
    }

    @Test
    fun `inserts and returns prefix matches`() {
        val trie = Trie().apply {
            insert("test")
            insert("testing")
            insert("tester")
            insert("foo")
        }
        assertListsEquivalent(trie.matchingPrefix("t"), listOf(
            "test",
            "testing",
            "tester",
        ))
        assertListsEquivalent(trie.matchingPrefix("tes"), listOf(
            "test",
            "testing",
            "tester",
        ))
        assertListsEquivalent(trie.matchingPrefix("test"), listOf(
            "test",
            "testing",
            "tester",
        ))
        assertListsEquivalent(trie.matchingPrefix("testi"), listOf(
            "testing",
        ))
    }

    @Test
    fun `deletes a word`() {
        val word = "test"
        val trie = Trie()

        trie.insert(word)
        assertTrue(trie.containsExact(word))

        trie.remove(word)
        assertFalse(trie.containsExact(word))
    }

    @Test
    fun `deletes a word without affecting related words`() {
        val trie = Trie().apply {
            insert("test")
            insert("testing")
        }

        assertTrue(trie.containsExact("test"))
        assertTrue(trie.containsExact("testing"))

        trie.remove("test")
        assertFalse(trie.containsExact("test"))
        assertTrue(trie.containsExact("testing"))
    }
}

private fun <T: Comparable<T>> assertListsEquivalent(expected: List<T>?, actual: List<T>?) {
    assertTrue(
        isEqualIgnoreOrder(expected, actual),
        "Expected $expected, but actual was $actual",
    )
}

private fun <T: Comparable<T>> isEqualIgnoreOrder(lhs: List<T>?, rhs: List<T>?): Boolean {
    if (lhs == rhs) {
        return true
    }
    if (lhs == null || rhs == null || lhs.size != rhs.size) {
        return false
    }
    return lhs.sorted().toList() == rhs.sorted().toList()
}