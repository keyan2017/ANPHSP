睑结膜无创血红蛋白浓度预测系统
本项目是一个针对移动设备的睑结膜无创血红蛋白浓度预测系统，旨在降低患者血红蛋白浓度监测成本，辅助医生及时诊断和治疗患者。

文件说明
1. app
这个文件夹包含了系统端主要代码，是 Android 程序的源代码。

2. prediction
这个文件夹包含了血红蛋白浓度预测程序的代码。

3. segment
这个文件夹包含了眼部图像分割模型的代码。

如何使用
app 文件夹
确保你的开发环境支持 Android 应用程序的编译和运行。
克隆或下载本仓库到你的本地环境。
打开 Android Studio（或其他支持 Android 开发的 IDE）。
选择 "Open an existing Android Studio project"，然后导航到 app 文件夹，选择打开。
等待项目加载完毕。
连接你的 Android 设备，确保已开启开发者选项并启用 USB 调试。
在 Android Studio 中点击 "Run" 按钮，选择你的设备并运行应用程序。
prediction 和 segment 文件夹
这两个文件夹的代码是系统的核心部分，一般情况下不需要直接操作。它们被整合到 app 文件夹中，以供 Android 应用程序调用。

依赖安装
在 app 文件夹中，我们使用了 OpenCV 库来处理图像数据。因此，在编译和运行该应用程序之前，请确保已安装 OpenCV。

OpenCV 安装方法
打开项目中的 build.gradle 文件。
在 dependencies 部分添加以下行：
arduino
Copy code
implementation 'org.opencv:opencv-android:4.5.4'
点击 "Sync Now" 以同步项目依赖。
注意事项
在运行 Android 应用程序之前，请确保你的设备已启用 USB 调试，并已连接到计算机。
如果在使用过程中遇到任何问题，请随时联系我们的团队以获取支持。
