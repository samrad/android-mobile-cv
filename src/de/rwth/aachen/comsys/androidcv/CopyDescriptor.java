package de.rwth.aachen.comsys.androidcv;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

/**
 * @author Sam 
 * Copy file from asset folder to External/Internal storage
 * 
 */
public class CopyDescriptor {

	private Context context;

	public CopyDescriptor(Context context) {
		super();
		this.context = context;
	}

	public void copy() throws IOException {

		AssetManager assetManager = context.getAssets();
		File file = null;

		try {

			// List of files in asset/descriptor folder
			String[] files = assetManager.list("descriptors");

			// Filter files based on ".points" extension
			for (String f : files) {
				if (f.matches(".*\\.points") && isExternalStorageWritable()) {

					// Copy the files to Internal Storage
					InputStream in = new BufferedInputStream(
							assetManager.open("descriptors/" + f));
					
					// Uncomment to copy to internal storage
//					OutputStream osInternal = context.openFileOutput(f,
//							Context.MODE_PRIVATE);
					
					file = new File(context.getExternalFilesDir(null), f);
					OutputStream osExternal = new FileOutputStream(file);
					
					// Buffer
					byte[] data = new byte[in.available()];
					
					// Read the input stream
					in.read(data);
					
					// Write to output stream
//					osInternal.write(data);
					osExternal.write(data);
					
//					osInternal.close();
					osExternal.close();

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.w("ExternalStorage", "Error writing " + file, e);
		}

	}
	
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
