import cv2
import numpy as np
import os


#非白区域最小外接矩阵裁剪
def cropped_image2(filename):
    image = cv2.imread(filename)
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    # 使用阈值将非白色区域设为255（白色），其他区域设为0（黑色）
    _, threshold = cv2.threshold(gray, 200, 255, cv2.THRESH_BINARY_INV)
    # 寻找非白色区域的轮廓
    contours, _ = cv2.findContours(threshold, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    if contours:
        largest_contour = max(contours, key=cv2.contourArea)
        x, y, w, h = cv2.boundingRect(largest_contour)
        # 裁剪图像
        cropped_image = image[y:y+h, x:x+w]
        return cropped_image,True
    else:
        return None, False

input_folder = "./datasets/unet/train/"   
output_folder = "./datasets/unet/train_crop/"
os.makedirs(output_folder, exist_ok=True)
for filename in os.listdir(input_folder):
    if filename.endswith(".jpg") :
        # 构建图像文件的完整路径
        input_path = os.path.join(input_folder, filename)    
        # 读取图像
        # image = cv2.imread(input_path)
        crop_image,result=cropped_image2(input_path)
        if result: 
         
            output_path = os.path.join(output_folder, filename)
            print(output_path)

        # 保存裁剪后的图像
            cv2.imwrite(output_path, crop_image)
        else:
            continue