package com.jambofooddelivery.utils

// Define a dummy Trace class or import it if it exists in your project or dependencies.
// Assuming FirebasePerformance and bundleOf are platform-specific or need to be mocked/wrapped in commonMain.
// Since this is commonMain, we shouldn't reference Android/Firebase classes directly unless we have expect/actual or a wrapper.

// For now, creating a stub implementation to resolve errors in commonMain.
// You should implement this properly using expect/actual for Firebase Performance monitoring.

class PerformanceMonitor {

    fun startTrace(traceName: String): Trace {
        // Mock implementation for commonMain
        return Trace(traceName)
    }

    fun logScreenRender(screenName: String, duration: Long) {
        // Mock implementation
        println("Screen Render: $screenName took $duration ms")
    }
}

class Trace(private val name: String) {
    fun start() {
        println("Trace started: $name")
    }
    
    fun stop() {
        println("Trace stopped: $name")
    }
    
    fun putAttribute(name: String, value: String) {
         println("Trace attribute: $name = $value")
    }
}