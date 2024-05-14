import pandas as pd
import shutil

# 读取CSV文件
data_train = pd.read_csv('D:/工作/论文/移动眼睑/code/eyelid-prediction/datasets/unet/train.csv')
data_test = pd.read_csv('D:/工作/论文/移动眼睑/code/eyelid-prediction/datasets/unet/val.csv')
# 复制图像到train文件夹中，并记录文件路径、hb值和数据类型
file_paths = []
hb_values = []
data_types = []
for index, row in data_train.iterrows():
    name = row['name']
    hb = row['hb']
    data_type = 'training'
    # image_path = f'conjunctiva/{name}.jpg'
    destination_path = f'train/{name}.jpg'
    # shutil.copy(image_path, destination_path)
    final_destination_path='unet/'+destination_path
    file_paths.append(final_destination_path)
    hb_values.append(hb)
    data_types.append(data_type)

for index, row in data_test.iterrows():
    name = row['name']
    hb = row['hb']
    data_type = 'testing'
    # image_path = f'conjunctiva/{name}.jpg'
    destination_path = f'test/{name}.jpg'
    # shutil.copy(image_path, destination_path)
    final_destination_path='unet/'+destination_path
    file_paths.append(final_destination_path)
    hb_values.append(hb)
    data_types.append(data_type)
    
# 生成新的CSV文件
new_data = pd.DataFrame({'age': hb_values, 'path': file_paths, 'type': data_types})
new_data.to_csv('D:/工作/论文/移动眼睑/code/eyelid-prediction/datasets/unet/unet.csv', index=False)
