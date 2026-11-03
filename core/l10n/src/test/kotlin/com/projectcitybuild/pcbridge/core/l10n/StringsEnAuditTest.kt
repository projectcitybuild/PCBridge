package com.projectcitybuild.pcbridge.core.l10n

import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtEscapeStringTemplateEntry
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPrefixExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

private val PLACEHOLDER_NAME_PATTERN = Regex("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")

private val SUPPORTED_PLACEHOLDER_CONSTRUCTORS = setOf(
    "Placeholder.unparsed",
    "Placeholder.component",
    "Placeholder.parsed",
    "Placeholder.styling",
    "TagResolver.resolver",
)

private val MINIMESSAGE_TAG_NAMES = setOf(
    "black",
    "dark_blue",
    "dark_green",
    "dark_aqua",
    "dark_red",
    "dark_purple",
    "gold",
    "gray",
    "dark_gray",
    "blue",
    "green",
    "aqua",
    "red",
    "light_purple",
    "yellow",
    "white",
    "reset",
    "escape",
    "newline",
    "br",
    "bold",
    "b",
    "italic",
    "i",
    "underlined",
    "u",
    "strikethrough",
    "st",
    "obfuscated",
    "obf",
    "rainbow",
    "gradient",
    "transition",
    "font",
    "key",
    "lang",
    "translate",
    "selector",
    "score",
    "nbt",
    "insertion",
    "click",
    "hover",
    "pride",
    "shadow_color",
    "shift",
    "space",
    "sprite",
    "head",
    "entity",
    "block",
    "item",
    "rel",
)
private val COLOR_TAG_NAMES = setOf(
    "black",
    "dark_blue",
    "dark_green",
    "dark_aqua",
    "dark_red",
    "dark_purple",
    "gold",
    "gray",
    "dark_gray",
    "blue",
    "green",
    "aqua",
    "red",
    "light_purple",
    "yellow",
    "white",
)

private const val INTERPOLATION_MARKER = '\u0001'
private const val UNRESOLVED_START_MARKER = '\u0002'
private const val UNRESOLVED_END_MARKER = '\u0003'
private const val DYNAMIC_TAG = "\u0001Hex.DYNAMIC\u0001"

private enum class FindingKind {
    VIOLATION,
    REVIEW,
}

private data class AuditFinding(
    val rule: String,
    val severity: FindingKind,
    val line: Int,
    val column: Int,
    val sourceExpression: String,
    val message: String,
    val remediation: String,
) {
    fun format(fileName: String): String {
        val label = if (severity == FindingKind.VIOLATION) rule else "REVIEW:$rule"
        return "$fileName:$line:$column [$label] $message\n  source: $sourceExpression\n  fix: $remediation"
    }
}

private data class AuditReport(
    val violations: List<AuditFinding>,
    val reviewWarnings: List<AuditFinding>,
)

private data class TemplateAnalysis(
    val text: String,
    val unresolvedExpressions: List<String> = emptyList(),
)

