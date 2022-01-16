package com.projectcitybuild.modules.nameguesser

import javax.inject.Inject

class NameGuesser @Inject constructor() {
    fun guessClosest(string: String, collection: Collection<String>): String? {
        return guessClosest(string, collection) { it }
    }

    fun <T> guessClosest(string: String, collection: Collection<T>, comparison: (T) -> String): T? {
        val searchString = string.lowercase()

        var partialMatches: MutableList<String> = mutableListOf()

        collection.forEach { element ->
            val elementString = comparison(element).lowercase()
            if (elementString == searchString) {
                return element
            }
            if (elementString.startsWith(searchString)) {
                partialMatches.add(elementString)
            }
        }

        val closestMatchingString = partialMatches
            .sorted()
            .firstOrNull()
            ?: return null

        return collection
            .first { comparison(it).lowercase() == closestMatchingString }
    }
}