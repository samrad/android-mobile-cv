/*
 * ObjectTracker.cpp
 *
 *  Created on: May 15, 2014
 *      Author: Sam
 */

#include <ObjectTracker.h>
#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/nonfree/features2d.hpp>
#include <opencv2/nonfree/nonfree.hpp>

#include <string>
#include <vector>
#include <fstream>
#include <iterator>
#include <dirent.h>
#include <errno.h>
#include <pthread.h>

#include <android/log.h>

#define LOG_TAG "ObjectTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

using namespace std;
using namespace cv;


ObjectTracker::ObjectTracker(string alg, bool symmetry, bool ratio) {

	if (alg.compare("ORB") == 0) {
		m_alg = ORB;
	}
	else if (alg.compare("FREAK") == 0) {
		m_alg = FREAK;
	}
	else {
		m_alg = SIFT;
	}

	m_symmetryTest = symmetry;
	m_ratioTest    = ratio;

}

// sort using a custom function object
bool response_comparator(const KeyPoint& p1, const KeyPoint& p2) {
    return p1.response > p2.response;
}

//ObjectTracker::~ObjectTracker() {
//	// TODO Auto-generated destructor stub
//}

std::vector<std::string> ObjectTracker::getList(std::string path) {

	DIR* dir = opendir(path.c_str());
	vector<string> result;

	if (0 == dir) {
		LOGE("Unable to open directory. %d\n", errno);
	} else {
		struct dirent* entry;
		while (0 != (entry = readdir(dir))) {

			// Only list files with ".points" extension
			string entryName = entry->d_name;
			if (entryName.find(".points") != std::string::npos) {

				result.push_back(entryName);
				LOGD("Filename: %s\n", entryName.c_str());

			}
		}

		LOGD("Total %d file(s) listed.", result.size());
		LOGD("Done listing. %d\n", errno);
		closedir(dir);
	}

	std::sort(result.begin(), result.end());
	return result;
}

void ObjectTracker::loadDescriptor(std::string path) {

	// Get the list of available descriptors
	vector<string> list = getList(path);

	// Load each descriptor file into a cv::Mat and hold all them
	// in a container vector.
	for (vector<string>::iterator it = list.begin(); it != list.end(); ++it) {
		LOGD("File: %s\n", (*it).c_str());

		string absPath = path + "/" + *it;
		LOGD("PointFile: %s\n", absPath.c_str());

		// Input file stream to descriptor file
		std::ifstream descriptorStream(absPath.c_str(),
				std::ios::in | std::ios::binary);
		std::vector<unsigned char> descriptorFile(
				(std::istreambuf_iterator<char>(descriptorStream)),
				std::istreambuf_iterator<char>());

		// Load each binary file to a cv::Mat
		cv::Mat mat;
		if (m_alg == ORB) {
			// ORB descriptor is 32 bytes
			mat = cv::Mat((int) (descriptorFile.size() / 32), 32, CV_8UC1,
					descriptorFile.data());

		} else if (m_alg == FREAK) {
			// FREAK descriptor is 64 bytes
			mat = cv::Mat((int) (descriptorFile.size() / 64), 64, CV_8UC1,
					descriptorFile.data());

		} else if (m_alg == SIFT) {
			// SURF descriptor includes 64 elements
			// of type floats, apparently 4 bytes each
			cv::Mat mat1 = cv::Mat((int) (descriptorFile.size() / 256), 256, CV_8UC1,
					descriptorFile.data());

			mat = Mat::zeros((int) (descriptorFile.size() / 256), 64, CV_32F);
			for (int i = 0; i < mat.rows; i++) {
				for (int j = 0; j < mat.cols; j++) {

					memcpy(&mat.at<float>(i, j), &mat1.at<uchar>(i, j * 4),	sizeof(float));
//					LOGD("Element at (0,%d): %f", j, mat.at<float>(i, j));
				}
			}

		};
		m_trainDescriptor.push_back(mat.clone());

		LOGD("Descriptor Col: %d | Type: %d", m_trainDescriptor[0].cols, m_trainDescriptor[0].type());
		LOGD("Descriptor file size: %d", descriptorFile.size());
		LOGD("Train descriptor container size: %d", m_trainDescriptor.size());
	}

}

