package ioio.examples.eightball;

import ioio.lib.api.RgbLedMatrix;
import ioio.lib.api.RgbLedMatrix.Matrix;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuInflater;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Vibrator;
import android.os.CountDownTimer;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.widget.ProgressBar;
import java.lang.Math;

//import android.app.AlertDialog;
//import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
//import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
//import android.content.Intent;
import android.content.res.Resources;
import android.view.MenuInflater;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.widget.ViewFlipper;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import android.util.Log;
import android.os.SystemClock;
import android.os.Message;

@SuppressLint("ParserError")
public class eightball extends IOIOActivity {
	private static final String LOG_TAG = "PixelEightball";	
	private RgbLedMatrix.Matrix KIND; 
	private short[] frame_;
	
	private SharedPreferences prefs;
	private String OKText;
	private short[] rgb_;
	
	private Bitmap frame1;
	private byte[] BitmapBytes;
	private InputStream BitmapInputStream;
	private ByteBuffer bBuffer;
	private ShortBuffer sBuffer;
	//private ShakeListener mSensorListener;
	private SensorManager mSensorManager;
	private Random randomGenerator = new Random();
	
	private MediaPlayer mediaPlayer;
	private AssetFileDescriptor intro1MP3;
	private AssetFileDescriptor intro2MP3;
	private AssetFileDescriptor transition1MP3;
	private AssetFileDescriptor notReadyMP3;
	
	private ReadOutTimer readoutTimer;
	private ResetTimer resetTimer;
	private ConnectTimer connectTimer; 
	private int readyFlag = 1;
	private boolean r_rated;
	private Resources resources;
	private String app_ver;	
	
	private int deviceFound = 0;
	private int random = 0;
	private int matrix_model;
	private boolean debug_;
    private int appAlreadyStarted = 0;
    private ioio.lib.api.RgbLedMatrix matrix_;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //force only portrait mode	
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
		readoutTimer = new ReadOutTimer(2000, 1000); 
		resetTimer = new ResetTimer(4000, 1000); 
		connectTimer = new ConnectTimer(15000,5000); //pop up a message if it's not connected by this timer
		OKText = getResources().getString(R.string.OKText);  
		 
		connectTimer.start(); //this timer will pop up a message box if the device is not found
		 
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	        
	        try
	        {
	            app_ver = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
	        }
	        catch (NameNotFoundException e)
	        {
	            Log.v(LOG_TAG, e.getMessage());
	        }
	        
