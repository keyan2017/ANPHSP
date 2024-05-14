import torch
import torch.nn as nn
import torchvision.transforms as transforms
import numpy as np
import csv
import random
import math
import warnings
warnings.filterwarnings('ignore')
from PIL import Image
import cv2
import os,io
from torch.utils.data import DataLoader
import torchvision.transforms.functional as TF


class DataSetFactory:
    def __init__(self,config):
        self.config = config
        if self.config.data_folder[-1]!='/':
            self.config.data_folder = self.config.data_folder + '/'
        
        samples={}
        samples = self.read_eyelids()
        
        
            
        print('training size %d : testing size %d' % ( len(samples['training']), len(samples['testing'])))
        shape = (self.config.input_size, self.config.input_size)
        
        val_transform = transforms.Compose([
            # transforms.Resize(shape),
            transforms.ToTensor(),
            ]) 
        train_transform = transforms.Compose([            
             
            # transforms.RandomGrayscale(0.1),
            # transforms.ColorJitter(brightness=0.5, contrast=0.5, hue=0.5),
            # transforms.RandomRotation(degrees=(20)),
            # transforms.RandomHorizontalFlip(p=0.5),
            # transforms.Resize(shape),   
            transforms.ToTensor(),
            ]) 

        train_transform = train_transform if self.config.do_aug else val_transform
        
        
        if self.config.da_type=="image_template":
            if os.path.exists(self.config.image_template_path):
                info = np.load(self.config.image_template_path)
                images = info['rgb']
                labels = info['labels']
                self.template_images = torch.tensor(images)
                self.template_labels = torch.tensor(labels)
            else:
                select_idxs = samples['select_idxs']
                #print(select_idxs)
                labels, images = [],[]
                for select in select_idxs:
                    label, idx = select
                    image_fn = samples['training'][idx]['image']
                    rgb = Image.open(image_fn).convert('RGB')
                    rgb = val_transform(rgb)[None,...]
                    labels.append(samples['training'][idx]['gt_age'])
                    images.append(rgb)
                labels = np.array(labels)
                images = torch.cat(images, 0).numpy()
                image_template_path = '{}{}'.format(self.config.data_folder, self.config.datanames.replace(',','_'))
                np.savez_compressed(image_template_path, rgb=images, labels = labels)
                print('image_template path is: ', image_template_path)
                self.template_images = torch.tensor(images)
                self.template_labels = torch.tensor(labels)
            
        
        self.training = DataSet(transform=train_transform, samples=samples['training'], type_='training', resize_shape=self.config.input_size)
        self.testing = DataSet(transform=val_transform, samples=samples['testing'], type_='testing', resize_shape=self.config.input_size)
        
        print('dataset---->ok!')
            
        
        
    
    def random_choose_template(self, smaples_idx):
        select_idxs = []
        for i in range(self.config.num_classes):
            idxs = smaples_idx[i]
            if len(idxs)==0:
                continue
            #np.random.choice(range(len(idxs)), size = 1, replace=False)
            select_idxs.append([i, np.random.randint(len(idxs))])
        return select_idxs

    def read_eyelids(self):
        samples={}
        samples['training'] = []
        samples['testing'] = []
        eyelid_samples=[[] for k in range(self.config.num_classes)]
        for name in self.config.datanames.split(','):
        
            filename = self.config.data_folder + name + '/' + name + '.csv'
           
            with open(filename, 'r') as csvin:
                data = csv.reader(csvin)
                next(data)
                for row in data:
                    # age=int(float(row[0])+0.5)
                    hemoglobin=float(row[0])
                    if hemoglobin < self.config.min_hb or hemoglobin>self.config.max_hb :
                        continue
                    hemoglobin = max(hemoglobin, self.config.min_hb)
                    hemoglobin = min(hemoglobin, self.config.max_hb)
                    # hemoglobin = int((hemoglobin - self.config.min_hb)*(1/self.config.interval))
                    # mid=hemoglobin - self.config.min_hb
                    
                    hemoglobin = round((hemoglobin - self.config.min_hb)/self.config.interval)
                    sample={'gt_hemoglobin':hemoglobin}
                    
                    #row[2]: data path
                    image_fn =  row[1]
                    sample['image'] = image_fn if self.config.data_folder in image_fn else self.config.data_folder + image_fn
                    data_type= row[2] #['training' or 'testing']
                    samples[data_type].append(sample)
                    if data_type=='training':
                        eyelid_samples[int(hemoglobin)].append(len(samples[data_type])-1)
        
        if self.config.da_type=="image_template" and not os.path.exists(self.config.image_template_path):
            samples['select_idxs'] = self.random_choose_template(eyelid_samples)

        for k in range(self.config.num_classes):
            kk=k*self.config.interval +self.config.min_hb
            print(kk,len(eyelid_samples[k]))
            
        return samples