void ObjectTracker::trainMatcher() {


	// Initialize the matcher based on algorithm
	switch (m_alg) {
	case ORB:
		m_matcher = new cv::BFMatcher(cv::NORM_HAMMING2, false);
		break;
	case FREAK:
		m_matcher = new cv::BFMatcher(cv::NORM_HAMMING, false);
		break;
	case SIFT:
		m_matcher = new cv::BFMatcher(cv::NORM_L2, false);
		break;
	default:
		LOGE("Unknown matcher");
	}

	// Clear old train data
	m_matcher->clear();

	// Add train descriptors
	if (!m_trainDescriptor.empty()) {
//		LOGD("train mat rows: %d", m_trainDescriptor.front().rows);
		LOGD("Train vector size: %d", m_trainDescriptor.size());
//		m_matcher->add(m_trainDescriptor); // DAMN THIS KILLED ME
	}

//	m_matcher->train();

}

void ObjectTracker::extractFeatures(cv::Mat imageGray, cv::Mat imageRgb) {

	m_imageGray = imageGray;
	m_imageRgb  = imageRgb;

	double t; // Benchmark ticker

	// 1. Detect Keypoints and extract descriptors in scene
	// --> DETECTION AND EXTRACTION BENCHMARK START  <--
	t = (double) cv::getTickCount();

	switch (m_alg) {
	case ORB: {
		cv::ORB orb;
		orb(imageGray, cv::noArray(), m_queryKeypoints, m_queryDescriptor);
//		OrbFeatureDetector orb;
//		orb.detect(imageGray, m_queryKeypoints);
//
//		// Sort by response
//		sort(m_queryKeypoints.begin(), m_queryKeypoints.end(), response_comparator);
//		// Keep # of keypoints limited
//		if (m_queryKeypoints.size() > 500) {
//			m_queryKeypoints.resize(500);
//		}
//
//		OrbDescriptorExtractor orbExtr;
//		orbExtr.compute(imageGray, m_queryKeypoints, m_queryDescriptor);
	}
		break;
	case FREAK: {
	    cv::FAST(imageGray, m_queryKeypoints, 10, true);
//	    FastFeatureDetector detector(10, true);
//	    detector.detect(imageGray, m_queryKeypoints);

		// Sort by response
//		sort(m_queryKeypoints.begin(), m_queryKeypoints.end(), response_comparator);
//		// Keep # of keypoints limited
//		if (m_queryKeypoints.size() > 500) {
//			m_queryKeypoints.resize(500);
//		}

	    cv::FREAK freak(true, true);
	    freak.compute(imageGray, m_queryKeypoints, m_queryDescriptor);
	}
	    break;
	case SIFT: {  // "non-free" modules are not available on Android
//		cv::SIFT sift(80);
//		sift(imageGray, cv::noArray(), m_queryKeypoints, m_queryDescriptor);

		// Detection by FAST
		FastFeatureDetector detector(10, true);
		detector.detect(imageGray, m_queryKeypoints);

		// Sort by response
		sort(m_queryKeypoints.begin(), m_queryKeypoints.end(), response_comparator);
		// Keep # of keypoints limited
		if (m_queryKeypoints.size() > 500) {
			m_queryKeypoints.resize(500);
		}

//		cv::SURF surf(80);
//		surf(imageGray, cv::noArray(), m_queryKeypoints, m_queryDescriptor);

//		cv::SURF surf(400, 4, 2, false, false);
		SurfDescriptorExtractor surf;
		surf.compute(imageGray, m_queryKeypoints, m_queryDescriptor);
	}
		break;
	default:
		LOGE("Unknown algorithm");
	}

	t = ((double) cv::getTickCount() - t) / cv::getTickFrequency();
	// --> DETECTION AND EXTRACTION BENCHMARK END   <--

	LOGD("Took %f sec to detect keypoints and descriptors using %s",
			t /1.0, algToString().c_str());

	LOGD("Computed %d keypoints and %d descriptors for scene using %s",
			m_queryKeypoints.size(), m_queryDescriptor.rows, algToString().c_str());

	if (m_queryDescriptor.rows <= 0) {
		LOGD("Too few descriptors");
	}

	// Test to annotate features
//	for( unsigned int i = 0; i < m_queryKeypoints.size(); i++ )
//	    {
//	        const KeyPoint& kp = m_queryKeypoints[i];
//	        circle(imageRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(30,155,255,255));
//	    }
}

