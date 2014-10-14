/**
 * 
 */
package de.rwth.aachen.comsys.androidcv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author Sam
 *
 */
public class FragmentWifi extends Fragment {

	// Debugging
    private static final String TAG = "FragmentWiFi";
    private static final boolean D = true;
    
    // UI
    private ListView lv;
    private List<String> files;
    private ProgressDialog progressDialog;
	
	// Download id returned by download manager
	private long enqueue;
	// Download manager
    private DownloadManager dm;
    // Address of the HTTP server
    private String hostAddress;
    // BroadcastReceiver
    private BroadcastReceiver mReceiver;
    
    private static final int PROGRESS = 0;

	public FragmentWifi() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// instantiate it within the onCreate method
		progressDialog = new ProgressDialog(getActivity());
		progressDialog.setMessage("A message");
		progressDialog.setIndeterminate(true);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(true);
		
		// Initialize the download manager
//		dm = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
		
		// Get the IP address of the connected WiFi
		// and generate the host address
		final WifiManager manager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		final DhcpInfo dhcp = manager.getDhcpInfo();
		@SuppressWarnings("deprecation")
		String gateway = Formatter.formatIpAddress(dhcp.gateway);
		hostAddress = "http://" + gateway + ":8000/";
		
		// AsyncTask to get list of files on the server
		new ParseFiles().execute(hostAddress);
		
		Log.d(TAG, hostAddress);
		
//		// Broadcast receiver to get notified after download is finished 
//		mReceiver = new BroadcastReceiver() {
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				
//				String action = intent.getAction();
//				if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
//					
//					Query query = new Query();
//					query.setFilterById(enqueue);
//					Cursor c = dm.query(query);
//					if (c.moveToFirst()) {
//						int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
//						if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
//
////							ImageView view = (ImageView) findViewById(R.id.imageView1);
////							String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
////							view.setImageURI(Uri.parse(uriString));
//						}
//					}
//				}
//			}
//		};
		
		// Register the "receiver" broadcast for 
		// ACTION_DOWNLOAD_COMPLETE notification
//		getActivity().registerReceiver(mReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		// UI view and ListView
		View view = inflater.inflate(R.layout.fragment_layout_wifi, container,false);
		lv = (ListView) view.findViewById(R.id.listView_files);
		
		// Tell the ActionaBar to use Fragment-Specific menu options
		setHasOptionsMenu(true);
		
