# 睑结膜无创血红蛋白浓度预测系统

该系统旨在为移动设备提供一种无创的血红蛋白浓度预测方法，以降低患者监测成本，辅助医生诊断和治疗患者。

## 文件说明

- **app**: 包含系统端主要代码，为 Android 应用程序的源代码。
- **prediction**: 包含血红蛋白浓度预测程序的代码。
- **segment**: 包含眼部图像分割模型的代码。

## 如何使用

### app 文件夹

1. 确保你的开发环境支持 Android 应用程序的编译和运行。
2. 克隆或下载本仓库到你的本地环境。
3. 打开 Android Studio（或其他支持 Android 开发的 IDE）。
4. 选择 "Open an existing Android Studio project"，然后导航到 app 文件夹，选择打开。
5. 等待项目加载完毕。
6. 连接你的 Android 设备，确保已开启开发者选项并启用 USB 调试。
7. 在 Android Studio 中点击 "Run" 按钮，选择你的设备并运行应用程序。

### prediction 和 segment 文件夹

这两个文件夹的代码是系统的核心部分，一般情况下不需要直接操作。它们被整合到 app 文件夹中，以供 Android 应用程序调用。

## 依赖安装

在 app 文件夹中，我们使用了 OpenCV 库来处理图像数据。因此，在编译和运行该应用程序之前，请确保已安装 OpenCV。

### OpenCV 安装方法

1. 打开项目中的 `build.gradle` 文件。
2. 在 `dependencies` 部分添加以下行：
implementation 'org.opencv:opencv-android:4.5.4'
3. 点击 "Sync Now" 以同步项目依赖。
## 注意事项

- 在运行 Android 应用程序之前，请确保你的设备已启用 USB 调试，并已连接到计算机。
- 如果在使用过程中遇到任何问题，请随时联系我们的团队以获取支持。
