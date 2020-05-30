package com.example.processor

class DecoratorClassBuilder(
    private val packageName: String,
    private val className: String,
    private val interfaceName: String
) {

    fun getContent(): String {
        return """
        package $packageName
        
        import com.example.tracking.Tracker
        import javax.inject.Inject
        
        class $className @Inject constructor(val tracker: Tracker): $interfaceName {
             
             override fun onMainScreenLoaded() {
                  tracker.track("onMainScreenLoaded", null)
             }
        
             override fun onButtonClicked(time: String) {
                  tracker.track("onButtonClicked", mapOf("time" to time))
             }
             
        }
    """.trimIndent()
    }
}