		return view;
	}
	
	
	
    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	inflater.inflate(R.menu.wifi_menu, menu);
	}
    
    

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// handle item selection
		switch (item.getItemId()) {
		case R.id.wifi_download_action:
//			startDownload();
			startAsyncDownload();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void startAsyncDownload() {
		
		final DownloadTask downloadTask = new DownloadTask(getActivity());
		downloadTask.execute(files.toArray(new String[files.size()]));
		
	}

	@Override
	public void onPause() {
    	super.onPause();
//		getActivity().unregisterReceiver(mReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_list_item_1, files);
		lv.setAdapter(adapter);
		
		// Register the "receiver" broadcast for 
		// ACTION_DOWNLOAD_COMPLETE notification
//		getActivity().registerReceiver(mReceiver, 
//				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

	}

//	private void startDownload() {
//		
//			Log.d(TAG, "Download Clicked");
//			
//			for (String f : files) {
//				
//				Uri uri = Uri.parse(hostAddress + f);
//				Request request = new Request(uri);
//				
//				long start = System.nanoTime(); 
//		        enqueue = dm.enqueue(request);
//		        
//		        ProgressThread m = new ProgressThread(dm, mHander, getActivity(), enqueue);
//		        m.start();
//			}
//			
////			Uri uri = Uri.parse(hostAddress + "descriptor_set_00_80.points");
//////			Uri uri = Uri.parse(hostAddress + "test.mp4");
////			Request request = new Request(uri);
////	        enqueue = dm.enqueue(request);
////	        
////	        ProgressThread m = new ProgressThread(dm, mHander, getActivity(), enqueue);
////	        m.start();
//	}
    
//    private final Handler mHander = new Handler() {
//
//		@Override
//		public void handleMessage(Message msg) {
//			Log.d(TAG, Integer.toString(msg.arg1));
//		}
//    	
//    };
    
    /**
     * Thread to keep track of the download
     * progress and publish it to UI if needed
     */
//    private class ProgressThread extends Thread {
//    	
//    	private final DownloadManager mmDownloadManager;
//    	private final Handler         mmHandler;
//    	private final Context         mmContext;
//    	private final long            mmDownloadId;
//
//		public ProgressThread(DownloadManager mmDownloadManager,
//				Handler mmHandler, Context mmContext, long mmDownloadId) {
//			super();
//			this.mmDownloadManager = mmDownloadManager;
//			this.mmHandler         = mmHandler;
//			this.mmContext         = mmContext;
//			this.mmDownloadId      = mmDownloadId;
//		}
//
//
//		@Override
//		public void run() {
//			
//			boolean downloading = true;
//			
//			// Query the DownloadManager until it's done
//            while (downloading) {
//
//                DownloadManager.Query q = new DownloadManager.Query();
//                q.setFilterById(mmDownloadId);
//
//                Cursor cursor = mmDownloadManager.query(q);
//                cursor.moveToFirst();
//                
//                int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(
//                		DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
//                
//                int bytes_total = cursor.getInt(cursor.getColumnIndex(
//                		DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
//
//                if (cursor.getInt(cursor.getColumnIndex(
//                		DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
//                    downloading = false;
//                }
//
//                // Calculate the progress (0-100)
//                final int progress = (int) ((bytes_downloaded * 100l) / bytes_total);
//                
//                Log.d(TAG, "Progress: " + Integer.toString(progress));
//                
//                // Send the progress back to UI
//                mmHandler.obtainMessage(FragmentWifi.PROGRESS, progress, 0);
//                cursor.close();
//            }
//		}
//    	
//    }
//    
    private class ParseFiles extends AsyncTask<String, Void, ArrayList<String>> {

		@Override
		protected ArrayList<String> doInBackground(String... params) {
			
			files = new ArrayList<String>();
			
			try {
				
				Document doc  = Jsoup.connect(params[0]).get();
				Elements anchors = doc.select("a");
				
				for (Element anchor : anchors) {
					
					files.add( anchor.attr("href"));
					Log.d(TAG, "href: " + anchor.attr("href"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return (ArrayList<String>) files;
		}

		@Override
		protected void onPostExecute(ArrayList<String> result) {
			super.onPostExecute(result);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getActivity(), android.R.layout.simple_list_item_1, result);
			lv.setAdapter(adapter);
			
		}
		
    	
    }
    
    
    private class DownloadTask extends AsyncTask<String, Integer, String> {
    	
    	private Context context;
    	
		public DownloadTask(Context context) {
			super();
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog.show();
		}


		@Override
		protected String doInBackground(String... files) {
			
			InputStream in 				 = null;
	        OutputStream out 			 = null;
	        File file 					 = null;
	        HttpURLConnection connection = null;
	        
	        // Iterate through the list of arguments
			for (String f : files) {
				
				try {

					URL url = new URL(hostAddress + f);
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

					// Check for HTTP response code.
					if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						return "Server returned HTTP "
								+ connection.getResponseCode() + " "
								+ connection.getResponseMessage();
					}

					// this will be useful to display download percentage
					// might be -1: server did not report the length
					int fileLength = connection.getContentLength();

					// download the file
					in = connection.getInputStream();
					file = new File(context.getExternalFilesDir(null), f);
					out = new FileOutputStream(file);

					// Buffer
					byte[] data = new byte[in.available()];

					long total = 0;
					int count;
					
					for (int i=0; i < 100; i++) {
					
					// BENCHMARK START
					long t = System.nanoTime();
					
					while ((count = in.read(data)) != -1) {
						// allow canceling with back button
						if (isCancelled()) {
							in.close();
							return null;
						}

						total += count;
						
						out.write(data, 0, count);
						
						// Publishing the progress only if total length is known
						if (fileLength > 0)
							publishProgress((int) (total * 100 / fileLength));
					}
					
					// BENCHMARK END
					t = System.nanoTime() - t;
					double e = t / 1000000.0;
					Log.d(TAG, "File: " + f + 
							   " | Size: " + fileLength + " b" +
							   " | Downloaded in: " + String.valueOf(e) + " ms");

					
					}	
				} // End of try

				catch (Exception e) {
					return e.toString();
				}

				finally {
					try {

						// Close out the input and output stream
						if (in != null)
							in.close();
						if (out != null)
							out.close();
					} catch (Exception e) {
						return e.toString();
					}

					// Disconnect the HTTP connection
					if (connection != null)
						connection.disconnect();

				} // End of finally
				
			} // End of for each loop
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
			
			progressDialog.setIndeterminate(false);
	        progressDialog.setMax(100);
	        progressDialog.setProgress(values[0]);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			progressDialog.dismiss();
			
			if (result != null)
	            Toast.makeText(context,"Download error: "+ result, Toast.LENGTH_LONG).show();
	        else
	            Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
		}

    }
}