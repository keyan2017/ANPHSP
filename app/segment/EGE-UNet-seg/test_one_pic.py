import torch
from torch.utils.data import DataLoader
# import timm
# from datasets.dataset import NPY_datasets
# from tensorboardX import SummaryWriter
from models.egeunet import EGEUNet

from engine import *
import os
import sys

from utils import *
from configs.config_setting import setting_config
from PIL import Image

import warnings
warnings.filterwarnings("ignore")


def read_pic(config,file_path='train/images/'):
    
    model_cfg = config.model_config
    model_cfg = config.model_config
    if config.network == 'egeunet':
        model = EGEUNet(num_classes=model_cfg['num_classes'], 
                        input_channels=model_cfg['input_channels'], 
                        c_list=model_cfg['c_list'], 
                        bridge=model_cfg['bridge'],
                        gt_ds=model_cfg['gt_ds'],
                        )
    checkpoint_dir='results/egeunet_eyelid_Sunday_23_July_2023_15h_15m_29s/checkpoints/'
    if os.path.exists(os.path.join(checkpoint_dir, 'best.pth')):
        print('#----------Testing----------#')
        best_weight = torch.load(checkpoint_dir + 'best.pth', map_location=torch.device('cpu'))
        model.load_state_dict(best_weight)
    
    model.eval()
    
    transformer = config.test_transformer
    images_list = os.listdir(config.data_path+file_path)
    images_list = sorted(images_list)
    for i in range(len(images_list)):
        img_name=images_list[i]
        img_path = config.data_path+file_path + images_list[i]
        img_ori = np.array(Image.open(img_path).convert('RGB'))
        img, img = transformer((img_ori, img_ori))
        img=img.expand(1,3,256,256)
        img = img.float()
        gt_pre, out = model(img)
        out = out.squeeze(1).cpu().detach().numpy()
        
        save_imgs_all(img, out, config.work_dir + 'outputs/', config.datasets, config.threshold, test_data_name=img_name)

        
if __name__ == '__main__':
    config = setting_config
    read_pic(config,file_path='val/images/')