#include <jni.h>
#include <string>
#include <jni.h>
#include <string>
#include <stdio.h>
#include <pthread.h>
#include <unistd.h>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include "onnxruntime/headers/onnxruntime_cxx_api.h"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG , "JNI", __VA_ARGS__)


using namespace cv;
using namespace dnn;
using namespace std;
using namespace Ort;
void mat2bitmap(JNIEnv *env, jobject bitmap,Mat &mat){
    //锁定画布
    void *pixels;
    AndroidBitmap_lockPixels(env,bitmap,&pixels);
    //获取Bitmap的信息
    AndroidBitmapInfo bitmapInfo;
    AndroidBitmap_getInfo(env,bitmap,&bitmapInfo);
    cv::Mat bitmapMat(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
    mat.copyTo(bitmapMat);//深拷贝
    AndroidBitmap_unlockPixels(env, bitmap);//解锁画布
}
cv::Mat DataNormalization(cv::Mat originalMat) {
    cv::Mat blob = cv::dnn::blobFromImage(originalMat, 1, cv::Size(256, 256), cv::Scalar(0,0,0), true, false,CV_32F);
    return blob;
}
Mat PostProcess(cv::Mat &frame,const std::vector<cv::Mat> &outs,cv::dnn::Net &net)//数据后处理
{
    cv::Mat frame_clone = frame.clone();
    int original_width = frame.cols;
    int original_height = frame.rows;
    float threshold = 0.5;
    cv::Size input_size(256, 256);
    cv::resize(frame, frame, input_size);
    cv::Mat segmentation_result = outs[5].reshape(1, {256,256});//outs 出来的格式是1*1*256*256 转成 1*256*256
    cv::threshold(segmentation_result, segmentation_result, threshold, 255, cv::THRESH_BINARY);  // 将通道二值化,二值化后可以将大于阈值的点都置为1，小于阈值的点都置为0，如果需要存图，可以置为255
    cv::imwrite("seg_res.jpg",segmentation_result);
    segmentation_result.convertTo(segmentation_result,CV_8U); //cv默认是CV32F 转成cv_8U
    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(segmentation_result, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);


    // 对每个轮廓点进行还原
    for (auto &contour : contours) {
        for (cv::Point &point : contour) {
            point.x = point.x * original_width / input_size.width;
            point.y = point.y * original_height / input_size.height;
        }
    }

    //将contours转成多边行进行输出
    std::vector<std::vector<cv::Point>> polygons;
    for (const auto& contour : contours) {
        std::vector<cv::Point> polygon;
        cv::approxPolyDP(contour, polygon, 3, true);  // 可选：对多边形进行近似处理,3表示精度，越小精度越高，点越多
        polygons.push_back(polygon);
    }
    double _area = 0.0;
    int _bestIndex = -1;
    for(int i = 0;i<polygons.size();i++)
    {
        if(_area<cv::contourArea(polygons[i]))
        {
            _area = cv::contourArea(polygons[i]);
            _bestIndex = i;
        }
    }
    cv::Mat mask = cv::Mat::zeros(frame_clone.size(), CV_8UC1);
    cv::drawContours(mask, contours, -1, cv::Scalar(255), cv::FILLED);
    cv::Mat result;
    cv::bitwise_and(frame_clone,frame_clone,result,mask);
    for(int r = 0;r<result.rows;r++)
    {
        for(int c=0;c<result.cols;c++)
        {
            if(!mask.at<uchar>(r,c)) //由于是通过mask模版蒙上去的，当mask的点为0时，result的区域为黑色，在这里将result的黑色改为白色
            {
                result.at<cv::Vec3b>(r,c) = cv::Vec3b(255, 255, 255);
            }

        }
    }
    return result;
}
CascadeClassifier eye_cascader;
Net seg;
Ort::Session session{nullptr};


const char* modelPathPre;

extern "C" JNIEXPORT void JNICALL
Java_com_example_hbdetect_Utils_DNNUtils_getOneEyeByIndex(
        JNIEnv* env,
        jobject /* this */, jobject bitmap,jobject Leye,jobject Reye) {
    if (bitmap == NULL) {

        return;
    }
    AndroidBitmapInfo bitmapInfo;
    memset(&bitmapInfo, 0, sizeof(bitmapInfo));
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
    void *pixels = NULL;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    Mat input;
    if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGBA_8888) {//mat的四通道
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
        image.copyTo(input);//深拷贝
    } else if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGB_565) {//mat的二通道CV_8UC2
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC2, pixels);
        image.copyTo(input);
    } else {

    }
    Mat gray;
    cvtColor(input,gray,COLOR_RGBA2GRAY);
    vector<Rect> res;
    for(int i = 10;i>0;i--) {
        eye_cascader.detectMultiScale(gray, res, 1.1, i, 0, Size(200, 200), Size(1500, 1500));
        if(res.size() >= 2)break;
    }
    jclass ans = env->FindClass("com/example/hbdetect/Eyes");
    jfieldID x = env->GetFieldID(ans, "x", "I");
    jfieldID y = env->GetFieldID(ans, "y", "I");
    jfieldID height = env->GetFieldID(ans, "height", "I");
    jfieldID width = env->GetFieldID(ans, "width", "I");

    if(res.size() == 0) {
        env->SetIntField(Leye, x, 0);
        env->SetIntField(Leye, y, 0);
        env->SetIntField(Leye, width, 1);
        env->SetIntField(Leye, height, 1);
        env->SetIntField(Reye, x, 0);
        env->SetIntField(Reye, y, 0);
        env->SetIntField(Reye, width, 1);
        env->SetIntField(Reye, height, 1);
    }else if(res.size() == 1){
        env->SetIntField(Leye, x, 0);
        env->SetIntField(Leye, y, 0);
        env->SetIntField(Leye, width, 1);
        env->SetIntField(Leye, height, 1);
        env->SetIntField(Reye, x, res[0].x);
        env->SetIntField(Reye, y, res[0].y);
        env->SetIntField(Reye, width, res[0].width);
        env->SetIntField(Reye, height, res[0].height);
    }else{
        env->SetIntField(Leye, x, res[0].x);
        env->SetIntField(Leye, y, res[0].y);
        env->SetIntField(Leye, width, res[0].width);
        env->SetIntField(Leye, height, res[0].height);
        env->SetIntField(Reye, x, res[1].x);
        env->SetIntField(Reye, y, res[1].y);
        env->SetIntField(Reye, width, res[1].width);
        env->SetIntField(Reye, height, res[1].height);
    }
    mat2bitmap(env, bitmap, input);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_hbdetect_Utils_DNNUtils_loadModel(JNIEnv *env, jobject thiz, jstring path1,
                                                            jstring path2, jstring path3) {
    const char *filePath_1 = env->GetStringUTFChars(path1, 0);
    const char *filePath_2 = env->GetStringUTFChars(path2, 0);
    const char *filePath_3 = env->GetStringUTFChars(path3, 0);
    eye_cascader.load(filePath_1);
    seg = readNetFromONNX(filePath_2);
    static Env environment(ORT_LOGGING_LEVEL_WARNING, "example-model-explorer");
    SessionOptions session_options;
    session = Session(environment, filePath_3, session_options);
    env->ReleaseStringUTFChars(path1, filePath_1);
    env->ReleaseStringUTFChars(path2, filePath_2);
    env->ReleaseStringUTFChars(path3, filePath_3);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_hbdetect_Utils_DNNUtils_Segmentation(JNIEnv *env, jobject thiz,
                                                             jobject bitmap) {
    if (bitmap == NULL) {
        return;
    }
    AndroidBitmapInfo bitmapInfo;
    memset(&bitmapInfo, 0, sizeof(bitmapInfo));
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
    void *pixels = NULL;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    Mat input;
    if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGBA_8888) {//mat的四通道
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
        image.copyTo(input);//深拷贝
    } else if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGB_565) {//mat的二通道CV_8UC2
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC2, pixels);
        image.copyTo(input);
    } else {
    }
    Mat image;
    cvtColor(input,image,COLOR_RGBA2BGR);
    cv::Mat blob = DataNormalization(image);
    seg.setInput(blob);
    std::vector<cv::Mat> net_output_img;
    seg.forward(net_output_img, seg.getUnconnectedOutLayersNames());
    Mat output = PostProcess(image,net_output_img,seg);
    cvtColor(output,output,COLOR_BGR2RGBA);
    mat2bitmap(env, bitmap, output);
}
std::vector<float> pre_process(cv::Mat& org) {
    cv::Mat blob = cv::dnn::blobFromImage(org, 1.0 / 255.0, cv::Size(96, 96), cv::Scalar(0, 0, 0), true, false, CV_32F);
    cv::Mat blob_three = blob.reshape(1, 96);
    return std::vector<float>(blob_three.reshape(1, 1));
}
int calculate_product(const std::vector<std::int64_t>& v) {
    int total = 1;
    for (auto& i : v) total *= i;
    return total;
}
template <typename T>
Ort::Value vec_to_tensor(std::vector<T>& data, const std::vector<std::int64_t>& shape) {
    Ort::MemoryInfo mem_info =
            Ort::MemoryInfo::CreateCpu(OrtAllocatorType::OrtArenaAllocator, OrtMemType::OrtMemTypeDefault);
    auto tensor = Ort::Value::CreateTensor<T>(mem_info, data.data(), data.size(), shape.data(), shape.size());
    return tensor;
}

