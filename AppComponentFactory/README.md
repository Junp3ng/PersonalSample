最近在Android14上开发，我们做了一个开机代理，结果发现系统总是能绕过我们的代理拉起Home。
后来发现SystemUI里的ThemeOverlayController服务启动时调用了AMS的setThemeOverlayReady方法，进而拉起Home。
我们尝试删除对应的ThemeOverlayController模块，结果没用。
然后我们发现这个编出来的SystemUI它不叫SystemUI.apk，它叫TvSystemUI.apk，最后发现TvSystemUI和SystemUI的区别就在于用的AppComponentFactory不同。
这个工程主要用于验证AppComponentFactory在App开发中的可用性。

## SystemUI和TvSystemUI
SystemUI是通过dagger图来管理各个组件的，其中根结点是由SystemUIInitializer提供的：
```java
public class SystemUIApplication extends Application implements
        SystemUIAppComponentFactoryBase.ContextInitializer {

    private SystemUIAppComponentFactoryBase.ContextAvailableCallback mContextAvailableCallback;
    // dagger根结点
    private SysUIComponent mSysUIComponent;
    private SystemUIInitializer mInitializer;

    @Override
    public void onCreate() {
        super.onCreate();
        // 获取SystemUIInitializer
        mInitializer = mContextAvailableCallback.onContextAvailable(this);
        // 从SystemUIInitializer中获取dagger根结点
        mSysUIComponent = mInitializer.getSysUIComponent();
        mBootCompleteCache = mSysUIComponent.provideBootCacheImpl();

        // 后面会从mSysUIComponent中获取需要启动的服务
    }

    // 这个方法继承自SystemUIAppComponentFactoryBase.ContextInitializer, 会在Application实例初始化的时候调用.
    @Override
    public void setContextAvailableCallback(
            SystemUIAppComponentFactoryBase.ContextAvailableCallback callback) {
        mContextAvailableCallback = callback;
    }
}
```

```kotlin
// 这个接口就是SystemUIInitializer的提供者
fun interface ContextAvailableCallback {
    /** Notifies when the Application Context is available.  */
    fun onContextAvailable(context: Context): SystemUIInitializer
}
```

SystemUIInitializer有两个子类，分别是`SystemUIInitializerImpl`和`TvSystemUIInitializer`。
`SystemUIInitializerImpl`实例由位于`frameworks/base/packages/SystemUI`的`PhoneSystemUIAppComponentFactory`提供：
```kotlin
class PhoneSystemUIAppComponentFactory : SystemUIAppComponentFactoryBase() {
    override fun createSystemUIInitializer(context: Context) = SystemUIInitializerImpl(context)
}
```

`TvSystemUIInitializer`实例由位于`packages/apps/TvSystemUI`的`TvSystemUIAppComponentFactory`提供:
```kotlin
class TvSystemUIAppComponentFactory : SystemUIAppComponentFactoryBase()  {
    override fun createSystemUIInitializer(context: Context) = TvSystemUIInitializer(context)
}
```

`SystemUIAppComponentFactoryBase`继承了`AppComponentFactory`:
```kotlin
abstract class SystemUIAppComponentFactoryBase : AppComponentFactory() {

    // 这个方法实际会在Application.onCreate时被调用
    protected abstract fun createSystemUIInitializer(context: Context): SystemUIInitializer

    private fun createSystemUIInitializerInternal(context: Context): SystemUIInitializer {
        return systemUIInitializer ?: run {
            val initializer = createSystemUIInitializer(context.applicationContext)
            try {
                initializer.init(false)
            } catch (exception: ExecutionException) {
                throw RuntimeException("Failed to initialize SysUI", exception)
            } catch (exception: InterruptedException) {
                throw RuntimeException("Failed to initialize SysUI", exception)
            }
            initializer.sysUIComponent.inject(
                this@SystemUIAppComponentFactoryBase
            )

            systemUIInitializer = initializer
            return initializer
        }
    }
    
    // Application实例初始化
    override fun instantiateApplicationCompat(cl: ClassLoader, className: String): Application {
        val app = super.instantiateApplicationCompat(cl, className)
        if (app !is ContextInitializer) {
            throw RuntimeException("App must implement ContextInitializer")
        } else {
            app.setContextAvailableCallback { context ->
                createSystemUIInitializerInternal(context)
            }
        }

        return app
    }
    // ......
}
```
所以用哪个SystemUIInitializer是看这个应用具体使用的`AppComponentFactory`配置的，更具体的是在`AndroidManifest.xml`的`<application android:appComponentFactory/>`配置的:
```xml
<manifest xmlns:tools="http://schemas.android.com/tools">
    <application
        android:appComponentFactory=".PhoneSystemUIAppComponentFactory"
        tools:replace="android:appComponentFactory"
    >
        
    </application>

</manifest>
```






