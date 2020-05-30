package com.example.processor

class DecoratorClassBuilder(private val className: String, private val packageName: String) {

    fun getContent(): String {
        return """
        package $packageName
        class $className {
             fun greeting() = "Hola Mundo!"
        }
    """.trimIndent()
    }
}