void ObjectTracker::getMatches() {

	if (m_queryDescriptor.rows <= 0 ) {
		return;
	}

	for (vector<Mat>::iterator trainIterator = m_trainDescriptor.begin();
			trainIterator != m_trainDescriptor.end(); ++trainIterator) {

		m_matcher->clear();

		double t;

		// To avoid Assertion Fail for the very first few frames
//		if (m_trainDescriptor[0].type() == m_queryDescriptor.type()
//				&& m_trainDescriptor[0].cols == m_queryDescriptor.cols)

		LOGD("Col: %d Row: %d Num: %d", m_queryDescriptor.cols, m_queryDescriptor.rows,
				m_queryKeypoints.size());

		if (trainIterator->type() == m_queryDescriptor.type()
				&& trainIterator->cols == m_queryDescriptor.cols)
		{

			// 2. Do the cross match between train and query descriptor
			// --> CORSS-MATCH BENCHMARK START  <--
			t = (double) cv::getTickCount();

			//	m_matcher->knnMatch(m_queryDescriptor, m_knnMatches1, 2);
//			m_matcher->knnMatch(m_queryDescriptor, m_trainDescriptor[0], m_knnMatches1, 2);
//			m_matcher->knnMatch(m_trainDescriptor[0], m_queryDescriptor, m_knnMatches2, 2);

			m_matcher->knnMatch(m_queryDescriptor, *trainIterator, m_knnMatches1, 2);
			m_matcher->knnMatch(*trainIterator, m_queryDescriptor, m_knnMatches2, 2);

			t = ((double) cv::getTickCount() - t) / cv::getTickFrequency();
			// --> CORSS-MATCH BENCHMARK END  <--
		}

		LOGD("Took %f sec to match descriptors", t / 1.0);
		LOGD("Found %d matches (train->query) and %d matches (query->train)",
				m_knnMatches1.size(), m_knnMatches2.size());


		// 3. Refine matches with ratio test
		// --> RATIO-TEST BENCHMARK START  <--
		t = (double) cv::getTickCount();

//		LOGD("knn1 before %d", m_knnMatches1.size());
		int removed = ratioRefine(m_knnMatches1);
		removed += ratioRefine(m_knnMatches2);
//		LOGD("knn1 after %d", m_knnMatches1.size());

		t = ((double) cv::getTickCount() - t) / cv::getTickFrequency();
		// --> RATIO-TEST BENCHMARK END  <--

		LOGD("Took %f sec to refine with ratio test", t / 1.0);
		LOGD("The ratio test removed %d descriptors", removed);

		// 4. Now lets see which descriptors survived and
		// if they correspond in the symmetrical case
		// --> SYMMETRY-TEST BENCHMARK START  <--
		t = (double) cv::getTickCount();

		symmetryRefine(m_knnMatches1, m_knnMatches2);
		t = ((double) cv::getTickCount() - t) / cv::getTickFrequency();
		// --> SYMMETRY-TEST BENCHMARK END  <--

		LOGD("Took %f sec to find symmetric descriptors", t / 1.0);
		LOGD("%d matches survived symmetry test", m_symMatches.size());

		if (m_symMatches.size() <= 0) {
			LOGD("Too few matches remaining");
		//		return cv::Point2f();
		} else {
			annotate();
		}
	}


}

