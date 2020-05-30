package com.example.processor

import com.example.annotation.Event
import com.example.annotation.EventTracker
import com.example.annotation.Property
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

class DecorateTracker : AbstractProcessor(){

    override fun getSupportedAnnotationTypes(): Set<String> {
        val eventTracker = checkNotNull(EventTracker::class.qualifiedName)
        val event = checkNotNull(Event::class.qualifiedName)
        val property = checkNotNull(Property::class.qualifiedName)
        return setOf(eventTracker, event, property)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    override fun process(set: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment?.getElementsAnnotatedWith(EventTracker::class.java)
            ?.forEach {
                val className = it.simpleName.toString()
                val packageName = processingEnv.elementUtils.getPackageOf(it).toString()
                generateClass(className, packageName)
            }
        return true
    }

    private fun generateClass(className: String, packageName: String){
        val fileName = "${className}Decorator"
        val fileContent = DecoratorClassBuilder(fileName, packageName).getContent()
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        val file = File(kaptKotlinGeneratedDir, "$fileName.kt")
        file.writeText(fileContent)
    }
}
