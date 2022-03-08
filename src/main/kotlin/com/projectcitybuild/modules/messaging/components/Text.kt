package com.projectcitybuild.modules.messaging.components

import com.projectcitybuild.modules.messaging.MessageBuilder

class Text: MessageBuilder.Token() {

    sealed class Token {
        data class Regular(
            var text: String? = null,
            var isItalic: Boolean = false,
            var isBold: Boolean = false,
            var color: Color = Color.WHITE,
        ): Token() {
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

            fun color(color: Color, string: String? = null): Regular {
                return apply {
                    text = text ?: string
                    this.color = color
                }
            }
        }

        data class URL(val string: String): Token()
    }

    private val tokens: MutableList<Token> = mutableListOf()

    fun italic(string: String? = null): Token.Regular {
        return Token.Regular().italic(string).also {
            tokens.add(it)
        }
    }

    fun bold(string: String? = null): Token.Regular {
        return Token.Regular().bold(string).also {
            tokens.add(it)
        }
    }

    fun color(color: Color, string: String? = null): Token.Regular {
        return Token.Regular().color(color, string).also {
            tokens.add(it)
        }
    }

    fun url(string: String) {
        tokens.add(Token.URL(string))
    }
}