/*
 * ObjectTracker.h
 *
 *  Created on: May 15, 2014
 *      Author: Sam
 */

#ifndef OBJECTTRACKER_H_
#define OBJECTTRACKER_H_

#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <string>
#include <vector>


class ObjectTracker {

public:
	ObjectTracker
	(
			std::string alg = "SIFT",
			bool s          = true,
			bool r          = true
	);
//	virtual ~ObjectTracker();

	void                                      loadDescriptor(std::string path);
	void                                      trainMatcher();
	void									  extractFeatures(cv::Mat imageGray, cv::Mat imageRgb);
	void 									  getMatches();
	int  									  ratioRefine(std::vector<std::vector<cv::DMatch> >& matches);
	void   					                  symmetryRefine
	                                          (
	                                        		  const std::vector<std::vector<cv::DMatch> >& matches1,
	                                        		  const std::vector<std::vector<cv::DMatch> >& matches2
			                                  );
	void									  annotate();
	enum                                      Algorithm { ORB, FREAK, SIFT};

private:
	cv::Mat									  m_imageGray;
	cv::Mat									  m_imageRgb;
	std::vector<cv::KeyPoint>                 m_queryKeypoints;
	cv::Mat                                   m_queryDescriptor;
	std::vector<cv::Mat>                      m_trainDescriptor;
	std::vector<cv::DMatch>                   m_symMatches;
	std::vector<std::vector<cv::DMatch> >     m_knnMatches1;
	std::vector<std::vector<cv::DMatch> >     m_knnMatches2;

	cv::Ptr<cv::DescriptorMatcher>            m_matcher;

	bool                                      m_ratioTest;
	bool 									  m_symmetryTest;
	Algorithm                                 m_alg;

	std::vector<std::string>                  getList(std::string path);
	std::string                               algToString();

};

#endif /* OBJECTTRACKER_H_ */








