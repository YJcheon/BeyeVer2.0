#include <jni.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/core.hpp>

using namespace cv;


extern "C"
JNIEXPORT void JNICALL
Java_com_Beye_capstone_RoadActivity_ConvertRGBtoGray(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
    // RGB->Gray

    Mat &matInput = *(Mat *)matAddrInput;

    Mat &matResult = *(Mat *)matAddrResult;


    cvtColor(matInput, matResult, COLOR_RGBA2GRAY);



}
extern "C"
JNIEXPORT void JNICALL
Java_com_Beye_capstone_RoadActivity_Gaussian(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_RoadActivity_BinaryDilate(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_RoadActivity_BinaryEdge(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_RoadActivity_BlurImage(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        blur(matInput, matResult, Size(3, 3));

}
extern "C"
JNIEXPORT void JNICALL
Java_com_Beye_capstone_RoadActivity_ClosingFilter(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat element = getStructuringElement(MORPH_RECT, Size(7, 7), Point(3, 3));
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        morphologyEx(matInput, matResult, MORPH_CLOSE, element);
        cvtColor(matResult, matResult, COLOR_RGB2RGBA);

}
extern "C"
JNIEXPORT void JNICALL
Java_com_Beye_capstone_RoadActivity_Binary(JNIEnv *env, jobject instance,
                                                               jlong matAddrInput,
                                                               jlong matAddrResult) {
        Mat &matInput = *(Mat *)matAddrInput;
        Mat &matResult = *(Mat *)matAddrResult;
        threshold(matInput, matResult, 35, 255, THRESH_BINARY);

}

extern "C"
JNIEXPORT void JNICALL
Java_com_Beye_capstone_RoadActivity_Calforob(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_RoadActivity_Eraseroad(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_capstone_RoadActivity_NormalEdge(JNIEnv *env, jobject instance,
                                                          jlong matAddrInput) {
    Mat &matInput = *(Mat *)matAddrInput;
    Mat &matResult = *(Mat *)matAddrInput;
    Canny(matInput, matResult, 130, 210, 3);
    threshold(matResult, matResult, 0, 255, THRESH_BINARY_INV);
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_Calculateob(JNIEnv *env, jobject instance,
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
    if (count > 300) {
        return true;
    } else
        return false;
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_Calforst(JNIEnv *env, jobject instance,
                                                         jlong matAddrInput) {
    int count =0;
    Mat &matInput = *(Mat *) matAddrInput;
    for (int y = 1; y < matInput.rows; y++) {
        int x = matInput.cols/2;
        if (matInput.at<uchar>(y, x) == 255 && matInput.at<uchar>(y - 1, x) != 255) {
            if(matInput.at<uchar>(y, x-2) == 255 && matInput.at<uchar>(y, x+2) == 255){
                if(matInput.at<uchar>(y, x-1) == 255 && matInput.at<uchar>(y, x+1) == 255){
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
Java_com_Beye_capstone_RoadActivity_Calfordown(JNIEnv *env, jobject instance,
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
Java_com_Beye_capstone_RoadActivity_Watershed(JNIEnv *env, jobject instance,
                                                        jlong matAddrInput,
                                                        jlong matAddrInput1,
                                                        jlong matAddrResult){
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
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = *(Mat *) matAddrInput;
    cvtColor(matResult,matResult, COLOR_RGBA2RGB);
    cvtColor(matInput, matInput, COLOR_RGBA2RGB);
    cv::Mat blank(matInput.size(), CV_8U, cv::Scalar(0xFF));
    cv::Mat dest;
    Mat &magic2 = *(Mat *) matAddrInput1;
    cvtColor(magic2, magic2, COLOR_RGBA2RGB);
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
    //Rect(topleftcornerX, topleftcornerY, width, height);
    //top rectangle
    markers(Rect(0, 0, matInput.cols, 5)) = Scalar::all(1);
    //bottom rectangle
    markers(Rect(0, matInput.rows - 5, matInput.cols, 5)) = Scalar::all(1);
    //left rectangle
    markers(Rect(0, 0, 5, matInput.rows)) = Scalar::all(1);
    //right rectangle
    markers(Rect(matInput.cols - 5, 0, 5, matInput.rows)) = Scalar::all(1);
    //centre rectangle
    int centreW = matInput.cols / 2;
    int centreH = matInput.rows / 4;
    markers(Rect(xmar, matInput.rows/6*5, marindex, matInput.rows / 6)) = Scalar::all(2);
    markers.convertTo(markers, COLOR_RGBA2GRAY);
    //Create watershed segmentation object
    WatershedSegmenter segmenter;
    segmenter.setMarkers(markers);
    Mat wshedMask = segmenter.process(matInput);
    Mat mask;
    Mat eroding;

    convertScaleAbs(wshedMask, mask, 1, 0);
    erode(matInput, eroding, Mat(), Point(1, 1), 1);

    bitwise_and(matInput, eroding, dest, mask);
    dest.convertTo(dest, CV_8U);
    cvtColor(dest, matResult, COLOR_RGB2GRAY);
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_CalforCr(JNIEnv *env, jobject instance,
                                                        jlong matAddrInput){

    Mat &matInput = *(Mat *) matAddrInput;
    int maxy =0;
    int label;
    bool crossbit = false;
    bool checkbit;
    bool endbit = false;
    label = matInput.at<uchar>(matInput.rows,matInput.cols/2);
    for (int y = matInput.rows; y > 0 ; y--) {
        for (int x = matInput.cols; x > 0 ; x--) {
                if (label != matInput.at<uchar>(y,x)){
                    matInput.at<uchar>(y,x) = 0;
                }
                else{
                    matInput.at<uchar>(y,x) = 255;
                }
        }
    }

    for (int y = matInput.rows; y > 0 ; y--) {
        checkbit = false;
        for (int x = matInput.cols; x > 0; x--) {
            if(matInput.at<uchar>(y,x) == 255){
                checkbit = true;
            }
        }
        if(checkbit && !endbit){
            maxy = y;
        }
        if(!checkbit){
            endbit = true;
        }
    }
    if (maxy > matInput.rows / 10 * 7 ) {
        crossbit = true;

    }
    return crossbit;
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_Calfortick(JNIEnv *env, jobject instance,
                                                       jlong matAddrInput) {
    Mat &contrastImage = *(Mat *) matAddrInput;
    Mat dotblocks = contrastImage.clone();
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[0] < 150 && contrastImage.at<Vec3b>(y, x)[1] > 160 &&
                contrastImage.at<Vec3b>(y, x)[2] > 160) {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 255;
                dotblocks.at<Vec3b>(y, x)[2] = 255;
            } else {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 0;
                dotblocks.at<Vec3b>(y, x)[2] = 0;
            }

        }
    }
    int bottomyellowcounter = 0;
    int topyellowcounter = 0;
    for (int x = contrastImage.cols / 2; x < contrastImage.cols; x++) {
        for (int y = contrastImage.rows / 2; y < contrastImage.rows; y++) {
            if ((dotblocks.at<Vec3b>(y, x)[0] + dotblocks.at<Vec3b>(y, x)[1] +
                 dotblocks.at<Vec3b>(y, x)[2]) > 450) {
                bottomyellowcounter++;
            }
        }
    }
    for (int x = 0; x < contrastImage.cols / 2; x++) {
        for (int y = 0; y < contrastImage.rows / 2; y++) {
            if ((dotblocks.at<Vec3b>(y, x)[0] + dotblocks.at<Vec3b>(y, x)[1] +
                 dotblocks.at<Vec3b>(y, x)[2]) > 450) {
                topyellowcounter++;
            }
        }
    }
    if (topyellowcounter > bottomyellowcounter) {
        return true;
    } else {
        return false;
    }
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_Checksubwaystation(JNIEnv *env, jobject instance,
                                               jlong matAddrInput) {
    Mat &contrastImage = *(Mat *) matAddrInput;
    Mat gray;
    Mat erase_noise;
    Mat dotblocks = contrastImage.clone();
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[0] < 150 && contrastImage.at<Vec3b>(y, x)[1] > 160 &&
                contrastImage.at<Vec3b>(y, x)[2] > 160) {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 255;
                dotblocks.at<Vec3b>(y, x)[2] = 255;
            } else {
                dotblocks.at<Vec3b>(y, x)[0] = 0;
                dotblocks.at<Vec3b>(y, x)[1] = 0;
                dotblocks.at<Vec3b>(y, x)[2] = 0;
            }

        }
    }
    erase_noise = contrastImage.clone();
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[0] > 70 ||
                contrastImage.at<Vec3b>(y, x)[1] > 70 ||
                contrastImage.at<Vec3b>(y, x)[2] > 70) {
                erase_noise.at<Vec3b>(y, x)[0] = 255;
                erase_noise.at<Vec3b>(y, x)[1] = 255;
                erase_noise.at<Vec3b>(y, x)[2] = 255;
            } else if (contrastImage.at<Vec3b>(y, x)[0] + contrastImage.at<Vec3b>(y, x)[1] <
                       contrastImage.at<Vec3b>(y, x)[2]) {
                erase_noise.at<Vec3b>(y, x)[0] = 255;
                erase_noise.at<Vec3b>(y, x)[1] = 255;
                erase_noise.at<Vec3b>(y, x)[2] = 255;
            } else if (contrastImage.at<Vec3b>(y, x)[2] + contrastImage.at<Vec3b>(y, x)[1] <
                       contrastImage.at<Vec3b>(y, x)[0]) {
                erase_noise.at<Vec3b>(y, x)[0] = 255;
                erase_noise.at<Vec3b>(y, x)[1] = 255;
                erase_noise.at<Vec3b>(y, x)[2] = 255;
            } else if (contrastImage.at<Vec3b>(y, x)[0] + contrastImage.at<Vec3b>(y, x)[2] <
                       contrastImage.at<Vec3b>(y, x)[1]) {
                erase_noise.at<Vec3b>(y, x)[0] = 255;
                erase_noise.at<Vec3b>(y, x)[1] = 255;
                erase_noise.at<Vec3b>(y, x)[2] = 255;
            }

        }
        cvtColor(erase_noise, gray, COLOR_RGBA2GRAY);
        Mat subway;
        subway = gray.clone();
        for (int x = 0; x < gray.cols; x++) {
            for (int y = 0; y < gray.rows; y++) {
                if (gray.at<uchar>(y, x) < 70) {
                    subway.at<uchar>(y, x) = 255;
                } else {
                    subway.at<uchar>(y, x) = 0;
                }

            }
        }
        erode(subway, subway, Mat(), Point(1, 1), 1);
        Mat subway_color;
        cvtColor(subway, subway_color, COLOR_GRAY2BGR);
        Mat img_labels, centroids, stats;
        int area1, left1, top1, width1, height1;
        int numOfLabels = connectedComponentsWithStats(subway, img_labels, stats, centroids, 8,
                                                       CV_32S);
        bool exitOuterLoop = false;
        for (int j = 1; j < numOfLabels; j++) {
            area1 = stats.at<int>(j, CC_STAT_AREA);
            left1 = stats.at<int>(j, CC_STAT_LEFT);
            top1 = stats.at<int>(j, CC_STAT_TOP);
            width1 = stats.at<int>(j, CC_STAT_WIDTH);
            height1 = stats.at<int>(j, CC_STAT_HEIGHT);
            int x = centroids.at<double>(j, 0);
            int y = centroids.at<double>(j, 1);
            rectangle(subway_color, Point(left1, top1), Point(left1 + width1, top1 + height1),
                      Scalar(0, 0, 255), 1);
            for (int y = top1; y < top1 + height1; y++) {
                for (int x = left1; x < left1 + width1; x++) {
                    if (dotblocks.at<Vec3b>(y, x)[0] + dotblocks.at<Vec3b>(y, x)[1] +
                        dotblocks.at<Vec3b>(y, x)[2] != 0) {
                        exitOuterLoop = true;
                    }
                }
            }
        }
        return exitOuterLoop;
    }
}
extern "C"
JNIEXPORT bool JNICALL
Java_com_Beye_capstone_RoadActivity_Calculatetrafficlight(JNIEnv *env, jobject instance,
                                               jlong matAddrInput, jint left_global,
                                               jint top_global, jint width_global, jint height_global,
                                               jint count_global) {
    Mat &contrastImage = *(Mat *) matAddrInput;
    int &left = left_global;
    int &top = top_global;
    int &width = width_global;
    int &height = height_global;
    int &count = count_global;
    Mat traffic_sig;
    traffic_sig = contrastImage.clone();
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[0] < 100 && contrastImage.at<Vec3b>(y, x)[1] < 100 &&
                contrastImage.at<Vec3b>(y, x)[2] > 150 && y < contrastImage.rows / 2) {
                traffic_sig.at<Vec3b>(y, x)[0] = 0;
                traffic_sig.at<Vec3b>(y, x)[1] = 0;
                traffic_sig.at<Vec3b>(y, x)[2] = 255;
            } else {
                traffic_sig.at<Vec3b>(y, x)[0] = 0;
                traffic_sig.at<Vec3b>(y, x)[1] = 0;
                traffic_sig.at<Vec3b>(y, x)[2] = 0;
            }

        }
    }
    int loopcounter = 0;
    Mat traffic_gray;
    cvtColor(traffic_sig, traffic_gray, COLOR_RGB2GRAY);
    Mat img_labels, stats, centroids;
    int numOfLabels_t = connectedComponentsWithStats(traffic_gray, img_labels, stats, centroids, 8,
                                                     CV_32S);
    for (int j = 1; j < numOfLabels_t; j++) {
        int area_global1 = stats.at<int>(j, CC_STAT_AREA);
        int left_global1 = stats.at<int>(j, CC_STAT_LEFT);
        int top_global1 = stats.at<int>(j, CC_STAT_TOP);
        int width_global1 = stats.at<int>(j, CC_STAT_WIDTH);
        int height_global1 = stats.at<int>(j, CC_STAT_HEIGHT);
        if (count_global == 0 || abs(left_global - left_global1) < 10) {
            left = left_global1;
            top = top_global1;
            width = width_global1;
            height = height_global1;
        }
        int x = centroids.at<double>(j, 0);
        int y = centroids.at<double>(j, 1);
        loopcounter++;
    }
    bool red_flash_flag = true;
    bool green_flash_flag = false;
    for (int x = left_global; x < left_global + width_global; x++) {
        for (int y = top_global; y < top_global + height_global; y++) {
            if (traffic_sig.at<Vec3b>(y, x)[0] == 0 && traffic_sig.at<Vec3b>(y, x)[1] == 0 &&
                traffic_sig.at<Vec3b>(y, x)[2] == 255) {
                red_flash_flag = false;
            }
        }
    }
    for (int x = left_global; x < left_global + width_global; x++) {
        for (int y = top_global + height_global; y < contrastImage.rows; y++) {
            if (contrastImage.at<Vec3b>(y, x)[1] >
                (contrastImage.at<Vec3b>(y, x)[2] + contrastImage.at<Vec3b>(y, x)[0]) / 1.3) {
                traffic_sig.at<Vec3b>(y, x)[0] = 0;
                traffic_sig.at<Vec3b>(y, x)[1] = 255;
                traffic_sig.at<Vec3b>(y, x)[2] = 0;
            }
        }
    }
    for (int x = 0; x < contrastImage.cols; x++) {
        for (int y = 0; y < contrastImage.rows; y++) {
            if (traffic_sig.at<Vec3b>(y, x)[0] == 0 && traffic_sig.at<Vec3b>(y, x)[1] == 255 &&
                traffic_sig.at<Vec3b>(y, x)[2] == 0 && red_flash_flag) {
                green_flash_flag = true;
            }
        }
        if (green_flash_flag && red_flash_flag) {
            return true;
        } else {
            return false;
        }
    }
}
