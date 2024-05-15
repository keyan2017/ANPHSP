# 睑结膜无创血红蛋白浓度预测系统

## 简介

睑结膜无创血红蛋白浓度预测系统旨在降低患者血红蛋白浓度监测成本，为医生提供辅助诊断和治疗患者。该系统面向移动设备，通过拍摄采集的眼睛图片，进行血红蛋白精确浓度值的预测。它集成了多种图像处理技术、数据处理技术和软件开发技术，实现了人睑结膜图像采集及显示、眼部区域图像显示、血红蛋白浓度识别及结果显示、红蛋白浓度 mask 图像显示等功能。

## 文件说明

- **app**: 系统端主要代码，包括人睑结膜图像采集与显示、眼部区域图像显示等功能的实现。
- **prediction**: 预测程序，用于进行血红蛋白浓度的预测。
- **segment**: 分割模型程序，实现了眼部区域的图像分割。

## 使用说明

### 下载代码

您可以通过 Git 将本项目克隆到本地：
```bash
git clone https://github.com/your_username/your_repository.git