extern "C"
JNIEXPORT float JNICALL
Java_com_example_hbdetect_Utils_DNNUtils_Prediction(JNIEnv *env, jobject thiz,
                                                           jobject bitmap) {
    if (bitmap == NULL) {

        return -1;
    }
    AndroidBitmapInfo bitmapInfo;
    memset(&bitmapInfo, 0, sizeof(bitmapInfo));
    AndroidBitmap_getInfo(env, bitmap, &bitmapInfo);
    void *pixels = NULL;
    AndroidBitmap_lockPixels(env, bitmap, &pixels);
    Mat input;
    if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGBA_8888) {//mat的四通道
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC4, pixels);
        image.copyTo(input);//深拷贝
    } else if (bitmapInfo.format == AndroidBitmapFormat::ANDROID_BITMAP_FORMAT_RGB_565) {//mat的二通道CV_8UC2
        Mat image(bitmapInfo.height, bitmapInfo.width, CV_8UC2, pixels);
        image.copyTo(input);
    } else {
    }
    cvtColor(input,input,COLOR_RGBA2RGB);


    Ort::AllocatorWithDefaultOptions allocator;
    std::vector<std::string> input_names;
    std::vector<std::int64_t> input_shapes;
    std::cout << "Input Node Name/Shape (" << input_names.size() << "):" << std::endl;
    for (std::size_t i = 0; i < session.GetInputCount(); i++) {
        input_names.emplace_back(session.GetInputNameAllocated(i, allocator).get());
        input_shapes = session.GetInputTypeInfo(i).GetTensorTypeAndShapeInfo().GetShape();
    }
    // some models might have negative shape values to indicate dynamic shape, e.g., for variable batch size.
    for (auto& s : input_shapes) {
        if (s < 0) {
            s = 1;
        }
    }

    std::vector<std::string> output_names;
    for (std::size_t i = 0; i < session.GetOutputCount(); i++) {
        output_names.emplace_back(session.GetOutputNameAllocated(i, allocator).get());
        auto output_shapes = session.GetOutputTypeInfo(i).GetTensorTypeAndShapeInfo().GetShape();
    }

    // Assume model has 1 input node and 1 output node.
    assert(input_names.size() == 1 && output_names.size() == 1);

    // Create a single Ort tensor of random numbers
    auto input_shape = input_shapes;
    auto total_number_elements = calculate_product(input_shape);

    // generate input tensor from MAT
    std::vector<float> input_tensor_values(total_number_elements);

    input_tensor_values = pre_process(input);

    // std::generate(input_tensor_values.begin(), input_tensor_values.end(), [&] { return rand() % 255; });
    std::vector<Ort::Value> input_tensors;
    input_tensors.emplace_back(vec_to_tensor<float>(input_tensor_values, input_shape));

    // double-check the dimensions of the input tensor
    assert(input_tensors[0].IsTensor() && input_tensors[0].GetTensorTypeAndShapeInfo().GetShape() == input_shape);
    // pass data through model
    std::vector<const char*> input_names_char(input_names.size(), nullptr);
    std::transform(std::begin(input_names), std::end(input_names), std::begin(input_names_char),
                   [&](const std::string& str) { return str.c_str(); });
    std::vector<const char*> output_names_char(output_names.size(), nullptr);
    std::transform(std::begin(output_names), std::end(output_names), std::begin(output_names_char),
                   [&](const std::string& str) { return str.c_str(); });
    try {
        auto output_tensors = session.Run(Ort::RunOptions{nullptr}, input_names_char.data(), input_tensors.data(),
                                          input_names_char.size(), output_names_char.data(), output_names_char.size());
        assert(output_tensors.size() == output_names.size() && output_tensors[0].IsTensor());
        float* output_data = output_tensors[0].GetTensorMutableData<float>();
        return output_data[0]*0.1+6.5;

    } catch (const Ort::Exception& exception) {
        LOGD("%s",exception.what());
        exit(-1);
    }
}