int ObjectTracker::ratioRefine(vector<vector<DMatch> >& matches) {

	int removed = 0;

	if (m_ratioTest) {

		// To avoid NaN's when best match has zero distance we will use inversed ratio.
		const float minRatio = 1.f / 1.5f;
//		const float minRatio = 0.8f;

		for (vector<vector<DMatch> >::iterator matchIterator =
				matches.begin(); matchIterator != matches.end();
				++matchIterator) {

			// if 2 NN has been identified
			if (matchIterator->size() > 1) {

				// check distance ratio
				double distanceRatio = (*matchIterator)[0].distance
						/ (*matchIterator)[1].distance;
//				LOGD("DistanceRatio: %f", distanceRatio);
				if (distanceRatio > minRatio) {
					matchIterator->clear(); // remove match
					removed++;
				}
			}
		}
	}

	return removed;
}

void ObjectTracker::symmetryRefine(
		const vector<vector<DMatch> >& matches1,
		const vector<vector<DMatch> >& matches2) {

//	// for all matches image 1 -> image 2
//	for (vector<vector<DMatch> >::const_iterator matchIterator1 =
//			matches1.begin(); matchIterator1 != matches1.end();
//			++matchIterator1) {
//		// ignore deleted matches
//		if (matchIterator1->size() < 2)
//			continue;
//		// for all matches image 2 -> image 1
//		for (vector<vector<DMatch> >::const_iterator matchIterator2 =
//				matches2.begin(); matchIterator2 != matches2.end();
//				++matchIterator2) {
//			// ignore deleted matches
//			if (matchIterator2->size() < 2)
//				continue;
//			// Match symmetry test
//			if (((DMatch) (*matchIterator1)[0]).queryIdx
//					== ((DMatch) (*matchIterator2)[0]).trainIdx
//					&& ((DMatch) (*matchIterator2)[0]).queryIdx
//							== ((DMatch) (*matchIterator1)[0]).trainIdx) {
//				// add symmetrical match
//				m_symMatches.push_back(
//						DMatch(((DMatch) (*matchIterator1)[0]).queryIdx,
//								((DMatch) (*matchIterator1)[0]).trainIdx,
//								((DMatch) (*matchIterator1)[0]).distance));
//				break; // next match in image 1 -> image 2
//			}
//		}
//	}


	m_symMatches.clear();

	for (size_t m = 0; m < matches1.size(); m++) {
		bool findCrossCheck = false;
		for (size_t fk = 0; fk < matches1[m].size(); fk++) {
			DMatch forward = matches1[m][fk];

			for (size_t bk = 0; bk < matches2[forward.trainIdx].size(); bk++) {
				DMatch backward = matches2[forward.trainIdx][bk];
				if (backward.trainIdx == forward.queryIdx) {
					m_symMatches.push_back(forward);
					findCrossCheck = true;
					break;
				}
			}
			if (findCrossCheck)
				break;
		}
	}

}

void ObjectTracker::annotate() {

	const Scalar colors[] = {Scalar(255, 0, 0, 255),      // Red
			          	  	 Scalar(30, 203, 255, 255),   // Cyan
			          	  	 Scalar(0, 255, 0, 255),      // Green
			          	  	 Scalar(0, 0, 255, 255)       // Blue
	};

	const Scalar color = colors[rand() % 4];

	for (size_t i = 0; i < m_symMatches.size(); i++) {

		const KeyPoint& kp = m_queryKeypoints[m_symMatches[i].queryIdx];
//		circle(m_imageRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(30, 203, 255, 255)); // Color: Cyan
		circle(m_imageRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255, 0, 0, 255), 3);

		// Test
//		circle(m_imageRgb, Point(kp.pt.x * 2, kp.pt.y * 2), 10, Scalar(255, 0, 0, 255));

	}
}

std::string ObjectTracker::algToString() {

	string result;
	switch (m_alg) {
	case ORB:
		result =  "ORB";
		break;
	case FREAK:
		result =  "FREAK";
		break;
	case SIFT:
		result =  "SIFT";
		break;
	default:
		result =  "Unknown enumeration";
	};

	return result;

}



