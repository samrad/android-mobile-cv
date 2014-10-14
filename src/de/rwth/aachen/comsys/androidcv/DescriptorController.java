package de.rwth.aachen.comsys.androidcv;


/**
 * @author Sam
 *
 */
public class DescriptorController {
	
	// Path to descriptors directory
	private String     storagePath;
	
	private Algorithm  algo;
	private Refine     refine;
	private Matcher    matcher;
		
	// Constructor
	public DescriptorController(String storagePath) {
		this.storagePath = storagePath;
	}
	
	// Load descriptor files
	public void loadDescriptor() {
		nativeLoadDescriptor(storagePath);
	}
	
	// Prepare the matcher with train data
	public void trainMatcher(Algorithm a, Refine r) {
		algo   = a;
		refine = r;
		nativeTrainMatcher(algo.toString(), refine.toString());
		
	}
	
	// Destroy the C++ object (ObjectTracker)
	public void destroy() {
		nativeDestroy();
	}
	
	/*
	 * Find matches between query descriptors and train descriptors
	 * @param {long}      matAddrGr - address of gray image
	 * @param {long}      matAddrGr - address of rgba image
	 * @param {Algorithm} a         - detection and extraction algorithm
	 * @param {Refine}    r         - out-liers removal method
	 * @Param {Matcher}   m         - descriptor matcher
	 * 
	 */
	public void FindMatch(long matAddrGr, long matAddrRgba) {
		nativeFindMatch(matAddrGr, matAddrRgba);
	}

	private native void nativeLoadDescriptor(String path);
	private native void nativeTrainMatcher(String algo, String refine);
	private native void nativeFindMatch(long matAddrGr, long matAddrRgba);
	private native void nativeDestroy();
	
	public enum Algorithm {
		ORB, 
		FREAK, 
		SIFT
	}
	
	public enum Refine {
		CROSS,
		RATIO,
		BOTH
	}
	
	public enum Matcher {
		BF_NORM_HAMMING,
		BF_NORM_HAMMING2,
		BF_NORM_L2
	}
	
}
