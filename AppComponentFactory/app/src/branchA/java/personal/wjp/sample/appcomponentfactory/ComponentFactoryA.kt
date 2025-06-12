package personal.wjp.sample.appcomponentfactory

import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Intent
import android.util.Log
import androidx.core.app.AppComponentFactory
import personal.wjp.sample.appcomponentfactory.MyApplication
import personal.wjp.sample.appcomponentfactory.initializer.InitializerA

class ComponentFactoryA:AppComponentFactory() {
    private fun log(msg:String) {
        Log.d("WJP", "ComponentFactoryA: $msg")
    }
    override fun instantiateApplicationCompat(cl: ClassLoader, className: String): Application {
        log("instantiateApplicationCompat $className")
        val app = super.instantiateApplicationCompat(cl, className)
        if (app is MyApplication) {
            app.setInitializer { InitializerA() }
        }
        return app
    }

    override fun instantiateActivityCompat(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): Activity {
        log("instantiateActivityCompat $className")
        return super.instantiateActivityCompat(cl, className, intent)
    }

    override fun instantiateReceiverCompat(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): BroadcastReceiver {
        log("instantiateReceiverCompat $className")
        return super.instantiateReceiverCompat(cl, className, intent)
    }

    override fun instantiateServiceCompat(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): Service {
        log("instantiateServiceCompat $className")
        return super.instantiateServiceCompat(cl, className, intent)
    }

    override fun instantiateProviderCompat(cl: ClassLoader, className: String): ContentProvider {
        log("instantiateProviderCompat $className")
        return super.instantiateProviderCompat(cl, className)
    }
}