package com.example.processor

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property
import com.example.tracking.TrackerEnum
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeSpec
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

class DecorateTracker : AbstractProcessor(){

    private lateinit var filer: Filer
    private lateinit var messager: Messager
    private lateinit var elementUtils: Elements

    override fun init(processingEnvironment: ProcessingEnvironment) {
        super.init(processingEnvironment)
        filer = processingEnvironment.filer
        messager = processingEnvironment.messager
        elementUtils = processingEnvironment.elementUtils
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun getSupportedAnnotationTypes(): Set<String?> {
        return setOf(EventTracker::class.qualifiedName)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnvironment: RoundEnvironment?
    ): Boolean {
        if (roundEnvironment?.processingOver() == true) return false
        roundEnvironment?.getElementsAnnotatedWith(EventTracker::class.java)
            ?.forEach { interfaceElement ->
                if (interfaceElement.kind != ElementKind.INTERFACE) {
                    error(interfaceElement, "EventTracker must be applied to an interface")
                    return true
                }
                val methods = interfaceElement.enclosedElements
                    .filter { it.kind == ElementKind.METHOD }
                    .map { it as ExecutableElement }
                    .toMethodSpecs()
                val eventTracker = interfaceElement.getAnnotation(EventTracker::class.java)
                val engines = eventTracker.engines
                    .map { TrackerEnum.valueOf("${it}_TRACKER") }
                val className = interfaceElement.simpleName.toString()
                val packageName = elementUtils.getPackageOf(interfaceElement).toString()
                val builtClass = buildClass(className, interfaceElement, methods, engines)
                writeClassFile(packageName, builtClass, interfaceElement)
            }
        return true
    }

    private fun List<ExecutableElement>.toMethodSpecs(): List<Pair<String, MethodSpec>> {
        return map { element ->
            val eventName = element.getAnnotation(Event::class.java)
            if (eventName == null) {
                error(element, "Method must be annotated with @Event")
                return listOf()
            }
            val name = element.simpleName.toString()
            val parameters = element.parameters.toExtraProperties()
            eventName.name to ClassBuilder.buildMethod(name, eventName.name, parameters)
        }
    }

    private fun List<VariableElement>.toExtraProperties() : Map<String, String> {
        return map { element ->
            val key = element.getAnnotation(Property::class.java)
            if (key == null) {
                error(element, "Parameter must be annotated with @Property")
                return mapOf()
            }
            key.name to element.simpleName.toString()
        }.toMap()
    }

    private fun error(element: Element, message: String, arguments: List<Any> = listOf()) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(message, arguments),
            element
        )
    }

    private fun buildClass(
        name: String,
        element: Element,
        tracking: List<Pair<String, MethodSpec>>,
        engines: List<TrackerEnum>
    ) : TypeSpec {
        val (events, methods) = tracking.unzip()
        return ClassBuilder.buildClass(name, element, events, engines)
            .addMethods(methods)
            .build()
    }

    private fun writeClassFile(packageName: String, classSpec: TypeSpec, element: Element) {
        val file = JavaFile
            .builder(packageName, classSpec)
            .build()
        try {
            file.writeTo(filer)
        } catch (error: IOException) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Failed to write the tracking decorator: $error",
                element
            )
        }
    }
}
