import numpy as np
import csv
import math
# from tensorboardX import SummaryWriter
import warnings
warnings.filterwarnings('ignore')
from PIL import Image

import torch
import torch.nn as nn
import torchvision.transforms as transforms
import os
from torchvision.transforms import ToTensor
from torch.utils.data import DataLoader
from Networks.DAANet import DAA
from EMA import EMA
from datasets.data_utils import DataSetFactory
import cv2
import pandas as pd
from sklearn.metrics import explained_variance_score
from sklearn.metrics import mean_absolute_error
from sklearn.metrics import mean_squared_error
from sklearn.metrics import r2_score
import onnxruntime as ort

import os
import csv
import matplotlib.pyplot as plt


def crop_and_resize_data(img, s=8):
    width, height = img.size
    w = max(width,s)
    d = w//s
    new_min_x, new_min_y = max(d, 0), max(d, 0)
    new_max_x, new_max_y = min(w - d, width), min(w - d, height)
    box = (new_min_x, new_min_y, new_max_x, new_max_y)
    # ratio_w = 1./np.float32(new_max_x - new_min_x)
    # ratio_h = 1./np.float32(new_max_y - new_min_y)
    box = (new_min_x, new_min_y, new_max_x, new_max_y)
    out_img = img.crop(box)
    return out_img

def readModeFile():
    folder_path='./models/unet_c3ae_104_binary'
    file_names = os.listdir(folder_path)
    
    for file_name in file_names:
        file_path = os.path.join(folder_path, file_name)
        print(file_path)
        
def readImagesFile():
    file_csv='./datasets/unet/val.csv' 
    file_images = './datasets/unet/test'
    shape=(96,96)
    val_transform = transforms.Compose([
            # transforms.Resize(shape),
            transforms.ToTensor(),
            ]) 
    
    with open(file_csv, 'r') as file:
        reader = csv.reader(file)
        next(reader)
    
    # 创建两个空数组用于存储数据
        file_name = []
        hb_value = []
        images=[]
    
    # 逐行读取CSV文件并将数据存储到数组中
        for row in reader:
            path = os.path.join(file_images, row[0]+'.jpg')
            file_name.append(path)
            hb_value.append(row[1])
            
            img = cv2.imread(path)
            img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
            img = cv2.resize(img, (96, 96))
            
            
            #PLT 图像处理
            # img=Image.open(path).convert('RGB')
            
            image=val_transform(img)
            image_test=torch.reshape(image,(1,3,96,96))
            images.append(image_test)    
    return file_name,hb_value,images     

class DetectPic(object):
    def __init__(self, config):
        self.config = config
        self.set_environment() 
        self.build_model()
        self.set_train_params()   
        self.load_model(self.config.pretrained_fn)
        # self.build_data_loader()
        self.model.eval()
        
        dummy_input = torch.randn(1, 3, 96, 96)
        onnx_file_path = 'DAAModel.onnx'
        torch.onnx.export(self.model , dummy_input, onnx_file_path,opset_version=11)
     
    def set_environment(self):    
        self.device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
        os.environ["CUDA_DEVICE_ORDER"] = "PCI_BUS_ID"
        os.environ['CUDA_LAUNCH_BLOCKING'] = '1'
        os.environ["CUDA_VISIBLE_DEVICES"] = ','.join([str(i) for i in self.config.device_ids])
        
    def set_train_params(self):
        self.init_lr    = self.config.lr
        self.lr         = self.init_lr
        self.epochs     = self.config.epochs
        self.optim      = torch.optim.Adam(self.model.parameters(), lr=self.lr, weight_decay=1e-3)
        self.ema        = EMA(self.model, 0.96)
        self.batch_size = self.config.batch_size
  

    def build_data_loader(self):        
        factory           = DataSetFactory(self.config)
        self.train_loader = DataLoader(factory.training, batch_size=self.batch_size, shuffle=True,
                                         num_workers=self.config.num_works, drop_last=True)
        self.val_loader   = DataLoader(factory.testing, batch_size=self.batch_size, shuffle=False, 
                                           num_workers=self.config.num_works//2, drop_last=False)
        
        self.val_iter     = iter(self.val_loader)
        
        if self.config.da_type=='image_template':
            self.template_images = factory.template_images.to(self.device).float()
            self.template_labels = factory.template_labels.to(self.device).float()
            if self.config.use_multiple_gpu:
                self.template_images = self.template_images.repeat(len(self.config.device_ids), 1, 1, 1)
                self.template_labels = self.template_labels.repeat(len(self.config.device_ids))

    def build_model(self):
        net_info = {
                    'da_type': self.config.da_type,
                    'feat_dim':self.config.feat_dim,
                    'backbone':self.config.backbone,
                    'num_classes': self.config.num_classes
                   }      
        self.model = DAA(net_info) 
        if self.config.use_multiple_gpu:
            self.model = torch.nn.DataParallel(self.model)
        self.model = self.model.to(self.device)

    def load_model(self, model_fn):
        if model_fn=='':
           return
        t = torch.cuda.is_available()
        state_dict = torch.load(model_fn) if t else torch.load(model_fn, map_location=lambda storage, loc: storage)
        
        
        try:
            self.optim.load_state_dict(state_dict['optimizer'])
            self.model.load_state_dict(state_dict['net'])
   
            return 
        except:
            pass
        
        state_dict = state_dict['net']
        model_dict = self.model.state_dict()
        
        for k,v in state_dict.items():
           model_dict[k] = model_dict[k].to(self.device)
            # print(k,v.shape)
        ex_list = self.config.pretrained_ex_params
        def ex_fun(k):
            for ex in ex_list:
                if ex in k:
                    return False
            return True
        predict='module.' if self.config.use_multiple_gpu else ''
        pretrained_dict = {k if 'module' in k else predict+k:v for k, v in state_dict.items() if ex_fun(k)}
        model_dict.update(pretrained_dict)
        self.model.load_state_dict(model_dict, strict=True)
        
        print('The model in path %s has been loaded successfully!'%model_fn)
    
    def run(self):
        self.model.eval()
        for n, (x_val, y_val) in enumerate(self.val_loader):
            images = x_val.to(self.device)
            hb = self.model(images)
            y_label = hb.detach().cpu().item()+self.config.min_hb
            gt = y_val['gt_hemoglobin'].detach().cpu().item()*self.config.interval+self.config.min_hb
            print('y_label:',y_label,'gt:',gt)
            
    def run_one_image(self,images):
        self.model.eval()
    
        images = images.to(self.device)
        
        test_pre = self.model(images)
        prediction_value = test_pre.detach().cpu().item()*self.config.interval+self.config.min_hb
        # print(prediction_value)
        return prediction_value
                                      
if __name__ == "__main__":

    from config import Config
    cfg = Config()
    
    file_name,hb_value,images =readImagesFile()
    dp=DetectPic(cfg)
    onnx_model_path = 'DAAModel.onnx'
    session = ort.InferenceSession(onnx_model_path)
    
    # input_data = np.random.randn(1, 3, 96, 96).astype(np.float32)
    # output = session.run(None, {'input.1': input_data})
    # print(output)
    
    
    for i,item in enumerate(file_name):
        p_value= dp.run_one_image(images[i])
        
        tt=images[i]
        
        input=images[i].numpy()
        
        # print(input)
        
        output = session.run(None, {'input.1':input})
        output=output[0]*cfg.interval+cfg.min_hb
        print(p_value,hb_value[i],output)
        
        # if i==200:
        #     break




    
        
        
            


   
