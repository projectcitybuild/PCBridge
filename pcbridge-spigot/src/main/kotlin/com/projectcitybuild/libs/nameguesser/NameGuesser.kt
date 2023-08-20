package com.projectcitybuild.libs.nameguesser

class NameGuesser {

    /**
     * Returns the closest matching String inside a collection.
     *
     * A String is considered to closely match if it matches exactly case-insensitive,
     * or it begins with the given string (also case-insensitive). If multiple strings
     * closely match, the nearest (alphabetically sorted) will be selected. For example,
     * given "a" and ["ab", "abc"], "ab" will be returned.
     *
     * @param string The string to match against
     * @param collection The collection to compare the string against
     * @return The closest matching string, if found
     */
    fun guessClosest(string: String, collection: Collection<String>): String? {
        return guessClosest(string, collection) { it }
    }

    /**
     * Returns the closest matching String inside a collection.
     *
     * A String is considered to closely match if it matches exactly case-insensitive,
     * or it begins with the given string (also case-insensitive). If multiple strings
     * closely match, the nearest (alphabetically sorted) will be selected. For example,
     * given "a" and ["ab", "abc"], "ab" will be returned.
     *
     * @param string The string to match against
     * @param collection The collection to compare the string against
     * @param comparison A closure to transform each item into a String. For example,
     *                   useful for converting Player objects into their display name
     * @return The closest matching string, if found
     */
    fun <T> guessClosest(string: String, collection: Collection<T>, comparison: (T) -> String): T? {
        val searchString = string.lowercase()

        val partialMatches: MutableList<String> = mutableListOf()

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