	        //******** preferences code
	        resources = this.getResources();
	        setPreferences();
	        //***************************
	        
		 
		 playIntro();
		// loadImage();
	 
	
	}
	
	 public static short byte2short(byte[] data, int i)
	 {
	    return (short)((data[i]<<8) | (data[i+1]));
	 } 
	
	
	public void StartButtonEvent(View view) { //go here for beer icon click
		
	//mSensorManager.unregisterListener(mSensorListener); //we've started so kill the shake listener		
			//Vibrator myVib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			//myVib.vibrate(200);		
		
	//here let's do the random number generator and then pick an image to load
		
		if (readyFlag == 1) { //only go here if we're not already playing
		
				readyFlag = 0; //set this so we don't play until ready
				
				mediaPlayer.stop(); 
				mediaPlayer.setLooping(false);
		
				
				transition1MP3 = getResources().openRawResourceFd(R.raw.transition3); 
				 
				 if (transition1MP3 != null) {
		
			            mediaPlayer = new MediaPlayer();
			            try {
							mediaPlayer.setDataSource(transition1MP3.getFileDescriptor(), transition1MP3.getStartOffset(), transition1MP3.getLength());
						} catch (IllegalArgumentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            try {
			            	transition1MP3.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            try {
							mediaPlayer.prepare();
						} catch (IllegalStateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			            mediaPlayer.start();
			        }
				
					readoutTimer.start();
		}
		
		else { //play not ready sound
			
			//mediaPlayer.stop(); 
			//mediaPlayer.setLooping(false);
			
			notReadyMP3 = getResources().openRawResourceFd(R.raw.notready); 
			 
			 if (notReadyMP3 != null) {
	
				 mediaPlayer = new MediaPlayer();
		            try {
						mediaPlayer.setDataSource(notReadyMP3.getFileDescriptor(), notReadyMP3.getStartOffset(), notReadyMP3.getLength());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            try {
		            	notReadyMP3.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            try {
						mediaPlayer.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            mediaPlayer.start();
		        }
			
		}
		
	 }
	
	public class ReadOutTimer extends CountDownTimer
	{

		//private int p = 0;
		//private AnalogInput battery;	
		public ReadOutTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{						
			
			if (r_rated == true) {			
				 random = (int) Math.ceil(Math.random() * 25);			
			}
			else {
				 random = (int) Math.ceil(Math.random() * 24);		
			}

		    
			if (matrix_model == 2 || matrix_model == 3) {
				switch (random) {  //it's 32x32 matrix
			            case 1:
			            	//Toast.makeText(getBaseContext(), "1", Toast.LENGTH_LONG).show();
			            	 BitmapInputStream = getResources().openRawResource(R.raw.a1); //it is certain		            	
			            	break;
			            case 2:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a2);  //decididely sod
			                break;
			            case 3:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a3);		
			                break;
			            case 4:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a4);		
			                break;	                
			            case 5:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a5);		
			                break;    
			            case 6:
			            	 BitmapInputStream = getResources().openRawResource(R.raw.a6);			            	
			            	break;
			            case 7:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a7);		
			                break;
			            case 8:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a8);		
			                break;
			            case 9:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a9);		
			                break;	                
			            case 10:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a10);		
			                break;    
			            case 11:
			            	 BitmapInputStream = getResources().openRawResource(R.raw.a11);			            	
			            	break;
			            case 12:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a12);		
			                break;
			            case 13:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a13);		
			                break;
			            case 14:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a14);		
			                break;	                
			            case 15:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a15);		
			                break;    
			            case 16:
			            	 BitmapInputStream = getResources().openRawResource(R.raw.a16);			            	
			            	break;
			            case 17:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a17);		
			                break;
			            case 18:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a18);		
			                break;
			            case 19:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a19);		
			                break;	                
			            case 20:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a20);		
			                break;    
			            case 21:
			            	 BitmapInputStream = getResources().openRawResource(R.raw.a21);			            	
			            	break;
			            case 22:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a22);		
			                break;
			            case 23:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a23);		
			                break;
			            case 24:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a24);		
			                break;	                
			            case 25:
			            	BitmapInputStream = getResources().openRawResource(R.raw.a25);		
			                break; 
			      }		
			}
			else {  //it's a 32 x 16 matrix
				switch (random) {
	            case 1:
	            	//Toast.makeText(getBaseContext(), "1", Toast.LENGTH_LONG).show();
	            	 BitmapInputStream = getResources().openRawResource(R.raw.a); //it is certain		            	
	            	break;
	            case 2:
	            	BitmapInputStream = getResources().openRawResource(R.raw.b);  //decididely sod
	                break;
	            case 3:
	            	BitmapInputStream = getResources().openRawResource(R.raw.c);		
	                break;
	            case 4:
	            	BitmapInputStream = getResources().openRawResource(R.raw.d);		
	                break;	                
	            case 5:
	            	BitmapInputStream = getResources().openRawResource(R.raw.e);		
	                break;    
	            case 6:
	            	 BitmapInputStream = getResources().openRawResource(R.raw.f);			            	
	            	break;
	            case 7:
	            	BitmapInputStream = getResources().openRawResource(R.raw.g);		
	                break;
	            case 8:
	            	BitmapInputStream = getResources().openRawResource(R.raw.h);		
	                break;
	            case 9:
	            	BitmapInputStream = getResources().openRawResource(R.raw.i);		
	                break;	                
	            case 10:
	            	BitmapInputStream = getResources().openRawResource(R.raw.j);		
	                break;    
	            case 11:
	            	 BitmapInputStream = getResources().openRawResource(R.raw.k);			            	
	            	break;
	            case 12:
	            	BitmapInputStream = getResources().openRawResource(R.raw.l);		
	                break;
	            case 13:
	            	BitmapInputStream = getResources().openRawResource(R.raw.m);		
	                break;
	            case 14:
	            	BitmapInputStream = getResources().openRawResource(R.raw.n);		
	                break;	                
	            case 15:
	            	BitmapInputStream = getResources().openRawResource(R.raw.o);		
	                break;    
	            case 16:
	            	 BitmapInputStream = getResources().openRawResource(R.raw.p);			            	
	            	break;
	            case 17:
	            	BitmapInputStream = getResources().openRawResource(R.raw.q);		
	                break;
	            case 18:
	            	BitmapInputStream = getResources().openRawResource(R.raw.r);		
	                break;
	            case 19:
	            	BitmapInputStream = getResources().openRawResource(R.raw.s);		
	                break;	                
	            case 20:
	            	BitmapInputStream = getResources().openRawResource(R.raw.t);		
	                break;    
	            case 21:
	            	 BitmapInputStream = getResources().openRawResource(R.raw.u);			            	
	            	break;
	            case 22:
	            	BitmapInputStream = getResources().openRawResource(R.raw.v);		
	                break;
	            case 23:
	            	BitmapInputStream = getResources().openRawResource(R.raw.w);		
	                break;
	            case 24:
	            	BitmapInputStream = getResources().openRawResource(R.raw.x);		
	                break;	                
	            case 25:
	            	BitmapInputStream = getResources().openRawResource(R.raw.y);		
	                break; 
	      }		
	}
		    
			 loadImage();
			 try {
				matrix_.frame(frame_);
			} catch (ConnectionLostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //writes whatever is in bitmap raw 565 file buffer to the RGB LCD
				
			 resetTimer.start(); //wait four seconds and clear
					
			}
			

		@Override
		public void onTick(long millisUntilFinished)	{
					//do nothing here		
		}
	}	
	
	
	
	public void loadImage() {
		try {
			int n = BitmapInputStream.read(BitmapBytes, 0, BitmapBytes.length); // reads
																				// the
																				// input
																				// stream
																				// into
																				// a
																				// byte
																				// array
			Arrays.fill(BitmapBytes, n, BitmapBytes.length, (byte) 0);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int y = 0;
		for (int i = 0; i < frame_.length; i++) {
			frame_[i] = (short) (((short) BitmapBytes[y] & 0xFF) | (((short) BitmapBytes[y + 1] & 0xFF) << 8));
			y = y + 2;
		}

	}
	
	 
	   @Override
	    public boolean onCreateOptionsMenu(Menu menu) 
	    {
	       MenuInflater inflater = getMenuInflater();
	       inflater.inflate(R.menu.mainmenu, menu);
	       return true;
	    }

	    @Override
	    public boolean onOptionsItemSelected (MenuItem item)
	    {
	       
				  
		  if (item.getItemId() == R.id.menu_about) {
			  
			    AlertDialog.Builder alert=new AlertDialog.Builder(this);
		      	alert.setTitle(getString(R.string.menu_about_title)).setIcon(R.drawable.icon).setMessage(getString(R.string.menu_about_summary) + "\n\n" + getString(R.string.versionString) + " " + app_ver).setNeutralButton(OKText, null).show();	
		   }
	    	
	    	if (item.getItemId() == R.id.menu_prefs)
	       {
	    		
	    		Intent intent = new Intent()
	           		.setClass(this,
	           				ioio.examples.eightball.preferences.class);
	       
	           this.startActivityForResult(intent, 0);
	       }  
	       return true;
	    }
	
	
	@Override
	    public void onActivityResult(int reqCode, int resCode, Intent data) //we'll go into a reset after this
	    {
	    	super.onActivityResult(reqCode, resCode, data);
	    	setPreferences(); //very important to have this here, after the menu comes back this is called, we'll want to apply the new prefs without having to re-start the app
	    	
	    } 
	
	private void setPreferences() //here is where we read the shared preferences into variables
	    {
	     SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	     r_rated = prefs.getBoolean("pref_rrated", false);
	     
	     matrix_model = Integer.valueOf(prefs.getString(   //the selected RGB LED Matrix Type
	    	        resources.getString(R.string.selected_matrix),
	    	        resources.getString(R.string.matrix_default_value))); 
	     
	     debug_ = prefs.getBoolean("pref_debugMode", false);
	     
	     switch (matrix_model) {  //get this from the preferences
	     
	     case 0:
	    	 KIND = Matrix.SEEEDSTUDIO_32x16;
	    	 BitmapInputStream = getResources().openRawResource(R.raw.eightball2);
	    	 break;
	     case 1:
	    	 KIND = Matrix.ADAFRUIT_32x16;
	    	 BitmapInputStream = getResources().openRawResource(R.raw.eightball2);
	    	 break;
	     case 2:
	    	 KIND = Matrix.SEEEDSTUDIO_32x32_NEW; //v1
	    	 BitmapInputStream = getResources().openRawResource(R.raw.eightball2a);
	    	 break;
	     case 3:
	    	 KIND = Matrix.SEEEDSTUDIO_32x32; //v2
	    	 BitmapInputStream = getResources().openRawResource(R.raw.eightball2a);
	    	 break;
	     default:	    		 
	    	 KIND = Matrix.SEEEDSTUDIO_32x32; //v2 as the default
	    	 BitmapInputStream = getResources().openRawResource(R.raw.eightball2a);
	     }
	     
	     frame_ = new short [KIND.width * KIND.height];
		 BitmapBytes = new byte[KIND.width * KIND.height *2]; //512 * 2 = 1024 or 1024 * 2 = 2048
		 
		 loadImage();
	 }
	
	public class ResetTimer extends CountDownTimer
	{
		
			public ResetTimer(long startTime, long interval)
				{
					super(startTime, interval);
				}
	
			@Override
			public void onFinish()
				{						
				 //we've finished showing the message so let's go back to the original 8 ball graphic and play the starting sound	
				if (matrix_model == 0 || matrix_model == 1) {
					BitmapInputStream = getResources().openRawResource(R.raw.eightball2); //it's 16x32
				}
				else {
					BitmapInputStream = getResources().openRawResource(R.raw.eightball2a); //it's 32x32
				}
				
				 loadImage();	
				 try {
					matrix_.frame(frame_);
				} catch (ConnectionLostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //writes whatever is in bitmap raw 565 file buffer to the RGB LCD
					
				 playIntro();
				 readyFlag = 1;
				}
				
	
			@Override
			public void onTick(long millisUntilFinished)	{
					//do nothing here		
			}
	}	
	
	private void playIntro()  { //plays the intro sound
		 intro1MP3 = getResources().openRawResourceFd(R.raw.intro1); 
		 
		
		 
		 if (intro1MP3 != null) {

	            mediaPlayer = new MediaPlayer();
	            mediaPlayer.setLooping(true);
	            try {
					mediaPlayer.setDataSource(intro1MP3.getFileDescriptor(), intro1MP3.getStartOffset(), intro1MP3.getLength());
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            try {
					intro1MP3.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            try {
					mediaPlayer.prepare();
				} catch (IllegalStateException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            mediaPlayer.start();
	        }
	}
	
	public class ConnectTimer extends CountDownTimer
	{

		public ConnectTimer(long startTime, long interval)
			{
				super(startTime, interval);
			}

		@Override
		public void onFinish()
			{
				if (deviceFound == 0) {
					showNotFound (); 					
				}
				
			}

		@Override
		public void onTick(long millisUntilFinished)				{
			//not used
		}
	}
	
	private void showNotFound() {	
		AlertDialog.Builder alert=new AlertDialog.Builder(this);
		alert.setTitle("Not Found").setIcon(R.drawable.icon).setMessage("Please ensure Bluetooth pairing has been completed prior. The Bluetooth pairing code is: 4545.").setNeutralButton("OK", null).show();	
}

	class IOIOThread extends BaseIOIOLooper {
		//private RgbLedMatrix matrix_;

		@Override
		protected void setup() throws ConnectionLostException {
			matrix_ = ioio_.openRgbLedMatrix(KIND);
			deviceFound = 1; //if we went here, then we are connected over bluetooth or USB
			connectTimer.cancel(); //we can stop this since it was found
			
			matrix_.frame(frame_);  //write eightball image to the matrix
  			
  			if (debug_ == true) {  			
	  			showToast("Bluetooth Connected");
  			}
  			
  			//if (appAlreadyStarted == 1) {  //this means we were already running and had a IOIO disconnect so show let's show what was in the matrix
  				//WriteImagetoMatrix();
  			//}
  			
  			appAlreadyStarted = 1; 
		}

		@Override
		public void loop() throws ConnectionLostException {
		
			//matrix_.frame(frame_); //writes whatever is in bitmap raw 565 file buffer to the RGB LCD
					
			}	
		
		@Override
		public void disconnected() {
			Log.i(LOG_TAG, "IOIO disconnected");
			if (debug_ == true) {  			
	  			showToast("Bluetooth Disconnected");
  			}		
		}

		@Override
		public void incompatible() {  //if the wrong firmware is there
			//AlertDialog.Builder alert=new AlertDialog.Builder(context); //causing a crash
			//alert.setTitle(getResources().getString(R.string.notFoundString)).setIcon(R.drawable.icon).setMessage(getResources().getString(R.string.bluetoothPairingString)).setNeutralButton(getResources().getString(R.string.OKText), null).show();	
			showToast("Incompatbile firmware!");
			showToast("This app won't work until you flash the IOIO with the correct firmware!");
			showToast("You can use the IOIO Manager Android app to flash the correct firmware");
			Log.e(LOG_TAG, "Incompatbile firmware!");
		}
		}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new IOIOThread();
	}
	
	@Override
    public void onPause() {
		super.onPause();
		 switch (matrix_model) {  //get this from the preferences
	     case 0:
	    	 BitmapInputStream = getResources().openRawResource(R.raw.blank16);	
	    	 break;
	     case 1:
	    	 BitmapInputStream = getResources().openRawResource(R.raw.blank16);	
	    	 break;
	     case 2:
	    	 BitmapInputStream = getResources().openRawResource(R.raw.blank32);	
	    	 break;
	     case 3:
	    	 BitmapInputStream = getResources().openRawResource(R.raw.blank32);	
	    	 break;	 
	     default:	    		 
	    	 BitmapInputStream = getResources().openRawResource(R.raw.blank32);	
	     }		 
		loadImage(); //load the blank frame before we exit
		try {
			matrix_.frame(frame_);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
    }
	
	//@Override  //need to fix this later, the music is still on when home is pressed, adding this causes a crash
   // public void onStop() {
	//	super.onStop();
		    	
	//	mediaPlayer.stop();
		//mediaPlayer.release();
   // }
	
	@Override
    public void onDestroy() {
		super.onDestroy();
		resetTimer.cancel();  //if user closes the program, need to kill this timer or we'll get a crash
		readoutTimer.cancel();
		connectTimer.cancel();
    	
		mediaPlayer.stop();
		mediaPlayer.release();
    }
	
	 private void showToast(final String msg) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast toast = Toast.makeText(eightball.this, msg, Toast.LENGTH_LONG);
	                toast.show();
				}
			});
		}  
	

}