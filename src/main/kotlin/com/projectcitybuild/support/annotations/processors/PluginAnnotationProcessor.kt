package com.projectcitybuild.support.annotations.processors

import com.google.common.collect.Maps
import com.projectcitybuild.support.annotations.annotations.*
import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.Tag
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.ElementFilter
import javax.tools.Diagnostic
import javax.tools.StandardLocation

//@SupportedAnnotationTypes("org.bukkit.plugin.java.annotation.*")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
class PluginAnnotationProcessor : AbstractProcessor() {
    private var hasMainBeenFound = false
    override fun process(annots: Set<TypeElement>, rEnv: RoundEnvironment): Boolean {
        var mainPluginElement: Element? = null
        hasMainBeenFound = false
        val elements = rEnv.getElementsAnnotatedWith(SpigotPlugin::class.java)
        if (elements.size > 1) {
            raiseError("Found more than one plugin main class")
            return false
        }
        if (elements.isEmpty()) { // don't raise error because we don't know which run this is
            return false
        }
        if (hasMainBeenFound) {
            raiseError("The plugin class has already been located, aborting!")
            return false
        }
        mainPluginElement = elements.iterator().next()
        hasMainBeenFound = true
        val mainPluginType: TypeElement = if (mainPluginElement is TypeElement) {
            mainPluginElement
        } else {
            raiseError("Main plugin class is not a class", mainPluginElement)
            return false
        }
        if (mainPluginType.enclosingElement !is PackageElement) {
            raiseError("Main plugin class is not a top-level class", mainPluginType)
            return false
        }
        if (mainPluginType.modifiers.contains(Modifier.STATIC)) {
            raiseError("Main plugin class cannot be static nested", mainPluginType)
            return false
        }
        if (!processingEnv.typeUtils.isSubtype(mainPluginType.asType(), fromClass(JavaPlugin::class.java))) {
            raiseError("Main plugin class is not an subclass of JavaPlugin!", mainPluginType)
        }
        if (mainPluginType.modifiers.contains(Modifier.ABSTRACT)) {
            raiseError("Main plugin class cannot be abstract", mainPluginType)
            return false
        }

        // check for no-args constructor
        checkForNoArgsConstructor(mainPluginType)
        val yml: MutableMap<String, Any> = Maps.newLinkedHashMap() // linked so we can maintain the same output into file for sanity

        // populate mainName
        val mainName = mainPluginType.qualifiedName.toString()
        yml["main"] = mainName // always override this so we make sure the main class name is correct

        // populate plugin name
        processAndPut(yml, "name", mainPluginType, mainName.substring(mainName.lastIndexOf('.') + 1), SpigotPlugin::class.java, String::class.java, "name")

        // populate version
        processAndPut(yml, "version", mainPluginType, "v0.0", SpigotPlugin::class.java, String::class.java, "version")

        // soft-dependencies
        val softDependencies = mainPluginType.getAnnotationsByType(SoftDependency::class.java)
        val softDepArr = arrayOfNulls<String>(softDependencies.size)
        for (i in softDependencies.indices) {
            softDepArr[i] = softDependencies[i].value
        }
        if (softDepArr.size > 0) yml["softdepend"] = softDepArr

        // commands
        // Begin processing external command annotations
        var commandMap: MutableMap<String?, Map<String?, Any?>?> = Maps.newLinkedHashMap()
        val validCommandExecutors = processExternalCommands(rEnv.getElementsAnnotatedWith(Commands::class.java), mainPluginType, commandMap)
        if (!validCommandExecutors) {
            // #processExternalCommand already raised the errors
            return false
        }
        val commands = mainPluginType.getAnnotation(Commands::class.java)

        // Check main class for any command annotations
        if (commands != null) {
            val merged: MutableMap<String?, Map<String?, Any?>?> = Maps.newLinkedHashMap()
            merged.putAll(commandMap)
            merged.putAll(processCommands(commands))
            commandMap = merged
        }
        yml["commands"] = commandMap

        // api-version
        if (mainPluginType.getAnnotation(ApiVersion::class.java) != null) {
            val apiVersion = mainPluginType.getAnnotation(ApiVersion::class.java)
            if (apiVersion.value != ApiVersion.Target.DEFAULT) {
                yml["api-version"] = apiVersion.value.version!!
            }
        }
        try {
            val yaml = Yaml()
            val file = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "plugin.yml")
            file.openWriter().use { w ->
                w.append("# Auto-generated plugin.yml, generated at ")
                    .append(LocalDateTime.now().format(dFormat))
                    .append(" by ")
                    .append(this.javaClass.name)
                    .append("\n\n")
                // have to format the yaml explicitly because otherwise it dumps child nodes as maps within braces.
                val raw = yaml.dumpAs(yml, Tag.MAP, DumperOptions.FlowStyle.BLOCK)
                w.write(raw)
                w.flush()
                w.close()
            }
            // try with resources will close the Writer since it implements Closeable
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return true
    }

