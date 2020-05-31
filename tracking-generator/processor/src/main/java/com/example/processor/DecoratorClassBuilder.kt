package com.example.processor

import com.example.tracking.Tracker
import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

class DecoratorClassBuilder(
    private val baseName: String,
    private val packageName: String,
    private val element: Element
) {

    fun buildDecorator(): TypeSpec {
        val trackerSpec = FieldSpec
            .builder(Tracker::class.java, "tracker", Modifier.PRIVATE)
            .build()

        val constructorSpec = MethodSpec
            .constructorBuilder()
            .addAnnotation(Inject::class.java)
            .addParameter(Tracker::class.java, "tracker")
            .addStatement("this.\$N = tracker", trackerSpec)
            .build()

        val methodSpec1 = MethodSpec
            .methodBuilder("onMainScreenLoaded")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .returns(TypeName.VOID)
            .addStatement("\$N.track(\$S, null)", trackerSpec, "onMainScreenLoaded")
            .build()

        val methodSpec2 = MethodSpec
            .methodBuilder("onButtonClicked")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String::class.java, "time")
            .returns(TypeName.VOID)
            .addStatement("tracker.track(\$S, null)", "onButtonClicked")
            .build()

        return TypeSpec
            .classBuilder("${baseName}Decorator")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(element.asType()))
            .addField(trackerSpec)
            .addMethod(constructorSpec)
            .addMethod(methodSpec1)
            .addMethod(methodSpec2)
            .build()
    }
}
