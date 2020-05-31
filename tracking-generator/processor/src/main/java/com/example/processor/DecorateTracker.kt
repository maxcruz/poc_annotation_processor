package com.example.processor

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property
import com.squareup.javapoet.JavaFile
import java.io.IOException
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * This class is going to generate the decorator tracker for the annotated interfaces
 */
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
        val eventTracker = EventTracker::class.qualifiedName

        // Maybe should be removed?
        val event = Event::class.qualifiedName
        val property = Property::class.qualifiedName
        return setOf(eventTracker, event, property)
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (roundEnvironment?.processingOver() == true) return false
        roundEnvironment?.getElementsAnnotatedWith(EventTracker::class.java)
            ?.forEach {
                if (it.kind != ElementKind.INTERFACE) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "EventTracker must be applied to an interface")
                    return false
                }
                // Improve get element metadata min 28:40
                val className = it.simpleName.toString()
                val packageName = elementUtils.getPackageOf(it).toString()
                generateClass(className, packageName, it)
            }
        return true
    }

    private fun generateClass(baseName: String, packageName: String, element: Element){
        val classSpec = DecoratorClassBuilder(baseName, packageName, element).buildDecorator()
        val file = JavaFile
            .builder(packageName, classSpec)
            .build()
        try {
            file.writeTo(filer)
        } catch (error: IOException) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write the tracking decorator: $error", element)
        }
    }
}
