package com.example.processor

import org.w3c.dom.Document
import java.io.StringWriter
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun String.camelToSnakeCase(): String {
    val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()
    return camelRegex.replace(this) { "_${it.value}" }.toLowerCase(Locale.US)
}

private const val ATTRIBUTE_ARRAY_NAME = "app_tracking_events"

object ResourceBuilder {

    fun buildTrackingResource(engines: List<String>, events: List<String>): Document {
        val builderFactory = DocumentBuilderFactory.newInstance()
        val documentBuilder = builderFactory.newDocumentBuilder()

        val document = documentBuilder.newDocument()
        val resources = document.createElement("resources")
        document.appendChild(resources)

        val arrays = document.createElement("arrays")
        arrays.setAttribute("name", ATTRIBUTE_ARRAY_NAME)
        resources.appendChild(arrays)

        val enginesString = engines.joinToString(",")
        events.forEach { event ->
            val array = document.createElement("item")
            array.appendChild(document.createTextNode("$event:$enginesString"))
            arrays.appendChild(array)
        }
        return document
    }
}
