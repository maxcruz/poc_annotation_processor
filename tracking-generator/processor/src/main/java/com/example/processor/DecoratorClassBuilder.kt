package com.example.processor

class DecoratorClassBuilder(
    private val packageName: String,
    private val className: String,
    private val interfaceName: String
) {

    fun getContent(): String {
        return """
        package $packageName;
        
        import com.example.tracking.Tracker;
        import javax.inject.Inject;
        
        public class $className implements $interfaceName {
        
            private Tracker tracker;
            
            @Inject
            public $className(Tracker tracker) {
                this.tracker = tracker;
            }

            @Override
            public void onMainScreenLoaded() {
                  tracker.track("onMainScreenLoaded", null);
             }
        
             @Override
             public void onButtonClicked(String time) {
                  tracker.track("onButtonClicked", null);
             }
             
        }
    """.trimIndent()
    }
}
