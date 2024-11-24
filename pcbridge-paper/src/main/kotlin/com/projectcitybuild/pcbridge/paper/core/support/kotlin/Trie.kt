package com.projectcitybuild.pcbridge.paper.core.support.kotlin

class Trie {
    private class Node {
        val children = mutableMapOf<Char, Node>()
        var isWord = false
    }

    private val root = Node()

    fun insert(string: String) {
        var current = root
        for (char in string) {
            current.children.getOrPut(char) { Node() }
            current = current.children[char]!!
        }
        current.isWord = true
    }

    fun containsExact(string: String): Boolean {
        var current = root
        for (char in string) {
            current = current.children[char] ?: return false
        }
        return current.isWord
    }

    fun matchingPrefix(prefix: String): List<String> {
        // Navigate to the end of the prefix
        var start = root
        for (char in prefix) {
            start = start.children[char] ?: return emptyList()
        }

        // DFS from the end of the prefix to build out the list
        val matches = mutableListOf<String>()
        val stack = ArrayDeque<Pair<Node, String>>().apply { // ArrayDequeue has better push/pop performance than a Stack
            add(Pair(start, prefix))
        }
        while (stack.isNotEmpty()) {
            val (node, currentPrefix) = stack.removeLast()
            if (node.isWord) {
                matches.add(currentPrefix)
            }
            for ((char, childNode) in node.children) {
                stack.add(Pair(childNode, currentPrefix + char))
            }
        }
        return matches
    }

    fun remove(string: String) {
        // Just remove the "word" flag - not space efficient but good enough for now
        var current = root
        for (char in string) {
            current = current.children[char] ?: return
        }
        current.isWord = false
    }
}