class DataSet(torch.utils.data.Dataset):

    def __init__(self, transform=None, samples=None, type_='training', resize_shape=None):
        self.transform = transform
        self.samples = samples
        self.resize_shape = resize_shape
        self.type_ = type_
    
    
    
    def crop_and_resize_data(self, img, s=8):
        width, height = img.size
        w = max(width,s)
        d = np.random.randint(1, 2*w//s)  if self.type_=='training' else w//s

        new_min_x, new_min_y = max(d, 0), max(d, 0)
        new_max_x, new_max_y = min(w - d, width), min(w - d, height)
        box = (new_min_x, new_min_y, new_max_x, new_max_y)
        ratio_w = 1./np.float32(new_max_x - new_min_x)
        ratio_h = 1./np.float32(new_max_y - new_min_y)
        box = (new_min_x, new_min_y, new_max_x, new_max_y)
        out_img = img.crop(box)
        return out_img

    def __getitem__(self, index):
        
        sample = self.samples[index]
        image_fn = sample['image']
        labels={}
        
        #opencv 
        img = cv2.imread(image_fn)
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        rgb = cv2.resize(img, (96, 96))
        
        #hsv 输入
        # img_hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
        # h_channel = img_hsv[:,:,0]
        # resized_img = cv2.resize(h_channel, (96, 96))
        
        # # 将S和V通道的值设置为H通道的值
        # img_hsv[:,:,1] = h_channel
        # img_hsv[:,:,2] = h_channel

        # rgb = Image.open(image_fn).convert('HSV')
        # img_array = np.array(rgb)
        # # 获取H通道的值
        # h_channel = img_array[:, :, 0]
        # # 将S和V通道的值设置为H通道的值
        # img_array[:, :, 1] = h_channel
        # img_array[:, :, 2] = h_channel
        # # 将numpy数组转换回图像
        # rgb = Image.fromarray(img_array, 'HSV')
        
        
        # tensor = torch.from_numpy(img_array)
        # print(tensor)
        
        
        # image = cv2.imread(image_fn)
        # rgb = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
        
        # rgb = self.crop_and_resize_data(rgb)
        # rgb = TF.to_tensor(rgb).convert('HSV')
        # rgb = TF.rgb_to_hsv(rgb)
       
        
        rgbs = self.transform(rgb)
        
        for k, v in sample.items():
            if k not in ['image']:
                labels[k] = torch.tensor(v).float()
        return rgbs, labels

    def __len__(self):
        return len(self.samples)



# class Config:
#     def __init__(self):
    
#         #data
#         self.datanames = 'unet'
#         self.data_folder = './datasets/'
#         self.do_aug = True
#         self.num_works = 4
        
#         # train
#         self.batch_size = 1
#         self.lr = 1e-3
#         self.epochs = 500
#         self.use_multiple_gpu=False
#         self.device_ids=[0,1] if self.use_multiple_gpu else [0]
#         self.pre_epoch = 0
#         self.pre_iter = 0
#         self.accuracy_threshold =1.5
#         self.do_multiscale=False
#         self.mode='train' if self.batch_size>1 else 'test'
#         self.save_folder='./models/{}'.format(self.datanames)
        
#         #net
#         self.backbone = 'c3ae' #['c3ae','resnet18']
#         self.input_size = 96 #96,128
        
#         self.feat_dim=32
#         self.min_hb = 6.5
#         self.max_hb = 16.8  
#         self.interval=0.5
#         self.num_classes = round((self.max_hb - self.min_hb)/self.interval) + 1
#         self.da_type = 'binary' #['binary','decimal', 'image_template']
#         #self.image_template_path='./datasets/HB.npz'
#         self.image_template_path=''

#         self.pretrained_fn='./models/unet_c3ae_12_binary/c3ae_epoch_25_ac_58.59-73.44.pth'
        

#         # self.pretrained_fn=''
#         self.pretrained_ex_params=[]
       
       

# if __name__ == "__main__":
#     cfg = Config()
    
#     factory           = DataSetFactory(cfg)
#     train_loader = DataLoader(factory.training, batch_size=12, shuffle=True,
#                                          num_workers=1, drop_last=True)
#     val_loader   = DataLoader(factory.testing, batch_size=1, shuffle=False, 
#                                            num_workers=1, drop_last=False)
#     for n, (images, labels) in enumerate(train_loader):
#         print(n)
        # print(labels)