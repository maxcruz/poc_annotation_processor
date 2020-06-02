package com.example.processor

import com.example.tracking.Tracker
import com.example.tracking.TrackerEnum
import com.squareup.javapoet.*
import com.squareup.javapoet.ClassName
import java.util.*
import javax.inject.Inject
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import kotlin.collections.HashMap

private const val TRACKER_FIELD = "tracker"
private const val CLASS_SUFIX = "Decorator"

object ClassBuilder {

    fun buildClass(
        baseName: String,
        element: Element,
        map: List<String>,
        engines: List<TrackerEnum>
    ): TypeSpec.Builder {
        val trackerSpec = FieldSpec
            .builder(Tracker::class.java, TRACKER_FIELD, Modifier.PRIVATE)
            .build()

        val constructorSpec = MethodSpec
            .constructorBuilder()
            .addAnnotation(Inject::class.java)
            .addParameter(Tracker::class.java, TRACKER_FIELD)
            .addStatement("this.\$N = \$N", trackerSpec, TRACKER_FIELD)
            .mapTrackingEvents(map, engines)
            .build()

        return TypeSpec
            .classBuilder("$baseName$CLASS_SUFIX")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addSuperinterface(TypeName.get(element.asType()))
            .addField(trackerSpec)
            .addMethod(constructorSpec)
    }

    fun buildMethod(name: String, event: String, properties: Map<String, String> = mapOf()): MethodSpec {
        val parametersSpec = properties.map {
            ParameterSpec.builder(String::class.java, it.value).build()
        }
        val propertiesLiteral = if (properties.isNotEmpty()) "properties" else "null"
        return with(MethodSpec.methodBuilder(name)) {
            addAnnotation(Override::class.java)
            addModifiers(Modifier.PUBLIC)
            returns(TypeName.VOID)
            addParameters(parametersSpec)
            buildProperties(propertiesLiteral, properties)
            addStatement("\$N.track(\$S, \$N)", TRACKER_FIELD, event, propertiesLiteral)
            build()
        }
    }

    private fun MethodSpec.Builder.mapTrackingEvents(
        map: List<String>,
        engines: List<TrackerEnum>
    ): MethodSpec.Builder {
        val listEnumType = ParameterizedTypeName.get(
            List::class.java,
            TrackerEnum::class.java
        )
        val listVariable = "list"
        val enums = engines.joinToString(",") { "\$4T.$it" }
        addStatement(
            "\$1T \$2N = \$3T.asList($enums)",
            listEnumType,
            listVariable,
            Arrays::class.java,
            TrackerEnum::class.java
        )

        val mappingType = ParameterizedTypeName.get(
            ClassName.get(Map::class.java),
            ClassName.get(String::class.java),
            listEnumType
        )
        val mappingVariable = "mapping"
        addStatement(
            "\$T \$N = new \$T()",
            mappingType,
            mappingVariable,
            HashMap::class.java
        )

        map.forEach {
            addStatement("\$N.put(\$S, \$N)", mappingVariable, it, listVariable)
        }

        addStatement("this.\$N.mapExtraEvents(\$N)", TRACKER_FIELD, mappingVariable)
        return this
    }

    private fun MethodSpec.Builder.buildProperties(
        variableName: String,
        properties: Map<String, String>
    ): MethodSpec.Builder {
        if (properties.isEmpty()) return this
        addStatement(
            "\$T<String, String> \$N = new \$T()",
            Map::class.java,
            variableName,
            HashMap::class.java
        )
        properties.forEach { property ->
            addStatement("\$N.put(\$S, \$N)", variableName, property.key, property.value)
        }
        return this
    }
}
