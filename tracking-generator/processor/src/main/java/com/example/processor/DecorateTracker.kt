package com.example.processor

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property
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
                //val eventTracker = interfaceElement.getAnnotation(EventTracker::class.java)
                //eventTracker.engines
                if (interfaceElement.kind != ElementKind.INTERFACE) {
                    error(interfaceElement, "EventTracker must be applied to an interface")
                    return true
                }
                val methods = interfaceElement.enclosedElements
                    .filter { it.kind == ElementKind.METHOD }
                    .map { it as ExecutableElement }
                    .toMethodSpecs()
                val className = interfaceElement.simpleName.toString()
                val packageName = elementUtils.getPackageOf(interfaceElement).toString()
                val builtClass = buildClass(className, interfaceElement, methods)
                writeClassFile(packageName, builtClass, interfaceElement)
            }
        return true
    }

    private fun List<ExecutableElement>.toMethodSpecs(): List<MethodSpec> {
        return map { element ->
            val eventName = element.getAnnotation(Event::class.java)
            if (eventName == null) {
                error(element, "Method must be annotated with @Event")
                return listOf()
            }
            val name = element.simpleName.toString()
            val parameters = element.parameters.toExtraProperties()
            ClassBuilder.buildMethod(name, eventName.name, parameters)
        }
    }

    private fun List<VariableElement>.toExtraProperties() : List<ExtraProperty> {
        return map { element ->
            val key = element.getAnnotation(Property::class.java)
            if (key == null) {
                error(element, "Parameter must be annotated with @Property")
                return listOf()
            }
            ExtraProperty(key.name, element.simpleName.toString())
        }
    }

    private fun error(element: Element, message: String, arguments: List<Any> = listOf()) {
        messager.printMessage(
            Diagnostic.Kind.ERROR,
            String.format(message, arguments),
            element
        )
    }

    private fun buildClass(name: String, element: Element, methods: List<MethodSpec>) : TypeSpec =
        ClassBuilder.buildClass(name, element)
            .addMethods(methods)
            .build()

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
