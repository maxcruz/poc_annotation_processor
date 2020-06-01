package com.example.processor

import com.example.tracking.Tracker
import com.squareup.javapoet.*
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

private const val TRACKER_FIELD = "tracker"
private const val CLASS_SUFIX = "Decorator"

object DecoratorClassBuilder {

    fun buildClass(baseName: String, element: Element): TypeSpec.Builder {
        val trackerSpec = FieldSpec
            .builder(Tracker::class.java, TRACKER_FIELD, Modifier.PRIVATE)
            .build()

        val constructorSpec = MethodSpec
            .constructorBuilder()
            .addAnnotation(Inject::class.java)
            .addParameter(Tracker::class.java, TRACKER_FIELD)
            .addStatement("this.\$N = \$N", trackerSpec, TRACKER_FIELD)
            .build()

        return TypeSpec
            .classBuilder("$baseName$CLASS_SUFIX")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(element.asType()))
            .addField(trackerSpec)
            .addMethod(constructorSpec)
    }

    fun buildMethod(name: String, parameters: List<String> = listOf()): MethodSpec {
        val parametersSpec = parameters.map {
            ParameterSpec.builder(String::class.java, it).build()
        }
        val properties = if (parameters.isNotEmpty()) "properties" else "null"
        return with(MethodSpec.methodBuilder(name)) {
            addAnnotation(Override::class.java)
            addModifiers(Modifier.PUBLIC)
            returns(TypeName.VOID)
            addParameters(parametersSpec)
            buildProperties(properties, parameters)
            addStatement("\$N.track(\$S, \$N)", TRACKER_FIELD, name, properties)
            build()
        }
    }

    private fun MethodSpec.Builder.buildProperties(
        name: String,
        parameters: List<String>
    ): MethodSpec.Builder {
        if (parameters.isEmpty()) return this
        val mapFormat = "\$T<String, String> \$N = new \$T<String, String>()"
        addStatement(mapFormat, Map::class.java, name, HashMap::class.java)
        parameters.forEach { property ->
            addStatement("\$N.put(\$S, \$N)", name, property, property)
        }
        return this
    }
}
