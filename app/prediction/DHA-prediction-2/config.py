import os

class Config:
    def __init__(self):
    
        #data
        self.datanames = 'unet'
        self.data_folder = './datasets/'
        self.do_aug = True
        self.num_works = 4
        
        # train
        self.batch_size = 1
        self.lr = 1e-3
        self.epochs = 1000
        self.use_multiple_gpu=False
        self.device_ids=[0,1] if self.use_multiple_gpu else [0]
        self.pre_epoch = 0
        self.pre_iter = 0
        self.accuracy_threshold =1.5
        self.do_multiscale=False
        self.mode='train' if self.batch_size>1 else 'test'
        self.save_folder='./models/{}'.format(self.datanames)
        
        #net
        self.backbone = 'c3ae' #['c3ae','resnet18']
        self.input_size = 96 #96,128
        
        self.feat_dim=32
        self.min_hb = 6.5
        self.max_hb = 16.8  
        self.interval=0.1
        self.num_classes = round((self.max_hb - self.min_hb)/self.interval) + 1
        self.da_type = 'binary' #['binary','decimal', 'image_template']
        #self.image_template_path='./datasets/HB.npz'
        # self.image_template_path=''

        # self.pretrained_fn='./models/unet_c3ae_104_binary_1128/c3ae_epoch_30_ac_57.81-25.00.pth'
        self.pretrained_fn='./models/unet_c3ae_104_binary/c3ae_epoch_120_ac_71.09-56.25.pth'
        

        # self.pretrained_fn=''
        self.pretrained_ex_params=[]
       
