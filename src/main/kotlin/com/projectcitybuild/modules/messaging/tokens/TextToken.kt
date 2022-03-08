package com.projectcitybuild.modules.messaging.tokens

import com.projectcitybuild.modules.messaging.MessageBuilderToken
import com.projectcitybuild.modules.messaging.components.Color
import com.projectcitybuild.modules.messaging.components.Decoration

class TextToken: MessageBuilderToken() {

    sealed class Part {
        data class Regular(
            var text: String? = null,
            var isItalic: Boolean = false,
            var isBold: Boolean = false,
            var isStrikethrough: Boolean = false,
            var color: Color = Color.WHITE,
            var decoration: Decoration? = null,
        ): Part() {
            fun italic(string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    isItalic = true
                }
            }

            fun bold(string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    isBold = true
                }
            }

            fun strikethrough(string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    isStrikethrough = true
                }
            }

            fun color(color: Color, string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    this.color = color
                }
            }

            fun decoration(decoration: Decoration, string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    this.decoration = decoration
                }
            }
        }

        data class URL(val string: String): Part()

        data class Command(val text: String, val command: String): Part()
    }

    val parts: MutableList<Part> = mutableListOf()

    fun italic(string: String? = null): Part.Regular {
        return Part.Regular().italic(string).also {
            parts.add(it)
        }
    }

    fun bold(string: String? = null): Part.Regular {
        return Part.Regular().bold(string).also {
            parts.add(it)
        }
    }

    fun strikethrough(string: String? = null): Part.Regular {
        return Part.Regular().strikethrough(string).also {
            parts.add(it)
        }
    }

    fun color(color: Color, string: String? = null): Part.Regular {
        return Part.Regular().color(color, string).also {
            parts.add(it)
        }
    }

    fun decoration(decoration: Decoration, string: String? = null): Part.Regular {
        return Part.Regular().decoration(decoration, string).also {
            parts.add(it)
        }
    }

    fun url(string: String) {
        parts.add(Part.URL(string))
    }

    fun command(text: String, command: String) {
        parts.add(Part.Command(text, command))
    }
}