# import os
# import shutil

# # 设置原始文件夹路径和目标文件夹路径
# original_folder = 'D:/工作/论文/移动眼睑/code/mobile-hemo/data/conjunctiva_label/'
# target_folder = 'D:/工作/论文/移动眼睑/code/mobile-hemo/data/label/'

# # 遍历原始文件夹中的所有文件
# for filename in os.listdir(original_folder):
#     # 去掉文件名中_后面的内容
#     new_filename = filename.replace('_1', '')
#     # 构建原始文件路径和目标文件路径
#     original_path = os.path.join(original_folder, filename)
#     target_path = os.path.join(target_folder, new_filename)
#     # 重命名文件
#     shutil.move(original_path, target_path)
    

import os
from PIL import Image
import numpy as np

# 设置文件夹路径和图像大小
folder_path = 'D:/工作/论文/移动眼睑/code/mobile-hemo/data/eye'
image_size = (256, 256)

# 遍历文件夹中的所有图片
images = []
for filename in os.listdir(folder_path):
    # 读取图像并将其调整为指定大小
    image = Image.open(os.path.join(folder_path, filename)).resize(image_size)
    # 将图像转换为NumPy数组并添加到列表中
    images.append(np.array(image))

# 计算所有图像的mean和std
images = np.stack(images)
mean = np.mean(images, axis=(0, 1, 2))
std = np.std(images, axis=(0, 1, 2))

print('Mean:', mean)
print('Std:', std)