private class StringsEnAuditor(
    private val psiFactory: KtPsiFactory,
) {
    fun audit(source: String): AuditReport {
        val file = psiFactory.createFile("StringsEn.kt", source)
        val findings = mutableListOf<AuditFinding>()

        auditHardcodedColors(file, findings)
        val calls = file.collectDescendantsOfType<KtCallExpression>()
        calls.filter { it.isMiniMessageCall() && !it.isInsideMiniMessageHelper() }.forEach { mmCall ->
            auditMiniMessageCall(file, mmCall, findings)
        }

        return AuditReport(
            violations = findings.filter { it.severity == FindingKind.VIOLATION },
            reviewWarnings = findings.filter { it.severity == FindingKind.REVIEW },
        )
    }

    private fun auditMiniMessageCall(
        file: KtFile,
        mmCall: KtCallExpression,
        findings: MutableList<AuditFinding>,
    ) {
        val templateExpression = mmCall.valueArguments.firstOrNull()?.getArgumentExpression()
        val analysis = templateExpression?.let(::templateText) ?: unresolvedTemplate("missing template expression")
        val template = analysis.text

        val tagScan = parseTags(template)
        val tags = tagScan.tags
        tagScan.warnings.forEach { warning ->
            findings += finding(
                file,
                mmCall,
                FindingKind.REVIEW,
                "MINIMESSAGE_SYNTAX_REVIEW",
                warning,
            )
        }
        tags.filter { !it.isClosing && !it.isSelfClosing && isHexColor(it.name) }.forEach { tag ->
            findings += finding(
                file,
                mmCall,
                FindingKind.VIOLATION,
                "HARDCODED_PALETTE_COLOR",
                "MiniMessage color tag '${tag.name}' bypasses the Hex palette.",
            )
        }
        auditInterpolations(file, mmCall, template, tags, analysis.unresolvedExpressions, findings)

        val resolvers = mmCall.valueArguments.drop(1)
            .mapNotNull { argument ->
                val resolverCall = argument.getArgumentExpression()?.let(::directCallExpression)
                if (resolverCall == null) {
                    findings += finding(
                        file,
                        argument,
                        FindingKind.REVIEW,
                        "DYNAMIC_RESOLVER_EXPRESSION",
                        "Resolver argument is not a direct, statically recognized resolver call.",
                    )
                    return@mapNotNull null
                }
                val resolverType = resolverType(resolverCall)
                if (resolverType == null) {
                    findings += finding(
                        file,
                        resolverCall,
                        FindingKind.REVIEW,
                        "UNSUPPORTED_RESOLVER_EXPRESSION",
                        "Resolver argument is a call that this audit cannot identify; review its placeholder names and parsing behavior.",
                    )
                    return@mapNotNull null
                }
                if (resolverType !in SUPPORTED_PLACEHOLDER_CONSTRUCTORS) {
                    findings += finding(
                        file,
                        resolverCall,
                        FindingKind.REVIEW,
                        "UNSUPPORTED_PLACEHOLDER_CONSTRUCTOR",
                        "Resolver constructor '$resolverType' is not recognized by this audit; review its parsing and formatting behavior.",
                    )
                }
                val name = resolverCall.valueArguments
                    .firstOrNull()
                    ?.getArgumentExpression()
                    ?.let(::literalString)
                if (name == null) {
                    findings += finding(
                        file,
                        resolverCall,
                        FindingKind.REVIEW,
                        "DYNAMIC_PLACEHOLDER_NAME",
                        "The placeholder name is not a string literal; review it manually.",
                    )
                    return@mapNotNull null
                }
                Resolver(resolverCall, resolverType, name)
            }

        resolvers.groupBy { it.name }.filterValues { it.size > 1 }.forEach { (name, duplicates) ->
            duplicates.drop(1).forEach { duplicate ->
                findings += finding(
                    file,
                    duplicate.call,
                    FindingKind.VIOLATION,
                    "DUPLICATE_PLACEHOLDER",
                    "Placeholder '$name' is declared more than once for this template.",
                )
            }
        }

        val resolverNames = resolvers.mapTo(mutableSetOf()) { it.name }
        tags
            .filter {
                !it.isClosing &&
                    !it.isSelfClosing &&
                    !it.inAttribute &&
                    PLACEHOLDER_NAME_PATTERN.matches(it.name) &&
                    it.name !in resolverNames &&
                    it.name !in MINIMESSAGE_TAG_NAMES
            }.forEach { tag ->
                findings += finding(
                    file,
                    mmCall,
                    FindingKind.REVIEW,
                    "UNDECLARED_TAG",
                    "Tag '${tag.name}' has no Placeholder resolver; confirm that it is an intentional MiniMessage extension.",
                )
            }

        resolvers.forEach { resolver ->
            if (!PLACEHOLDER_NAME_PATTERN.matches(resolver.name)) {
                findings += finding(
                    file,
                    resolver.call,
                    FindingKind.VIOLATION,
                    "INVALID_PLACEHOLDER_NAME",
                    "Placeholder '${resolver.name}' must use lowercase words separated by single hyphens.",
                )
            }

            val uses = tags.filter { !it.isClosing && !it.isSelfClosing && it.name == resolver.name }
            if (uses.isEmpty()) {
                findings += finding(
                    file,
                    resolver.call,
                    FindingKind.VIOLATION,
                    "PLACEHOLDER_NOT_IN_TEMPLATE",
                    "Placeholder '${resolver.name}' is not used by its MiniMessage template.",
                )
                return@forEach
            }

            if (resolver.type == "Placeholder.unparsed") {
                uses
                    .withIndex()
                    .filter { (_, use) -> use.inAttribute || !use.dynamicActive }
                    .forEach { (index, _) ->
                        findings += finding(
                            file,
                            resolver.call,
                            FindingKind.VIOLATION,
                            "SCALAR_NOT_DYNAMIC",
                            "Scalar placeholder '${resolver.name}' usage ${index + 1} must be explicitly wrapped in Hex.DYNAMIC.",
                        )
                    }
            }

            if (
                resolver.type == "Placeholder.component" &&
                uses.any { !it.inAttribute && it.dynamicActive }
            ) {
                findings += finding(
                    file,
                    resolver.call,
                    FindingKind.REVIEW,
                    "DYNAMIC_COMPONENT_STYLE",
                    "Component placeholder '${resolver.name}' is inside Hex.DYNAMIC; verify that its own formatting is preserved.",
                )
            }

            if (resolver.type == "Placeholder.parsed") {
                findings += finding(
                    file,
                    resolver.call,
                    FindingKind.VIOLATION,
                    "PARSED_PLACEHOLDER",
                    "Placeholder.parsed can interpret runtime data as MiniMessage; use Placeholder.unparsed or Placeholder.component.",
                )
            }
        }
    }

    private fun auditInterpolations(
        file: KtFile,
        mmCall: KtCallExpression,
        template: String,
        tags: List<MiniMessageTag>,
        unresolvedExpressions: List<String>,
        findings: MutableList<AuditFinding>,
    ) {
        var index = 0
        var unresolvedIndex = 0
        while (index < template.length) {
            if (template[index] != INTERPOLATION_MARKER) {
                if (template[index] == UNRESOLVED_START_MARKER) {
                    val end = template.indexOf(UNRESOLVED_END_MARKER, index + 1)
                    if (end < 0) {
                        findings += finding(
                            file,
                            mmCall,
                            FindingKind.REVIEW,
                            "DYNAMIC_TEMPLATE",
                            "An unresolved Kotlin expression is part of the MiniMessage template.",
                        )
                        break
                    }
                    val expression = unresolvedExpressions.getOrNull(unresolvedIndex++)
                        ?: "an unresolved Kotlin expression"
                    val tag = tags.filter { index > it.start && end < it.end }.minByOrNull { it.end - it.start }
                    findings += finding(
                        file,
                        mmCall,
                        FindingKind.REVIEW,
                        if (tag?.body?.contains(':') == true) "DYNAMIC_TAG_ARGUMENT_REVIEW" else "DYNAMIC_TEMPLATE",
                        "Kotlin expression '$expression' could not be statically analyzed as MiniMessage template text.",
                    )
                    index = end + 1
                    continue
                }
                index++
                continue
            }
            val end = template.indexOf(INTERPOLATION_MARKER, index + 1)
            if (end < 0) break
            val expression = template.substring(index + 1, end)
            if (!Regex("Hex\\.[A-Z][A-Z_0-9]*").matches(expression)) {
                val containingTags = tags.filter { index > it.start && end < it.end }
                val tag = containingTags.minByOrNull { it.end - it.start }
                val isTagName = tag != null && !tag.isClosing && tag.name.contains(INTERPOLATION_MARKER)
                val isTagArgument = !isTagName && containingTags.any { it.inAttribute || it.body.contains(':') }
                val isEscapedContent = isInsideEscape(index, tags)
                findings += finding(
                    file,
                    mmCall,
                    if (isEscapedContent) FindingKind.REVIEW else FindingKind.VIOLATION,
                    when {
                        isEscapedContent -> "ESCAPED_INTERPOLATION_REVIEW"
                        isTagName -> "DYNAMIC_TAG_NAME"
                        isTagArgument -> "DYNAMIC_TAG_ARGUMENT"
                        else -> "UNTRUSTED_MINIMESSAGE_INTERPOLATION"
                    },
                    when {
                        isEscapedContent -> {
                            "Runtime value '$expression' is inside an <escape> section; confirm it cannot terminate the escaped region."
                        }
                        isTagName -> {
                            "Runtime value '$expression' is interpolated into a MiniMessage tag name."
                        }
                        isTagArgument -> {
                            "Runtime value '$expression' is interpolated into a MiniMessage tag argument."
                        }
                        else -> {
                            "Runtime value '$expression' is parsed as part of a MiniMessage template; " +
                                "use Placeholder.unparsed or Placeholder.component."
                        }
                    },
                )
            }
            index = end + 1
        }
    }

    private fun auditHardcodedColors(
        file: KtFile,
        findings: MutableList<AuditFinding>,
    ) {
        file.collectDescendantsOfType<KtCallExpression>().forEach { call ->
            if (call.isInsideHexObject() || call.receiverText()?.substringAfterLast('.') != "TextColor") {
                return@forEach
            }
            val method = call.calleeExpression?.text ?: return@forEach
            val arguments = call.valueArguments.mapNotNull { it.getArgumentExpression() }
            val hasLiteralColor = when (method) {
                "color" ->
                    arguments.size in setOf(1, 3) &&
                        arguments.all { it.text.matches(Regex("(?:0[xX][0-9a-fA-F]+|[0-9]+)[uU]?[lL]?")) }
                "fromHexString", "fromCSSHexString" ->
                    arguments.firstOrNull()?.let(::literalString)?.let(::isHexColor) == true
                else -> false
            }
            if (hasLiteralColor) {
                findings += finding(
                    file,
                    call,
                    FindingKind.VIOLATION,
                    "HARDCODED_PALETTE_COLOR",
                    "Use a Hex palette constant instead of a hardcoded color literal.",
                )
            }
        }
    }

    private fun KtCallExpression.isInsideHexObject(): Boolean = generateSequence(parent) { it.parent }
        .filterIsInstance<KtClassOrObject>()
        .any { it.name == "Hex" }

    private fun Char.isHexDigit(): Boolean = isDigit() || lowercaseChar() in 'a'..'f'

    private fun templateText(expression: KtExpression): TemplateAnalysis = when (expression) {
        is KtStringTemplateExpression -> {
            TemplateAnalysis(
                text = buildString {
                    expression.entries.forEach { entry ->
                        when (entry) {
                            is KtEscapeStringTemplateEntry -> {
                                append(entry.unescapedValue)
                            }
                            is KtStringTemplateEntryWithExpression -> {
                                val interpolatedExpression = entry.expression?.text
                                if (interpolatedExpression == null) {
                                    append(UNRESOLVED_START_MARKER)
                                    append(UNRESOLVED_END_MARKER)
                                } else {
                                    append(INTERPOLATION_MARKER)
                                    append(interpolatedExpression)
                                    append(INTERPOLATION_MARKER)
                                }
                            }
                            else -> {
                                append(entry.text)
                            }
                        }
                    }
                },
                unresolvedExpressions = expression.entries
                    .filterIsInstance<KtStringTemplateEntryWithExpression>()
                    .filter { it.expression == null }
                    .map { "incomplete string interpolation" },
            )
        }
        is KtBinaryExpression -> {
            if (expression.operationToken == KtTokens.PLUS) {
                val left = expression.left?.let(::templateText) ?: unresolvedTemplate(expression.text)
                val right = expression.right?.let(::templateText) ?: unresolvedTemplate(expression.text)
                TemplateAnalysis(
                    text = left.text + right.text,
                    unresolvedExpressions = left.unresolvedExpressions + right.unresolvedExpressions,
                )
            } else {
                unresolvedTemplate(expression.text)
            }
        }
        is KtParenthesizedExpression -> {
            expression.expression?.let(::templateText) ?: unresolvedTemplate(expression.text)
        }
        is KtDotQualifiedExpression -> {
            val selector = expression.selectorExpression as? KtCallExpression
            if (selector?.calleeExpression?.text == "trimIndent" && selector.valueArguments.isEmpty()) {
                templateText(expression.receiverExpression)
            } else if (
                selector?.calleeExpression?.text == "trimMargin" &&
                selector.valueArguments.size <= 1 &&
                selector.valueArguments.all { it.getArgumentExpression()?.let(::literalString) != null }
            ) {
                templateText(expression.receiverExpression)
            } else {
                unresolvedTemplate(expression.text)
            }
        }
        else -> {
            unresolvedTemplate(expression.text)
        }
    }

    private fun unresolvedTemplate(expression: String): TemplateAnalysis = TemplateAnalysis(
        text = "$UNRESOLVED_START_MARKER$UNRESOLVED_END_MARKER",
        unresolvedExpressions = listOf(expression),
    )

    private fun literalString(expression: KtExpression): String? {
        val template = expression as? KtStringTemplateExpression ?: return null
        if (template.entries.any { it is KtStringTemplateEntryWithExpression }) return null
        return template.entries.joinToString("") { it.text }
    }

    /**
     * Scans angle-delimited tags while respecting escaped delimiters, quoted attributes, and nested tags.
     * Quoted and nested attribute contents are scanned in their own color scope.
     */
    private fun parseTags(template: String): TagScanResult {
        val tags = mutableListOf<MiniMessageTag>()
        val warnings = mutableListOf<String>()
        var nextScope = 0

        fun scanRange(
            start: Int,
            limit: Int,
            inAttribute: Boolean,
        ) {
            val scope = nextScope++
            val colorStack = mutableListOf<String>()
            var escapeDepth = 0
            var cursor = start
            while (cursor < limit) {
                val tagStart = template.indexOf('<', cursor)
                if (tagStart < 0 || tagStart >= limit) break
                if (isEscaped(template, tagStart) ||
                    !template.getOrNull(tagStart + 1).isMiniMessageTagStart()
                ) {
                    cursor = tagStart + 1
                    continue
                }

                val parsed = readTag(template, tagStart, limit)
                if (parsed == null) {
                    if (escapeDepth == 0) {
                        warnings += "Possible MiniMessage tag at offset $tagStart is not closed."
                    }
                    cursor = tagStart + 1
                    continue
                }

                val rawBody = template.substring(tagStart + 1, parsed.end)
                val body = rawBody.trim()
                val isClosing = body.startsWith('/')
                val isSelfClosing = !isClosing && body.endsWith('/')
                val tagContent = body.removePrefix("/").removeSuffix("/").trim()
                val name = tagContent.substringBefore(':').substringBefore(' ').trim()
                val isEscapeTag = name == "escape"
                if (escapeDepth > 0 && !isEscapeTag) {
                    cursor = parsed.end + 1
                    continue
                }
                val tagColor = name.takeIf(::isColorTag)
                if (isClosing && tagColor != null) {
                    val matchingColor = colorStack.indexOfLast { it == tagColor }
                    if (matchingColor < 0) {
                        warnings += "Closing color tag '$name' at offset $tagStart has no matching open tag in its scope."
                    } else {
                        colorStack.subList(matchingColor, colorStack.size).clear()
                    }
                }

                tags += MiniMessageTag(
                    start = tagStart,
                    end = parsed.end,
                    body = body,
                    name = name,
                    isClosing = isClosing,
                    isSelfClosing = isSelfClosing,
                    inAttribute = inAttribute,
                    scope = scope,
                    scopeStart = start,
                    scopeEnd = limit,
                    dynamicActive = DYNAMIC_TAG in colorStack,
                )
                if (!isClosing && !isSelfClosing && tagColor != null) {
                    colorStack += tagColor
                }
                if (isEscapeTag && !isSelfClosing) {
                    escapeDepth = if (isClosing) {
                        (escapeDepth - 1).coerceAtLeast(0)
                    } else {
                        escapeDepth + 1
                    }
                }

                val nestedAttribute = inAttribute || !isHoverShowText(tagContent)
                parsed.contentRanges.forEach { range ->
                    if (range.first < range.second) {
                        scanRange(range.first, range.second, nestedAttribute)
                    }
                }
                cursor = parsed.end + 1
            }
        }

        scanRange(0, template.length, inAttribute = false)
        return TagScanResult(tags.sortedBy { it.start }, warnings.distinct())
    }

    private fun readTag(
        template: String,
        start: Int,
        limit: Int,
    ): ParsedTag? {
        var cursor = start + 1
        var quote: Char? = null
        var quotedContentStart = -1
        var nestedDepth = 0
        var nestedStart = -1
        val contentRanges = mutableListOf<Pair<Int, Int>>()

        while (cursor < limit) {
            val current = template[cursor]
            if (isEscaped(template, cursor)) {
                cursor++
                continue
            }
            if (quote != null) {
                if (current == quote) {
                    contentRanges += quotedContentStart to cursor
                    quote = null
                    quotedContentStart = -1
                }
                cursor++
                continue
            }
            when {
                current == '"' || current == '\'' -> {
                    quote = current
                    quotedContentStart = cursor + 1
                }
                current == '<' && template.getOrNull(cursor + 1).isMiniMessageTagStart() -> {
                    if (nestedDepth == 0) nestedStart = cursor
                    nestedDepth++
                }
                current == '>' && nestedDepth > 0 -> {
                    nestedDepth--
                    if (nestedDepth == 0 && nestedStart >= 0) {
                        contentRanges += nestedStart to (cursor + 1)
                        nestedStart = -1
                    }
                }
                current == '>' -> {
                    return ParsedTag(cursor, contentRanges.distinct())
                }
            }
            cursor++
        }
        return null
    }

    private fun Char?.isMiniMessageTagStart(): Boolean =
        this == '/' || this == '#' || this == INTERPOLATION_MARKER || this?.isLetter() == true || this == '!'

    private fun isHoverShowText(tagContent: String): Boolean {
        val name = tagContent.substringBefore(':').trim()
        if (name != "hover") return false
        return tagContent.substringAfter(':', "")
            .substringBefore(':')
            .trim()
            .equals("show_text", ignoreCase = true)
    }

    private fun isColorTag(name: String): Boolean = name == DYNAMIC_TAG || name in COLOR_TAG_NAMES || isHexColor(name)

    private fun isHexColor(name: String): Boolean {
        if (!name.startsWith('#')) return false
        val digits = name.drop(1)
        return (digits.length == 6 || digits.length == 8) && digits.all { it.isHexDigit() }
    }

    private fun isInsideEscape(
        index: Int,
        tags: List<MiniMessageTag>,
    ): Boolean {
        val scope = tags
            .filter { index in it.scopeStart until it.scopeEnd }
            .minByOrNull { it.scopeEnd - it.scopeStart }
            ?.scope ?: return false
        return tags
            .asSequence()
            .filter { it.scope == scope && it.start < index }
            .sortedBy { it.start }
            .fold(0) { depth, tag ->
                when {
                    tag.name == "escape" && !tag.isClosing -> depth + 1
                    tag.name == "escape" && tag.isClosing -> (depth - 1).coerceAtLeast(0)
                    else -> depth
                }
            } > 0
    }

    private fun isEscaped(
        text: String,
        index: Int,
    ): Boolean {
        var slashCount = 0
        var cursor = index - 1
        while (cursor >= 0 && text[cursor] == '\\') {
            slashCount++
            cursor--
        }
        return slashCount % 2 == 1
    }

    private fun resolverType(call: KtCallExpression): String? {
        val constructor = call.calleeExpression?.text ?: return null
        return when (call.receiverText()) {
            "Placeholder" -> "Placeholder.$constructor"
            "TagResolver" -> "TagResolver.$constructor"
            else -> null
        }
    }

    private fun directCallExpression(expression: KtExpression): KtCallExpression? = when (expression) {
        is KtCallExpression -> expression
        is KtDotQualifiedExpression -> expression.selectorExpression as? KtCallExpression
        is KtParenthesizedExpression -> expression.expression?.let(::directCallExpression)
        is KtPrefixExpression -> expression.baseExpression?.let(::directCallExpression)
        else -> null
    }

    private fun KtCallExpression.isMiniMessageCall(): Boolean {
        if (calleeExpression?.text == "mm") return true
        if (calleeExpression?.text != "deserialize") return false
        val receiver = receiverText() ?: return false
        return receiver == "miniMessage" || receiver.startsWith("MiniMessage.miniMessage")
    }

    private fun KtCallExpression.isInsideMiniMessageHelper(): Boolean = calleeExpression?.text == "deserialize" &&
        generateSequence(parent) { it.parent }
            .filterIsInstance<KtNamedFunction>()
            .any { it.name == "mm" }

    private fun KtCallExpression.receiverText(): String? {
        val qualifiedExpression = parent as? KtDotQualifiedExpression ?: return null
        return qualifiedExpression.receiverExpression.text.takeIf { qualifiedExpression.selectorExpression === this }
    }

    private fun finding(
        file: KtFile,
        element: org.jetbrains.kotlin.com.intellij.psi.PsiElement,
        kind: FindingKind,
        rule: String,
        message: String,
    ): AuditFinding {
        val offset = element.textRange.startOffset.coerceIn(0, file.text.length)
        val lineStart = file.text.lastIndexOf('\n', (offset - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
        val lineEnd = file.text.indexOf('\n', offset).let { if (it < 0) file.text.length else it }
        val line = file.text.substring(0, offset).count { it == '\n' } + 1
        val column = offset - lineStart + 1
        val snippet = file.text.substring(lineStart, lineEnd).trim().ifEmpty { element.text.trim() }.take(180)

        return AuditFinding(
            rule = rule,
            severity = kind,
            line = line,
            column = column,
            sourceExpression = snippet,
            message = message,
            remediation = remediation(rule),
        )
    }

    private fun remediation(rule: String): String = when (rule) {
        "SCALAR_NOT_DYNAMIC" -> "Wrap this placeholder occurrence in <\${Hex.DYNAMIC}>...</\${Hex.DYNAMIC}>."
        "DYNAMIC_TAG_ARGUMENT", "DYNAMIC_TAG_NAME", "UNTRUSTED_MINIMESSAGE_INTERPOLATION" ->
            "Keep MiniMessage syntax static and pass runtime text with Placeholder.unparsed or a Component."
        "PARSED_PLACEHOLDER" -> "Use Placeholder.unparsed for text or Placeholder.component for formatted components."
        "INVALID_PLACEHOLDER_NAME" -> "Use lowercase words separated by single hyphens."
        "DUPLICATE_PLACEHOLDER" -> "Keep one resolver declaration for this placeholder name."
        "PLACEHOLDER_NOT_IN_TEMPLATE" -> "Add the placeholder tag to the template or remove the unused resolver."
        "HARDCODED_PALETTE_COLOR" -> "Use the matching Hex palette constant."
        "DYNAMIC_TEMPLATE", "DYNAMIC_PLACEHOLDER_NAME", "ESCAPED_INTERPOLATION_REVIEW" ->
            "Make the expression statically auditable or review its MiniMessage parsing and trust boundaries."
        else -> "Review the resolver or tag and use a supported, statically validated form."
    }

    private data class Resolver(
        val call: KtCallExpression,
        val type: String,
        val name: String,
    )

    private data class MiniMessageTag(
        val start: Int,
        val end: Int,
        val body: String,
        val name: String,
        val isClosing: Boolean,
        val isSelfClosing: Boolean,
        val inAttribute: Boolean,
        val scope: Int,
        val scopeStart: Int,
        val scopeEnd: Int,
        val dynamicActive: Boolean,
    )

    private data class TagScanResult(
        val tags: List<MiniMessageTag>,
        val warnings: List<String>,
    )

    private data class ParsedTag(
        val end: Int,
        val contentRanges: List<Pair<Int, Int>>,
    )
}

class StringsEnAuditTest {
    @Test
    fun `StringsEn has no definite localization violations`() {
        val source = File(System.getProperty("stringsEnSource")).readText()
        val report = auditor.audit(source)

        report.violations.forEach { println(it.format("StringsEn.kt")) }
        report.reviewWarnings.forEach { println(it.format("StringsEn.kt")) }
        assertTrue(
            report.violations.isEmpty(),
            report.violations.joinToString(separator = "\n") { it.format("StringsEn.kt") },
        )
    }

    @Test
    fun `unparsed placeholders safely preserve MiniMessage-looking input`() {
        val report = audit(
            """
                fun message(input: String) = mm(
                    "<${'$'}{Hex.ERROR}>Value: <${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}></${'$'}{Hex.ERROR}>",
                    Placeholder.unparsed("value", input),
                )
            """.trimIndent(),
        )

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        val untrustedInput = "<red>not red</red>"
        val component = StringsEn().errors.errorWorldNotFound(untrustedInput)
        assertEquals(
            "Could not find world $untrustedInput",
            net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
                .plainText()
                .serialize(component),
        )
    }

    @Test
    fun `reports scalar color naming interpolation and literal color violations`() {
        val report = audit(
            """
                fun message(input: String) = mm(
                    "<click:run_command:'${'$'}{input}'>Hello <value></click>",
                    Placeholder.unparsed("value", input),
                    Placeholder.unparsed("bad_name", input),
                )
                fun direct(input: String) = mm("<red>${'$'}input</red>")
                fun directDeserialize(input: String) = MiniMessage.miniMessage().deserialize("<red>${'$'}input</red>")
                val hardcoded = TextColor.color(0x123456)
            """.trimIndent(),
        )

        val rules = report.violations.map { it.rule }.toSet()
        assertTrue("SCALAR_NOT_DYNAMIC" in rules, report.violations.toString())
        assertTrue("INVALID_PLACEHOLDER_NAME" in rules, report.violations.toString())
        assertTrue("DYNAMIC_TAG_ARGUMENT" in rules, report.violations.toString())
        assertTrue("UNTRUSTED_MINIMESSAGE_INTERPOLATION" in rules, report.violations.toString())
        assertTrue("HARDCODED_PALETTE_COLOR" in rules, report.violations.toString())
    }

    @Test
    fun `every scalar placeholder occurrence must be highlighted`() {
        val bothHighlighted = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}> " +
                        "<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )
        val oneHighlighted = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}> <value>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )
        val noneHighlighted = audit(
            """
                fun message(value: String) = mm(
                    "<value> <value>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )

        assertTrue(bothHighlighted.violations.none { it.rule == "SCALAR_NOT_DYNAMIC" })
        assertEquals(1, oneHighlighted.violations.count { it.rule == "SCALAR_NOT_DYNAMIC" })
        assertEquals(2, noneHighlighted.violations.count { it.rule == "SCALAR_NOT_DYNAMIC" })
    }

    @Test
    fun `nested multiline and chained formatting supports multiple placeholders`() {
        val source = """
            fun message(first: String, second: String) = mm(
                '''
                <${'$'}{Hex.SUCCESS}><bold><${'$'}{Hex.DYNAMIC}><first></${'$'}{Hex.DYNAMIC}></bold>
                <${'$'}{Hex.DYNAMIC}><underlined><second></underlined></${'$'}{Hex.DYNAMIC}></${'$'}{Hex.SUCCESS}>
                '''.trimIndent(),
                Placeholder.unparsed("first", first),
                Placeholder.unparsed("second", second),
            )
        """.trimIndent().replace("'''", "\"\"\"")

        val report = audit(source)

        assertTrue(report.violations.isEmpty(), report.violations.toString())
    }

    @Test
    fun `escaped tags do not establish dynamic highlighting`() {
        val escapedLiteral = audit("""fun message() = mm("\\<red>literal\\</red>")""")
        val escapedDynamicTag = audit(
            """
                fun message(value: String) = mm(
                    "\\<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )

        assertTrue(escapedLiteral.violations.isEmpty(), escapedLiteral.violations.toString())
        assertEquals(
            1,
            escapedDynamicTag.violations.count { it.rule == "SCALAR_NOT_DYNAMIC" },
            escapedDynamicTag.violations.toString(),
        )
    }

    @Test
    fun `nested dynamic formatting survives semantic colors and rejects self closing tags`() {
        val nestedSemanticColor = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.DYNAMIC}><red><value></red></${'$'}{Hex.DYNAMIC}>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )
        val selfClosingDynamic = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.DYNAMIC}/><value>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )

        assertTrue(nestedSemanticColor.violations.none { it.rule == "SCALAR_NOT_DYNAMIC" })
        assertEquals(1, selfClosingDynamic.violations.count { it.rule == "SCALAR_NOT_DYNAMIC" })
    }

    @Test
    fun `nested attribute tags are scanned without treating click text as visible placeholders`() {
        val report = audit(
            """
                fun message(value: String) = mm(
                    "<hover:show_text:'<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>'>Hover</hover>" +
                        "<click:run_command:'say <${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>'>Run</click>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )

        assertEquals(1, report.violations.count { it.rule == "SCALAR_NOT_DYNAMIC" }, report.violations.toString())
        assertTrue(report.violations.none { it.rule == "PLACEHOLDER_NOT_IN_TEMPLATE" }, report.violations.toString())
    }

    @Test
    fun `tag arguments survive concatenation and escaped delimiters`() {
        val concatenated = audit(
            """
                fun message(command: String) = mm(
                    "<click:run_command:'" + "${'$'}command" + "'>Run</click>",
                )
            """.trimIndent(),
        )
        val escapedDelimiter = audit(
            """
                fun message(command: String) = mm(
                    "<click:run_command:'say \\> ${'$'}command'>Run</click>",
                )
            """.trimIndent(),
        )

        assertTrue(concatenated.violations.any { it.rule == "DYNAMIC_TAG_ARGUMENT" }, concatenated.toString())
        assertTrue(concatenated.reviewWarnings.none { it.rule == "MINIMESSAGE_SYNTAX_REVIEW" }, concatenated.toString())
        assertTrue(escapedDelimiter.violations.any { it.rule == "DYNAMIC_TAG_ARGUMENT" }, escapedDelimiter.toString())
        assertTrue(escapedDelimiter.reviewWarnings.none { it.rule == "MINIMESSAGE_SYNTAX_REVIEW" }, escapedDelimiter.toString())
    }

    @Test
    fun `ambiguous unterminated tag syntax produces a review warning`() {
        val report = audit("""fun message() = mm("<click:run_command:'unfinished>")""")

        assertTrue(report.reviewWarnings.any { it.rule == "MINIMESSAGE_SYNTAX_REVIEW" }, report.reviewWarnings.toString())
        assertTrue(report.violations.isEmpty(), report.violations.toString())
    }

    @Test
    fun `nested escape sections suppress tags and review runtime interpolation`() {
        val escapedTags = audit(
            """
                fun message(value: String) = mm(
                    "<escape><${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}></escape>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )
        val nestedEscapes = audit(
            """
                fun message(value: String) = mm(
                    "<escape>literal <escape>${'$'}value</escape> text</escape>",
                )
            """.trimIndent(),
        )

        assertTrue(escapedTags.violations.any { it.rule == "PLACEHOLDER_NOT_IN_TEMPLATE" })
        assertTrue(escapedTags.violations.none { it.rule == "SCALAR_NOT_DYNAMIC" })
        assertTrue(nestedEscapes.reviewWarnings.any { it.rule == "ESCAPED_INTERPOLATION_REVIEW" })
        assertTrue(nestedEscapes.violations.isEmpty(), nestedEscapes.violations.toString())
    }

    @Test
    fun `direct palette colors are detected without flagging unrelated strings or comments`() {
        val report = audit(
            """
                // TextColor.color(0x123456)
                val note = "Hex color #123456 appears in this documentation."
                val numeric = TextColor.color(0x123456)
                val rgb = TextColor.color(10, 20, 30)
                val hex = TextColor.fromHexString("#12ABEF")
                val palette = TextColor.color(Hex.SUCCESS_COLOR)
            """.trimIndent(),
        )

        assertEquals(3, report.violations.count { it.rule == "HARDCODED_PALETTE_COLOR" }, report.violations.toString())
    }

    @Test
    fun `interpolation in tag names commands and hover attributes is reported`() {
        val report = audit(
            """
                fun message(name: String, command: String, hover: String) = mm(
                    "<${'$'}name>Text</${'$'}name>" +
                        "<click:run_command:'${'$'}{command}'>" +
                        "<hover:show_text:'${'$'}{hover}'>" +
                        "<insertion:'${'$'}{hover}'>Run</insertion></hover></click>",
                )
            """.trimIndent(),
        )

        val rules = report.violations.map { it.rule }
        assertTrue("DYNAMIC_TAG_NAME" in rules, report.violations.toString())
        assertEquals(3, rules.count { it == "DYNAMIC_TAG_ARGUMENT" }, report.violations.toString())
    }

    @Test
    fun `static Hex interpolation and ordinary Kotlin strings are accepted`() {
        val report = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.SUCCESS}><${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}></${'$'}{Hex.SUCCESS}>",
                    Placeholder.unparsed("value", value),
                )
                fun unrelated(value: String) = "Hello ${'$'}value"
            """.trimIndent(),
        )

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        assertTrue(report.reviewWarnings.isEmpty(), report.reviewWarnings.toString())
    }

    @Test
    fun `dynamic and unsupported placeholder constructors are reviewed`() {
        val dynamicName = audit(
            """
                fun message(name: String, value: String) = mm(
                    "<value>",
                    Placeholder.unparsed(name, value),
                )
            """.trimIndent(),
        )
        val unsupportedConstructor = audit(
            """
                fun message(value: String) = mm(
                    "<value>",
                    Placeholder.custom("value", value),
                )
            """.trimIndent(),
        )

        assertTrue(dynamicName.reviewWarnings.any { it.rule == "DYNAMIC_PLACEHOLDER_NAME" })
        assertTrue(unsupportedConstructor.reviewWarnings.any { it.rule == "UNSUPPORTED_PLACEHOLDER_CONSTRUCTOR" })
    }

    @Test
    fun `component styling and TagResolver constructors remain supported`() {
        val report = audit(
            """
                fun message(component: Component, style: StyleBuilderApplicable) = mm(
                    "<component><style><value>",
                    Placeholder.component("component", component),
                    Placeholder.styling("style", style),
                    TagResolver.resolver("value") { },
                )
            """.trimIndent(),
        )

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        assertTrue(report.reviewWarnings.isEmpty(), report.reviewWarnings.toString())
    }

    @Test
    fun `dynamic MiniMessage templates and interpolation in escaped sections are reviewed`() {
        val dynamicTemplate = audit(
            """
                fun message(template: String) = MiniMessage.miniMessage().deserialize(template)
            """.trimIndent(),
        )
        val escapedInterpolation = audit(
            """
                fun message(value: String) = mm("<escape>${'$'}value</escape>")
            """.trimIndent(),
        )

        assertTrue(dynamicTemplate.reviewWarnings.any { it.rule == "DYNAMIC_TEMPLATE" })
        assertTrue(escapedInterpolation.reviewWarnings.any { it.rule == "ESCAPED_INTERPOLATION_REVIEW" })
        assertTrue(escapedInterpolation.violations.isEmpty(), escapedInterpolation.violations.toString())
    }

    @Test
    fun `parsed placeholders are definite violations and duplicates are detected`() {
        val parsed = audit(
            """
                fun message(value: String) = mm(
                    "<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>",
                    Placeholder.parsed("value", value),
                )
            """.trimIndent(),
        )
        val duplicate = audit(
            """
                fun message(value: String) = mm(
                    "<value>",
                    Placeholder.unparsed("value", value),
                    Placeholder.component("value", Component.text(value)),
                )
            """.trimIndent(),
        )
        val unused = audit(
            """
                fun message(value: String) = mm(
                    "<other>",
                    Placeholder.unparsed("value", value),
                )
            """.trimIndent(),
        )

        assertTrue(parsed.violations.any { it.rule == "PARSED_PLACEHOLDER" })
        assertTrue(duplicate.violations.any { it.rule == "DUPLICATE_PLACEHOLDER" })
        assertTrue(unused.violations.any { it.rule == "PLACEHOLDER_NOT_IN_TEMPLATE" })
    }

    @Test
    fun `component formatting risks are review warnings rather than violations`() {
        val report = audit(
            """
                fun message(input: Component) = mm(
                    "<${'$'}{Hex.DYNAMIC}><value></${'$'}{Hex.DYNAMIC}>",
                    Placeholder.component("value", input),
                )
            """.trimIndent(),
        )

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        assertEquals(listOf("DYNAMIC_COMPONENT_STYLE"), report.reviewWarnings.map { it.rule })
    }

    @Test
    fun `component placeholders can inherit semantic colors without warnings`() {
        val report = audit(
            """
                fun message(input: Component) = mm(
                    "<${'$'}{Hex.SUCCESS}><value></${'$'}{Hex.SUCCESS}>",
                    Placeholder.component("value", input),
                )
            """.trimIndent(),
        )

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        assertTrue(report.reviewWarnings.isEmpty(), report.reviewWarnings.toString())
    }

    @Test
    fun `unknown simple tags are review warnings`() {
        val report = audit("""fun message() = mm("<custom>Text</custom>")""")

        assertTrue(report.violations.isEmpty(), report.violations.toString())
        assertEquals(listOf("UNDECLARED_TAG"), report.reviewWarnings.map { it.rule })
    }

    private fun audit(source: String): AuditReport = auditor.audit(source)

    companion object {
        private val disposable = Disposer.newDisposable()
        private val environment = KotlinCoreEnvironment.createForProduction(
            disposable,
            CompilerConfiguration(),
            EnvironmentConfigFiles.JVM_CONFIG_FILES,
        )
        private val auditor = StringsEnAuditor(KtPsiFactory(environment.project))
    }
}

object StringsEnAuditCli {
    @JvmStatic
    fun main(args: Array<String>) {
        require(args.size == 2) { "Expected source and report file arguments." }

        val sourceFile = File(args[0])
        val reportFile = File(args[1])
        val disposable = Disposer.newDisposable()
        try {
            val environment = KotlinCoreEnvironment.createForProduction(
                disposable,
                CompilerConfiguration(),
                EnvironmentConfigFiles.JVM_CONFIG_FILES,
            )
            val report = StringsEnAuditor(KtPsiFactory(environment.project)).audit(sourceFile.readText())
            val diagnostics = report.violations + report.reviewWarnings
            val rendered = diagnostics.joinToString(separator = "\n") { it.format(sourceFile.name) }

            reportFile.parentFile?.mkdirs()
            reportFile.writeText(
                buildString {
                    if (rendered.isNotEmpty()) appendLine(rendered)
                    appendLine("Audit summary: ${report.violations.size} violation(s), ${report.reviewWarnings.size} review warning(s).")
                },
            )
            diagnostics.forEach { println(it.format(sourceFile.name)) }
            println("Audit summary: ${report.violations.size} violation(s), ${report.reviewWarnings.size} review warning(s).")

            check(report.violations.isEmpty()) {
                "Localization audit found ${report.violations.size} definite violation(s). See ${reportFile.path}."
            }
        } finally {
            Disposer.dispose(disposable)
        }
    }
}
