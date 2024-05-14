# 项目名称

这是一个关于血红蛋白浓度预测系统的项目，它使用了一些特定的包和工具来实现某些功能。在这个 README 文件中，我们将提供一些关于如何使用和配置这个项目的信息。

## 使用说明
使用androidstudio进行开发，导入代码并安装opencv和程序所需包，进行运行
### 测试图片

所有的测试图片都应该放在 `/image` 文件夹下。你可以在这个文件夹中存放你需要用来测试的图片。

### 所需包

为了正常运行这个项目，你需要在你的项目中添加以下依赖：

```groovy
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
implementation group: 'org.apache.poi', name: 'poi-ooxml', version: '3.17'
implementation group: 'org.apache.xmlbeans', name: 'xmlbeans', version: '3.1.0'
implementation 'javax.xml.stream:stax-api:1.0'
implementation 'com.fasterxml:aalto-xml:1.2.2'
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.8.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
implementation 'androidx.navigation:navigation-fragment:2.5.3'
implementation 'androidx.navigation:navigation-ui:2.5.3'
implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
implementation 'org.jetbrains:annotations:15.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.3'
implementation 'com.isseiaoki:simplecropview:1.1.8'
implementation 'com.soundcloud.android:android-crop:1.0.1@aar'
