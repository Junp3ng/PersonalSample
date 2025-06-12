package personal.wjp.sample.appcomponentfactory

import android.app.Application
import android.util.Log
import personal.wjp.sample.appcomponentfactory.initializer.Initializer

class MyApplication:Application() {
    private lateinit var initializerProvider:() -> Initializer
    private fun log(msg:String) {
        Log.d("WJP", "MyApplication: $msg")
    }
    override fun onCreate() {
        log("onCreate")
        super.onCreate()
        if (this::initializerProvider.isInitialized) {
            log("initializer name is ${initializerProvider.invoke().name}")
        }
    }

    fun setInitializer(provider: () -> Initializer) {
        initializerProvider = provider
    }
}