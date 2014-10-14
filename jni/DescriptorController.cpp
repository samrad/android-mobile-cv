#include <DescriptorController.h>
#include <ObjectTracker.h>
#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <string>
#include <vector>
#include <fstream>
#include <dirent.h>
#include <errno.h>

#include <android/log.h>

#define LOG_TAG "JNI/DescriptorController"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;

//ObjectTracker objectTracker("FREAK", true, true);
static ObjectTracker m_objectTracker;
//ObjectTracker objectTracker("ORB", true, true);



void Java_de_rwth_aachen_comsys_androidcv_DescriptorController_nativeLoadDescriptor(
		JNIEnv* env, jobject obj, jstring path) {

	string nativePath = env->GetStringUTFChars(path, 0);
	LOGD("External Storage at: %s", nativePath.c_str());

	// Initialize the ObjectTracker
//	m_objectTracker = new ObjectTracker("ORG", true, true);
	m_objectTracker.loadDescriptor(nativePath);
	m_objectTracker.trainMatcher();

	env->DeleteLocalRef(path);

}

void Java_de_rwth_aachen_comsys_androidcv_DescriptorController_nativeTrainMatcher(
		JNIEnv* env, jobject obj, jstring a, jstring r) {

}


void Java_de_rwth_aachen_comsys_androidcv_DescriptorController_nativeFindMatch(
		JNIEnv* env, jobject obj, jlong addrGray, jlong addrRgba) {

	Mat& mGr = *(Mat*) addrGray;
	Mat& mRgb = *(Mat*) addrRgba;

//	vector<KeyPoint> v;
//	Mat d;
//
//	cv::ORB orb(500);
//	orb(mGr, cv::noArray(), v, d);

	//
//	Mat mGrScaled;
//	cv::resize(mGr, mGrScaled, Size(), 0.5, 0.5);
//	m_objectTracker->extractFeatures(mGrScaled, mRgb);

	m_objectTracker.extractFeatures(mGr, mRgb);
	m_objectTracker.getMatches();


}

void Java_de_rwth_aachen_comsys_androidcv_DescriptorController_nativeDestroy(JNIEnv* env) {

//	delete m_objectTracker;
//	m_objectTracker = NULL;
}
