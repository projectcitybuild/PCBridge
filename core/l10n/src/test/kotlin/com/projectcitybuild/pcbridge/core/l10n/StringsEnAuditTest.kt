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
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtParenthesizedExpression
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateEntryWithExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

private val PLACEHOLDER_NAME_PATTERN = Regex("[a-z][a-z0-9]*(?:-[a-z0-9]+)*")

private val MINIMESSAGE_TAG_NAMES =
    setOf(
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

private const val INTERPOLATION_MARKER = '\u0001'

private enum class FindingKind {
    VIOLATION,
    REVIEW,
}

private data class AuditFinding(
    val kind: FindingKind,
    val line: Int,
    val rule: String,
    val message: String,
)

private data class AuditReport(
    val violations: List<AuditFinding>,
    val reviewWarnings: List<AuditFinding>,
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
            auditMiniMessageCall(file, mmCall, calls, findings)
        }

        return AuditReport(
            violations = findings.filter { it.kind == FindingKind.VIOLATION },
            reviewWarnings = findings.filter { it.kind == FindingKind.REVIEW },
        )
    }

    private fun auditMiniMessageCall(
        file: KtFile,
        mmCall: KtCallExpression,
        calls: List<KtCallExpression>,
        findings: MutableList<AuditFinding>,
    ) {
        val templateExpression = mmCall.valueArguments.firstOrNull()?.getArgumentExpression()
        val template = templateExpression?.let(::templateText)
        if (template == null) {
            findings +=
                finding(
                    file,
                    mmCall,
                    FindingKind.REVIEW,
                    "DYNAMIC_TEMPLATE",
                    "The MiniMessage template is not a statically readable string; review it manually.",
                )
            return
        }

        val tags = parseTags(template)
        auditInterpolations(file, mmCall, template, tags, findings)

        val resolvers =
            calls
                .filter { it !== mmCall && enclosingMiniMessageCall(it) === mmCall }
                .mapNotNull { resolverCall ->
                    val resolverType = resolverType(resolverCall) ?: return@mapNotNull null
                    val name =
                        resolverCall.valueArguments
                            .firstOrNull()
                            ?.getArgumentExpression()
                            ?.let(::literalString)
                    if (name == null) {
                        findings +=
                            finding(
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
                findings +=
                    finding(
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
                PLACEHOLDER_NAME_PATTERN.matches(it.body) &&
                    it.body !in resolverNames &&
                    it.body !in MINIMESSAGE_TAG_NAMES
            }.forEach { tag ->
                findings +=
                    finding(
                        file,
                        mmCall,
                        FindingKind.REVIEW,
                        "UNDECLARED_TAG",
                        "Tag '${tag.body}' has no Placeholder resolver; confirm that it is an intentional MiniMessage extension.",
                    )
            }

        resolvers.forEach { resolver ->
            if (!PLACEHOLDER_NAME_PATTERN.matches(resolver.name)) {
                findings +=
                    finding(
                        file,
                        resolver.call,
                        FindingKind.VIOLATION,
                        "INVALID_PLACEHOLDER_NAME",
                        "Placeholder '${resolver.name}' must use lowercase words separated by single hyphens.",
                    )
            }

            val uses = tags.filter { it.body == resolver.name }
            if (uses.isEmpty()) {
                findings +=
                    finding(
                        file,
                        resolver.call,
                        FindingKind.VIOLATION,
                        "PLACEHOLDER_NOT_IN_TEMPLATE",
                        "Placeholder '${resolver.name}' is not used by its MiniMessage template.",
                    )
                return@forEach
            }

            if (resolver.type == "unparsed" && uses.none { it.dynamicDepth > 0 }) {
                findings +=
                    finding(
                        file,
                        resolver.call,
                        FindingKind.VIOLATION,
                        "SCALAR_NOT_DYNAMIC",
                        "Scalar placeholder '${resolver.name}' must be explicitly wrapped in Hex.DYNAMIC.",
                    )
            }

            if (resolver.type == "component" && uses.any { it.dynamicDepth > 0 }) {
                findings +=
                    finding(
                        file,
                        resolver.call,
                        FindingKind.REVIEW,
                        "DYNAMIC_COMPONENT_STYLE",
                        "Component placeholder '${resolver.name}' is inside Hex.DYNAMIC; verify that its own formatting is preserved.",
                    )
            }
        }
    }

    private fun auditInterpolations(
        file: KtFile,
        mmCall: KtCallExpression,
        template: String,
        tags: List<MiniMessageTag>,
        findings: MutableList<AuditFinding>,
    ) {
        var index = 0
        while (index < template.length) {
            if (template[index] != INTERPOLATION_MARKER) {
                index++
                continue
            }
            val end = template.indexOf(INTERPOLATION_MARKER, index + 1)
            if (end < 0) break
            val expression = template.substring(index + 1, end)
            if (!Regex("Hex\\.[A-Z][A-Z_0-9]*").matches(expression)) {
                val tag = tags.firstOrNull { index > it.start && end < it.end }
                val isTagArgument = tag?.body?.contains(':') == true
                findings +=
                    finding(
                        file,
                        mmCall,
                        FindingKind.VIOLATION,
                        if (isTagArgument) "DYNAMIC_TAG_ARGUMENT" else "UNTRUSTED_MINIMESSAGE_INTERPOLATION",
                        if (isTagArgument) {
                            "Runtime value '$expression' is interpolated into a MiniMessage tag argument."
                        } else {
                            "Runtime value '$expression' is parsed as part of a MiniMessage template; " +
                                "use Placeholder.unparsed or Placeholder.component."
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
        file.collectDescendantsOfType<KtStringTemplateExpression>().forEach { string ->
            if (!string.isInsideHexObject() && containsHexColor(string.text)) {
                findings +=
                    finding(
                        file,
                        string,
                        FindingKind.VIOLATION,
                        "HARDCODED_PALETTE_COLOR",
                        "Use a Hex palette constant instead of a hardcoded color literal.",
                    )
            }
        }

        file.collectDescendantsOfType<KtCallExpression>().forEach { call ->
            if (call.calleeExpression?.text != "color" || call.receiverText() != "TextColor") return@forEach
            val literal = call.valueArguments
                .firstOrNull()
                ?.getArgumentExpression()
                ?.text ?: return@forEach
            if (literal.startsWith("0x", ignoreCase = true) && literal.drop(2).all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) {
                findings +=
                    finding(
                        file,
                        call,
                        FindingKind.VIOLATION,
                        "HARDCODED_PALETTE_COLOR",
                        "Use a Hex palette constant instead of a hardcoded color literal.",
                    )
            }
        }
    }

    private fun KtStringTemplateExpression.isInsideHexObject(): Boolean = generateSequence(parent) { it.parent }
        .filterIsInstance<KtClassOrObject>()
        .any { it.name == "Hex" }

    private fun containsHexColor(text: String): Boolean {
        for (index in text.indices) {
            if (text[index] != '#') continue
            var end = index + 1
            while (end < text.length && text[end].isHexDigit()) end++
            if (end - index in 7..9) return true
        }
        return false
    }

    private fun Char.isHexDigit(): Boolean = isDigit() || lowercaseChar() in 'a'..'f'

    private fun templateText(expression: KtExpression): String? = when (expression) {
        is KtStringTemplateExpression -> {
            buildString {
                expression.entries.forEach { entry ->
                    when (entry) {
                        is KtStringTemplateEntryWithExpression -> {
                            append(INTERPOLATION_MARKER)
                            append(entry.expression?.text ?: return null)
                            append(INTERPOLATION_MARKER)
                        }
                        else -> {
                            append(entry.text)
                        }
                    }
                }
            }
        }
        is KtBinaryExpression -> {
            if (expression.operationToken == KtTokens.PLUS) {
                val left = expression.left?.let(::templateText) ?: return null
                val right = expression.right?.let(::templateText) ?: return null
                left + right
            } else {
                null
            }
        }
        is KtParenthesizedExpression -> {
            expression.expression?.let(::templateText)
        }
        is KtDotQualifiedExpression -> {
            templateText(expression.receiverExpression)
        }
        else -> {
            null
        }
    }

    private fun literalString(expression: KtExpression): String? {
        val template = expression as? KtStringTemplateExpression ?: return null
        if (template.entries.any { it is KtStringTemplateEntryWithExpression }) return null
        return template.entries.joinToString("") { it.text }
    }

    private fun parseTags(template: String): List<MiniMessageTag> {
        val tags = mutableListOf<MiniMessageTag>()
        var dynamicDepth = 0
        var cursor = 0
        val dynamicTag = "$INTERPOLATION_MARKER" + "Hex.DYNAMIC" + INTERPOLATION_MARKER

        while (cursor < template.length) {
            val start = template.indexOf('<', cursor)
            if (start < 0) break
            val end = template.indexOf('>', start + 1)
            if (end < 0) break
            val body = template.substring(start + 1, end)
            if (body == "/$dynamicTag") {
                dynamicDepth = (dynamicDepth - 1).coerceAtLeast(0)
            } else {
                tags += MiniMessageTag(start, end, body, dynamicDepth)
                if (body == dynamicTag) dynamicDepth++
            }
            cursor = end + 1
        }
        return tags
    }

    private fun resolverType(call: KtCallExpression): String? {
        if (call.receiverText() != "Placeholder") return null
        return call.calleeExpression?.text?.takeIf { it == "unparsed" || it == "component" }
    }

    private fun enclosingMiniMessageCall(call: KtCallExpression): KtCallExpression? = generateSequence(call.parent) { it.parent }
        .filterIsInstance<KtCallExpression>()
        .firstOrNull { it.isMiniMessageCall() }

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
    ) = AuditFinding(kind, lineNumber(file, element), rule, message)

    private fun lineNumber(
        file: KtFile,
        element: org.jetbrains.kotlin.com.intellij.psi.PsiElement,
    ): Int = file.text.substring(0, element.textRange.startOffset).count { it == '\n' } + 1

    private data class Resolver(
        val call: KtCallExpression,
        val type: String,
        val name: String,
    )

    private data class MiniMessageTag(
        val start: Int,
        val end: Int,
        val body: String,
        val dynamicDepth: Int,
    )
}

class StringsEnAuditTest {
    @Test
    fun `StringsEn has no definite localization violations`() {
        val source = File(System.getProperty("stringsEnSource")).readText()
        val report = auditor.audit(source)

        assertTrue(
            report.violations.isEmpty(),
            report.violations.joinToString(separator = "\n") { "${it.line}: ${it.rule}: ${it.message}" },
        )
        report.reviewWarnings.forEach { println("Review warning ${it.line}: ${it.message}") }
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
