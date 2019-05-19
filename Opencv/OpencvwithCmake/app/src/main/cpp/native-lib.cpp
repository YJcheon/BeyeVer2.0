#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>

using namespace cv;
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_opencvwithcmake_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

class WatershedSegmenter {
private:
    cv::Mat markers;
public:
    void setMarkers(cv::Mat& markerImage)
    {
        markerImage.convertTo(markers, CV_32S);
    }

    cv::Mat process(cv::Mat &image)
    {
        cv::watershed(image, markers);
        markers.convertTo(markers, CV_8U);
        return markers;
    }
};
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
    // RGB->Gray

    Mat &matInput = *(Mat *)matAddrInput;

    Mat &matResult = *(Mat *)matAddrResult;


    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);



}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_Gaussian(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
    //gaussian blur

    Mat &matInput = *(Mat *)matAddrInput;

    Mat &matResult = *(Mat *)matAddrResult;
    GaussianBlur(matInput, matResult, Size(0, 0), 3);
    cv::addWeighted(matInput, 1.5, matResult, -0.5, 0, matResult);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_BinaryDilate(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
    //binary & dilate filter

    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrResult;

 	threshold(matInput, matResult, 127, 255, THRESH_BINARY);
    dilate(matResult, matResult, Mat(), Point(1, 1), 1);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_BinaryEdge(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {

        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;

        threshold(matInput, matResult, 110, 255, THRESH_BINARY);
		Canny(matResult, matResult, 130, 210, 3);
		threshold(matResult, matResult, 0, 255, THRESH_BINARY_INV);
		erode(matResult, matResult, Mat(), Point(1, 1), 1);

		}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_BlurImage(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        blur(matInput, matResult, Size(3, 3));

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_ClosingFilter(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat element = getStructuringElement(MORPH_RECT, Size(7, 7), Point(3, 3));
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        morphologyEx(matInput, matResult, MORPH_CLOSE, element);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_Binary(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        threshold(matInput, matResult, 55, 255, THRESH_BINARY);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_Calforob(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput1,
                                                               jlong matAddrInput2,
                                                               jlong matAddrInput3,
                                                               jlong matAddrResult) {
        Mat &matInput1 = *(Mat *)matAddrInput1;
        Mat &matInput2 = *(Mat *)matAddrInput2;
        Mat &matResult = *(Mat *)matAddrResult;
        Mat label = matInput1.clone();
        Mat &origin = *(Mat *)matAddrInput3;
        erode(matInput2, label, Mat(), Point(1,1), 1);
        for (int y = 0; y < matInput1.rows; y++) {
        	for (int x = 0; x < matInput1.cols; x++) {
        		if ((matInput1.at<uchar>(y, x) - label.at<uchar>(y, x)) >(150)) {
        			matResult.at<uchar>(y, x) = (matInput1.at<uchar>(y, x) - label.at<uchar>(y, x));
        		}
        		else
        			matResult.at<uchar>(y, x) = 0;
        	}
        }
        Mat magic2 = matInput1.clone();
    	for (int y = 0; y < matInput1.rows; y++) {
    		for (int x = 0; x < matInput1.cols; x++) {
    			if ((matResult.at<uchar>(y, x) - label.at<uchar>(y, x)) >(200)) {
    				magic2.at<uchar>(y, x) = (matResult.at<uchar>(y, x) - label.at<uchar>(y, x));
    			}
    			else
    				magic2.at<uchar>(y, x) = 0;
    		}
    	}
    	matResult = magic2.clone();
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_Eraseroad(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        int k = 0;
        for (int y = 0; y < matInput.rows; y++) {
            for (int x = 0; x < matInput.cols; x++) {
         	    k = abs(matInput.rows - y);
         	    if (x < (matInput.cols / 5) + k / 2) {
         		    matResult.at<uchar>(y, x) = 0;
         	    }
         	    else if (x >(matInput.cols / 5 * 4) - k / 2) {
         		    matResult.at<uchar>(y, x) = 0;
         	    }
         	    else if (y < matInput.rows / 5*3){
         		    matResult.at<uchar>(y, x) = 0;
         	    }
            }
        }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_NormalEdge(JNIEnv *env, jobject instance,
                                                          jlong matAddrInput) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrInput;
    Canny(matInput, matResult, 130, 210, 3);
    threshold(matResult, matResult, 0, 255, THRESH_BINARY_INV);
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_example_opencvwithcmake_MainActivity_Calculateob(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput) {
    int count = 0;
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrInput;
    int k = 0;
    for (int y = 0; y < matInput.rows; y++) {
        for (int x = 0; x < matInput.cols; x++) {
            k = abs(matInput.rows - y);
            if (x < (matInput.cols / 5) + k / 2) {
                matResult.at<uchar>(y, x) = 0;
            } else if (x > (matInput.cols / 5 * 4) - k / 2) {
                matResult.at<uchar>(y, x) = 0;
            } else if (y < matInput.rows / 5 * 3) {
                matResult.at<uchar>(y, x) = 0;
            } else if (matResult.at<uchar>(y, x) != 0) {
                count++;
            }
        }
    }
    if (count > 200) {
        return true;
    } else
        return false;
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_example_opencvwithcmake_MainActivity_Calforst(JNIEnv *env, jobject instance,
                                                         jlong matAddrInput) {
    int count =0;
    Mat &matInput = *(Mat *) matAddrInput;
    for (int y = 1; y < matInput.rows; y++) {
        int x = matInput.cols/2;
        if (matInput.at<uchar>(y, x) == 255 && matInput.at<uchar>(y - 1, x) != 255) {
            if(matInput.at<uchar>(y, x-2) == 255 && matInput.at<uchar>(y, x+2) == 255){
                if(matInput.at<uchar>(y, x-1) = 255 && matInput.at<uchar>(y, x+1) == 255){
                    count++;
                }
            }
        }
    }
    if (count>2){
        return true;
    }
    else{
        return false;
    }
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_example_opencvwithcmake_MainActivity_Calfordown(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput) {

    bool countforfirst = false;
    bool countformax =false;
    int downindex = 0;
    int downy = 0;
    Mat &matInput = *(Mat *) matAddrInput;
    int downsomethingy =matInput.rows;
    for (int x = (matInput.cols / 5 * 2); x < (matInput.cols / 5 * 4); x++) {
        for (int y = (matInput.rows / 5 * 3); y < matInput.rows; y++) {
            if (matInput.at<uchar>(y, x) == 0)
                if (y < downsomethingy) {				{

                        downsomethingy = y;
                        countforfirst = true;
                    }
                }
        }
        if (countforfirst) {
            if (abs(downsomethingy - downy) < 4) {
                countformax = true;
            }
            downy = downsomethingy;
        }
        if (countformax) {
            downindex++;
        }
    }
    if(downindex>120){
        return true;
    }
    else{
        return  false;
    }
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_opencvwithcmake_MainActivity_Watershed(JNIEnv *env, jobject instance,
                                                        jlong matAddrInput,
                                                        jlong matAddrInput1,
                                                        jlong matAddrResult){
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matResult;
    cv::Mat blank(matInput.size(), CV_8U, cv::Scalar(0xFF));
    cv::Mat dest;
    Mat &magic2 = *(Mat *) matAddrInput1;
    int xmar = 0;
    int marindex = 0;
    int xmax=0;
    for (int y = (matInput.rows/6*5); y < matInput.rows; y++) {
        for (int x = (matInput.cols / 5*2); x > 0; x--) {
            if (magic2.at<uchar>(y, x) == 255) {
                if (x > xmar) {
                    xmar = x;
                }
            }

        }
    }
    for (int y = (matInput.rows/6*5); y < matInput.rows; y++) {
        for (int x = (matInput.cols / 5*2); x < matInput.cols; x++) {
            if (magic2.at<uchar>(y, x) == 255) {
                if (x-xmar < marindex) {
                    marindex = x-xmar;
                    xmax = x;
                }
                if (marindex == 0) {
                    marindex = x - xmar;
                    xmax = x;
                }
            }
        }
    }

    // Create markers image
    Mat markers(matInput.size(), CV_8U, cv::Scalar(-1));
    //centre rectangle
    int centreW = matInput.cols / 2;
    int centreH = matInput.rows / 4;
    markers(Rect(xmar, matInput.rows/6*5, marindex, matInput.rows / 6)) = Scalar::all(2);
    markers.convertTo(markers, COLOR_RGBA2GRAY);
    //Create watershed segmentation object
    WatershedSegmenter segmenter;
    segmenter.setMarkers(markers);
    cv::Mat wshedMask = segmenter.process(matInput);
    cv::Mat mask;
    Mat eroding;

    convertScaleAbs(wshedMask, mask, 1, 0);
    double thresh = threshold(mask, mask, 1, 255, THRESH_BINARY);
    erode(matInput, eroding, Mat(), Point(1, 1), 1);


    bitwise_and(matInput, eroding, dest, mask);
    dest.convertTo(dest, CV_8U);
    cvtColor(dest, matResult, COLOR_RGBA2GRAY);
    matResult = dest;
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_example_opencvwithcmake_MainActivity_CalforCr(JNIEnv *env, jobject instance,
                                                        jlong matAddrInput){

    Mat &matInput = *(Mat *) matAddrInput;
    int maxy =0;
    bool crossbit = false;
    for (int y = 0; y < matInput.rows; y++) {
        for (int x = 0; x < matInput.cols; x++) {
            if (matInput.at<uchar>(y, x) != 0) {
                if(maxy<y){
                    maxy =y;
                }
            }
        }
    }
    if (maxy > matInput.rows / 10 * 7 ) {
        crossbit = true;

    }
    return crossbit;
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_example_opencvwithcmake_MainActivity_Calfortick(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput) {
    Mat &contrastImage = *(Mat *) matAddrInput;
    Mat dotblocks = contrastImage.clone();
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[0] < 150 && contrastImage.at<Vec3b>(y, x)[1] > 160 && contrastImage.at<Vec3b>(y, x)[2] > 160) {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 255;
                dotblocks.at<Vec3b>(y, x)[2] = 255;
            }
            else {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 0;
                dotblocks.at<Vec3b>(y, x)[2] = 0;
            }

        }
    }
    int bottomyellowcounter=0;
    int topyellowcounter = 0;
    for (int x = contrastImage.cols/2; x < contrastImage.cols; x++) {
        for (int y = contrastImage.rows/2; y < contrastImage.rows; y++) {
            if ((dotblocks.at<Vec3b>(y, x)[0] + dotblocks.at<Vec3b>(y, x)[1] + dotblocks.at<Vec3b>(y, x)[2]) > 450) {
                bottomyellowcounter++;
            }
        }
    }
    for (int x = 0; x < contrastImage.cols/2; x++) {
        for (int y = 0; y < contrastImage.rows/2; y++) {
            if ((dotblocks.at<Vec3b>(y, x)[0] + dotblocks.at<Vec3b>(y, x)[1] + dotblocks.at<Vec3b>(y, x)[2]) > 450) {
                topyellowcounter++;
            }
        }
    }
    if (topyellowcounter > bottomyellowcounter ) {
        return true;
    }
    else{
        return false;
    }
}