    private fun checkForNoArgsConstructor(mainPluginType: TypeElement) {
        for (constructor in ElementFilter.constructorsIn(mainPluginType.enclosedElements)) {
            if (constructor.parameters.isEmpty()) {
                return
            }
        }
        raiseError("Main plugin class must have a no argument constructor.", mainPluginType)
    }

    private fun raiseError(message: String) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message)
    }

    private fun raiseError(message: String, element: Element?) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, message, element)
    }

    private fun fromClass(clazz: Class<*>): TypeMirror {
        return processingEnv.elementUtils.getTypeElement(clazz.name).asType()
    }

    private fun <A : Annotation?, R> processAndPut(
        map: MutableMap<String, Any>,
        name: String,
        el: Element,
        defaultVal: R,
        annotationType: Class<A>,
        returnType: Class<R>,
        methodName: String,
    ): R? {
        val result: R? = process(el, defaultVal, annotationType, returnType, methodName)
        if (result != null) map[name] = result
        return result
    }

    private fun <A : Annotation?, R> process(
        el: Element,
        defaultVal: R,
        annotationType: Class<A>,
        returnType: Class<R>,
        methodName: String,
    ): R {
        val result: R
        val ann = el.getAnnotation(annotationType)
        result = if (ann == null) defaultVal else {
            try {
                val value = annotationType.getMethod(methodName)
                val res = value.invoke(ann)
                (if (returnType == String::class.java) res.toString() else returnType.cast(res)) as R
            } catch (e: Exception) {
                throw RuntimeException(e) // shouldn't happen in theory (blame Choco if it does)
            }
        }
        return result
    }

    private fun processExternalCommands(commandExecutors: Set<Element>, mainPluginType: TypeElement, commandMetadata: MutableMap<String?, Map<String?, Any?>?>): Boolean {
        for (element in commandExecutors) {
            // Check to see if someone annotated a non-class with this
            if (element !is TypeElement) {
                this.raiseError("Specified Command Executor class is not a class.")
                return false
            }
            val typeElement = element
            if (typeElement == mainPluginType) {
                continue
            }

            // Check to see if annotated class is actuall a command executor
            val mirror = processingEnv.elementUtils.getTypeElement(CommandExecutor::class.java.name).asType()
            if (!processingEnv.typeUtils.isAssignable(typeElement.asType(), mirror)) {
                this.raiseError("Specified Command Executor class is not assignable from CommandExecutor ")
                return false
            }
            val annotation = typeElement.getAnnotation(Commands::class.java)
            if (annotation != null && annotation.value.size > 0) {
                commandMetadata.putAll(processCommands(annotation))
            }
        }
        return true
    }

    /**
     * Processes a set of commands.
     *
     * @param commands The annotation.
     *
     * @return The generated command metadata.
     */
    private fun processCommands(commands: Commands): Map<String?, Map<String?, Any?>?> {
        val commandList: MutableMap<String?, Map<String?, Any?>?> = Maps.newLinkedHashMap()
        for (command in commands.value) {
            commandList[command.name] = processCommand(command)
        }
        return commandList
    }

    /**
     * Processes a single command.
     *
     * @param commandAnnotation The annotation.
     *
     * @return The generated command metadata.
     */
    private fun processCommand(commandAnnotation: Command): Map<String?, Any?> {
        val command: MutableMap<String?, Any?> = Maps.newLinkedHashMap()
        if (commandAnnotation.aliases.size == 1) {
            command["aliases"] = commandAnnotation.aliases.get(0)
        } else if (commandAnnotation.aliases.size > 1) {
            command["aliases"] = commandAnnotation.aliases
        }
        if (!commandAnnotation.desc.isEmpty()) {
            command["description"] = commandAnnotation.desc
        }
        if (!commandAnnotation.permission.isEmpty()) {
            command["permission"] = commandAnnotation.permission
        }
        if (!commandAnnotation.permissionMessage.isEmpty()) {
            command["permission-message"] = commandAnnotation.permissionMessage
        }
        if (!commandAnnotation.usage.isEmpty()) {
            command["usage"] = commandAnnotation.usage
        }
        return command
    }

    companion object {
        private val dFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
    }
}
