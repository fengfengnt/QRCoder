package com.android.cjcc.qrcoder;



import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.HybridBinarizer;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class QRDecoderActivity extends Activity implements SurfaceHolder.Callback{

	  private Camera mCamera01;
	  private Button mButton01, mButton02, mButton03;
	  private TextView mTextView02;
	  
	  private ImageView mImageView01;
	  private String TAG = "HIPPO";
	  private SurfaceView mSurfaceView01;
	  private SurfaceHolder mSurfaceHolder01;
	  
	  private boolean bIfPreview = false;
	  
	  private static final String DECODEFLAG = "!@"; 
//	  private boolean bIsDecode = false;
      Size mOptimalPreviewSize ;
      Size mOptimalPictureSize ;
      
      boolean mIsPortrait = false;
	  
	  private FaceTask mFaceTask ;
	  
	  private Handler autoFocusHandler; 
	  
	  private int takePictureType = 0;
	  private boolean canDecode = false;
	  
	  private Camera.PreviewCallback previewCallback;
	    /** �۽��¼��ڲ��� */  
	  private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback() {  
	          
	        @Override  
	        public void onAutoFocus(boolean success, Camera camera) {//����������۽��㷨  

	        	int maxcounter = 0;
	        	if(!success) {
	        		Log.e(TAG, "auto focus failed");
	        	}
	        	else {
		        	Log.d(TAG, "autofocus success");
	        	}
	            if (success && takePictureType < maxcounter) {  
	                takePictureType +=1;  
	                canDecode = false;
	            } else if (takePictureType >= maxcounter) {  
	                //takePicture();
	            	mCamera01.setOneShotPreviewCallback(previewCallback);
	            	canDecode = true;
	                takePictureType = 0;  
	            } else {  
	                takePictureType = 0;  
	                canDecode = false;
	            }  


	        	
	        	if(canDecode) {
	        		mCamera01.setOneShotPreviewCallback(previewCallback);
	        	}
	        	//auto focus interval
	        	int interval = 2000; 
	            autoFocusHandler.postDelayed(doAutoFocus, interval);// repeat after [interval] seconds
	        	        	
	        }  
	    };  
	      
	    /** �۽��߳� */  
	    private Runnable doAutoFocus = new Runnable() {  
	        @Override  
	        public void run() {  
	            if(bIfPreview){  
	                mCamera01.autoFocus(mAutoFocusCallback);  
	            }  
	        }  
	    }; 
	  
	  private Bitmap mImg;
	  private String mInfo = "";
	  private Handler handler=null;  
	  // ����Runnable������runnable�и��½���  
	  Runnable   runnableUi=new  Runnable(){  
	        @Override  
	        public void run() {  
	            //���½���  
	        	mImageView01.setImageBitmap(mImg);
	        	
	        	if(mInfo.contains(DECODEFLAG)) {
	        		String info = mInfo.substring(DECODEFLAG.length());
	        		String firstline = "Getting information from two-dimension code: \n";
	        		String secondline = info;
	        		String thirdline = "\nCamera is closed automatically";
	        		StringBuffer sb = new StringBuffer();
					sb.append(firstline);
					sb.append(secondline);
					sb.append(thirdline);
					SpannableStringBuilder ssb = new SpannableStringBuilder(sb.toString());
					int begin = 0;
					ssb.setSpan(new ForegroundColorSpan(Color.BLACK), begin, begin+firstline.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					begin = begin+firstline.length();
					ssb.setSpan(new ForegroundColorSpan(Color.RED), begin, begin+secondline.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					begin = begin+secondline.length();
					ssb.setSpan(new ForegroundColorSpan(Color.BLACK), begin, begin+thirdline.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
					begin = begin+thirdline.length();
					mTextView02.setText(ssb);
	        	}
	        	else {
	        		mTextView02.setText(mInfo);
	        	}
	        }  
	          
	    }; 
	  
	  /** Called when the activity is first created. */
	  @Override
	  public void onCreate(Bundle savedInstanceState)
	  {
	    super.onCreate(savedInstanceState);
	    
	  //�����������̵߳�handler  
        handler=new Handler();  
	    
        autoFocusHandler = new Handler();
        
        previewCallback = new PreviewCallback() {
            public void onPreviewFrame(byte[] _data, Camera camera)
            {               	                 	  
          	  if(null != mFaceTask){
          		  Status s = mFaceTask.getStatus();
                    switch(s){
                    case RUNNING:
                    	Log.d(TAG, "facetask is running");
                        return;
                    case PENDING:
                  	  Log.d(TAG, "facetask is pending");
                        mFaceTask.cancel(false);
                        break;
                        default:
                      	  Log.d(TAG, "facetask is " + s.toString());
                      	  break;
                    }
                }
          	    Log.d(TAG, "Let's run task");
                mFaceTask = new FaceTask(_data);
                mFaceTask.execute((Void)null);
            }
        };
        
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    
	    setContentView(R.layout.activity_qrdecoder);
	    DrawCaptureRect mDraw = new DrawCaptureRect
	    (
	      QRDecoderActivity.this,
	      // PORTRAIT
	      //110, 10, 100, 100,
	      190, 10, 100, 100,
	      //181, 1, 118, 118,
	      getResources().getColor(R.drawable.red)
	    );
	    //addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	    
	    //this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    
	    DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    
	    mTextView02 = (TextView)findViewById(R.id.myTextView2);
	    mTextView02.setMaxWidth(400);
	    mTextView02.setMaxLines(5);
	    
	    mImageView01 = (ImageView) findViewById(R.id.myImageView1);
	    
	    mSurfaceView01 = (SurfaceView) findViewById(R.id.mSurfaceView1);
	    
	    mSurfaceView01.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mCamera01 != null && bIfPreview) {
					mCamera01.autoFocus(mAutoFocusCallback);
				}
				
			}
		});
	    
	    if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	  LayoutParams lp = mSurfaceView01.getLayoutParams();
              lp.height = 240;
              lp.width = 320;
              mSurfaceView01.setLayoutParams(lp);
	    }
	    else  if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
	    	  mIsPortrait = true;
	    	  LayoutParams lp = mSurfaceView01.getLayoutParams();
              lp.height = 640;
              lp.width = 480;
              mSurfaceView01.setLayoutParams(lp);
	    }
	    
	    mSurfaceHolder01 = mSurfaceView01.getHolder();
	    
	    mSurfaceHolder01.addCallback(QRDecoderActivity.this);
	    
	    //mSurfaceHolder01.setFixedSize(160, 120);
	      
	    /*
	     * �HSURFACE_TYPE_PUSH_BUFFERS(3)
	     * �@
	     * */
	    mSurfaceHolder01.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    
	    mButton01 = (Button)findViewById(R.id.myButton1);
	    mButton02 = (Button)findViewById(R.id.myButton2);
	    mButton03 = (Button)findViewById(R.id.myButton3);
	    
	    mButton01.setOnClickListener(new Button.OnClickListener()
	    {
	      @Override
	      public void onClick(View arg0)
	      {
	        // TODO Auto-generated method stub
	       
	        initCamera();
	        mInfo = "In processing...";
	        mTextView02.setText(mInfo);
	      }
	    });
	    
	    mButton02.setOnClickListener(new Button.OnClickListener()
	    {
	      @Override
	      public void onClick(View arg0)
	      {
	        // TODO Auto-generated method stub
	        resetCamera();
	      }
	    });
	    
	    mButton03.setOnClickListener(new Button.OnClickListener()
	    {
	      @Override
	      public void onClick(View arg0)
	      {
	        // TODO Auto-generated method stub
	        takePicture();
	      }
	    });
	  }
	
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			// TODO Auto-generated method stub
			previewCallback = null;
			mAutoFocusCallback = null;
		}
		
		 class DrawCaptureRect extends View
		  {
		    private int colorFill;
		    private int intLeft,intTop, intWidth,intHeight;
		    Paint mPaint01 = new Paint();
		    
		    public DrawCaptureRect(Context context, int intX, int intY, int intWidth, int intHeight, int colorFill)
		    {
		      super(context);
		      this.colorFill = colorFill;
		      this.intLeft = intX;
		      this.intTop = intY;
		      this.intWidth = intWidth;
		      this.intHeight = intHeight;
		    }
		    
		    
		    @Override
		    protected void onDraw(Canvas canvas)
		    {
		      mPaint01.setStyle(Paint.Style.FILL);
		      mPaint01.setColor(colorFill);
		      mPaint01.setStrokeWidth(1.0F);

		      canvas.drawLine(this.intLeft, this.intTop, this.intLeft+intWidth, this.intTop, mPaint01);
		      canvas.drawLine(this.intLeft, this.intTop, this.intLeft, this.intTop+intHeight, mPaint01);
		      canvas.drawLine(this.intLeft+intWidth, this.intTop, this.intLeft+intWidth, this.intTop+intHeight, mPaint01);
		      canvas.drawLine(this.intLeft, this.intTop+intHeight, this.intLeft+intWidth, this.intTop+intHeight, mPaint01);
		      super.onDraw(canvas);
		    }
		  }
		 
		 class AndroidQRCodeImage implements QRCodeImage
		  {
		    Bitmap image;
		    
		    public AndroidQRCodeImage(Bitmap image)
		    {
		      this.image = image;
		    }
		    
		    public int getWidth()
		    {
		      return image.getWidth();
		    }
		    
		    public int getHeight()
		    {
		      return image.getHeight();
		    }
		    
		    public int getPixel(int x, int y)
		    {
		      return image.getPixel(x, y);
		    }   
		  }
		 
		 private void initCamera()
		  {
		    if(!bIfPreview)
		    {
		      mCamera01 = Camera.open();
		    }
		    
		    if (mCamera01 != null && !bIfPreview)
		    {
		      Log.i(TAG, "inside the camera");
		      
	
		      
		      Camera.Parameters parameters = mCamera01.getParameters();
		      
		      parameters.setPreviewFormat(ImageFormat.NV21);
		      parameters.setPictureFormat(PixelFormat.JPEG);
		      
		     //����Ԥ����֡��,��Ӳ��Ӱ��.
		      //parameters.setPreviewFrameRate(10);
		      
		      List<Size> supportedPreviewSizes = mCamera01.getParameters().getSupportedPreviewSizes();
		      List<Size> supportedPictureSizes = mCamera01.getParameters().getSupportedPictureSizes();
		      WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		      Display display = windowManager.getDefaultDisplay();
				DisplayMetrics displayMetrics = new DisplayMetrics();
				display.getMetrics(displayMetrics);
		      mOptimalPreviewSize = getOptimalPreviewSize(supportedPreviewSizes, display.getWidth(), display.getHeight());
		      {
		    	  mOptimalPictureSize = supportedPictureSizes.get(0);
				  int maxSize = 1280;
				  if(maxSize > 0){
						for(Size size : supportedPictureSizes){
							if(maxSize >= Math.max(size.width,size.height)){
								mOptimalPictureSize = size;
								break;
							}
						}
					}
		      }
		      
		      mOptimalPreviewSize.width = 640;   //do not use optimal size temporarily
		      mOptimalPreviewSize.height = 480;
		      parameters.setPreviewSize(mOptimalPreviewSize.width, mOptimalPreviewSize.height);
		      parameters.setPictureSize(mOptimalPictureSize.width, mOptimalPictureSize.height);
		      
		      parameters.setJpegQuality(100);
		      parameters.setJpegThumbnailQuality(100);

		      mCamera01.setParameters(parameters);
		      

		      
		      int o = getCameraDisplayOrientation(QRDecoderActivity.this);
		      mCamera01.setDisplayOrientation(o);
		      
		      Camera.Size previewSize = mCamera01.getParameters().getPreviewSize();  
		      if (o == 90 || o == 270) {  
		          // swap - the physical camera itself doesn't rotate in relation  
		          // to the screen ;)  
		    	  mSurfaceHolder01.setFixedSize(previewSize.height, previewSize.width);  
		      } else {  
		    	  mSurfaceHolder01.setFixedSize(previewSize.width, previewSize.height);  
		    
		      }
		      
		      //mCamera01.setPreviewCallback(previewCallback);
		      
		      //String thing = mCamera01.getParameters().flatten(); 
		      //System.out.println(thing);
		      try
		      {
		    	mCamera01.setPreviewDisplay(mSurfaceHolder01);
		        mCamera01.startPreview();
		      }
		      catch(Exception e)
		      {
		        e.printStackTrace();
		      }
		      bIfPreview = true;
		      
		      if(parameters.getFocusMode().compareTo(Camera.Parameters.FOCUS_MODE_AUTO) == 0 ||
		    		  parameters.getFocusMode().compareTo(Camera.Parameters.FOCUS_MODE_MACRO) == 0) {
		    	  Log.d(TAG, "may auto focus");
		    	  mCamera01.autoFocus(mAutoFocusCallback);
		      }
		    }
		  }
	
		 
		 private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		        final double ASPECT_TOLERANCE = 0.1;
		        double targetRatio = (double) w / h;
		        if (sizes == null) return null;

		        Size optimalSize = null;
		        double minDiff = Double.MAX_VALUE;

		        int targetHeight = h;

		        // Try to find an size match aspect ratio and size
		        for (Size size : sizes) {
		            double ratio = (double) size.width / size.height;
		            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
		            if (Math.abs(size.height - targetHeight) < minDiff) {
		                optimalSize = size;
		                minDiff = Math.abs(size.height - targetHeight);
		            }
		        }

		        // Cannot find the one match the aspect ratio, ignore the requirement
		        if (optimalSize == null) {
		            minDiff = Double.MAX_VALUE;
		            for (Size size : sizes) {
		                if (Math.abs(size.height - targetHeight) < minDiff) {
		                    optimalSize = size;
		                    minDiff = Math.abs(size.height - targetHeight);
		                }
		            }
		        }
		        return optimalSize;
		    }
		 
		 private int getCameraDisplayOrientation(Activity activity) {
             int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
             int degrees = 0;
             switch (rotation) {
             case Surface.ROTATION_0:
                     degrees = 0;
                     break;
             case Surface.ROTATION_90:
                     degrees = 90;
                     break;
             case Surface.ROTATION_180:
                     degrees = 180;
                     break;
             case Surface.ROTATION_270:
                     degrees = 270;
                     break;
             } 
             int result;
             Camera.CameraInfo info = new Camera.CameraInfo();
             Camera.getCameraInfo(0, info);//0:cameraId.
             
             if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {  
                 result = (info.orientation + degrees) % 360;  
                 result = (360 - result) % 360;  
             } else {  
                 result = (info.orientation - degrees + 360) % 360;  
             }

             return result;
     }
		  
		  private void takePicture() 
		  {
		    if (mCamera01 != null && bIfPreview) 
		    {
		      mCamera01.takePicture(shutterCallback, rawCallback, jpegCallback);
		    }
		  }
		  
		  private void resetCamera()
		  {
		    if (mCamera01 != null && bIfPreview)
		    {
		      try
		      {
		        mCamera01.stopPreview();
		        mCamera01.setPreviewCallback(null);
		        mCamera01.release();
		        mCamera01 = null;
		        bIfPreview = false;
		      }
		      catch(Exception e)
		      {
		        e.printStackTrace();
		      }
		    }
		  }
		   
		  private ShutterCallback shutterCallback = new ShutterCallback() 
		  { 
		    public void onShutter() 
		    { 
		      // Shutter has closed 
		    } 
		  }; 
		   
		  private PictureCallback rawCallback = new PictureCallback() 
		  { 
		    public void onPictureTaken(byte[] _data, Camera _camera) 
		    { 
		      // TODO Handle RAW image data 
		    } 
		  }; 

		  private PictureCallback jpegCallback = new PictureCallback() 
		  {
		    public void onPictureTaken(byte[] _data, Camera _camera)
		    {
		      // TODO Handle JPEG image data
		      
		      try
		      {
		        /*
		        import java.io.File;
		        
		        String strQRTestFile = "/sdcard/test_qrcode.jpg"; 
		        File myImageFile = new File(strQRTestFile);
		        
		        if(myImageFile.exists())
		        {
		          Bitmap myBmp = BitmapFactory.decodeFile(strQRTestFile); 
		          mImageView01.setImageBitmap(myBmp);
		          String strQR2 = decodeQRImage(myBmp);
		          if(strQR2!="")
		          {
		            if (URLUtil.isNetworkUrl(strQR2))
		            {
		              makeTextToast(strQR2, true);
		              Uri mUri = Uri.parse(strQR2);
		              Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
		              startActivity(intent);
		            }
		            else
		            {
		              makeTextToast(strQR2, true);
		            }
		          }
		        }
		        */
		        
		        Bitmap bm = null;
		        bm = BitmapFactory.decodeByteArray(_data, 0, _data.length);
		              
		        doSomethingNeeded(bm, true, false);
		        
//		        int resizeWidth = 320;
//		        int resizeHeight = 240;
//		        float scaleWidth = ((float) resizeWidth) / bm.getWidth();
//		        float scaleHeight = ((float) resizeHeight) / bm.getHeight();
//		        
//		        Matrix matrix = new Matrix();
//		        matrix.postScale(scaleWidth, scaleHeight);
//		        
//		        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
//		        
//		        Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 10, 10, 200, 200);
//		        //Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 30, 10, 100, 100);
//		        //Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 21, 1, 118, 118);
//		        //Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 60, 20, 200, 200);
//		        
//		        mImageView01.setImageBitmap(resizedBitmapSquare);
//		        
//		        
//		        
//		        
//		        String strQR2 = decodeQRImage(resizedBitmapSquare);
//		        if(strQR2!="")
//		        {
//		        	freshTextInfo(strQR2);
//		          if (URLUtil.isNetworkUrl(strQR2))
//		          {
//		            makeTextToast(strQR2, true);
//		            Uri mUri = Uri.parse(strQR2);
//		            Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
//		            startActivity(intent);
//		          }
//		          else if(eregi("wtai://",strQR2))
//		          {
//		            String[] aryTemp01 = strQR2.split("wtai://");
//		            Intent myIntentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+aryTemp01[1]));
//		            startActivity(myIntentDial); 
//		          }
//		          else if(eregi("TEL:",strQR2))
//		          {
//		            String[] aryTemp01 = strQR2.split("TEL:");
//		            Intent myIntentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+aryTemp01[1]));
//		            startActivity(myIntentDial);
//		          }
//		          else
//		          {
//		            makeTextToast(strQR2, true);
//		          }
//		        }

		        resetCamera();
//		        //�����˼�����
		        initCamera();
		      }
		      catch (Exception e)
		      {
		        Log.e(TAG, e.getMessage());
		      }
		    }
		  };
		  
		  private  void freshTextInfo(String info) {
			  mInfo = info;
		  }
		  
		  public static boolean eregi(String strPat, String strUnknow)
		  {
		    String strPattern = "(?i)"+strPat;
		    Pattern p = Pattern.compile(strPattern);
		    Matcher m = p.matcher(strUnknow);
		    return m.find();
		  }
		  
		  public String eregi_replace(String strFrom, String strTo, String strTarget)
		  {
		    String strPattern = "(?i)"+strFrom;
		    Pattern p = Pattern.compile(strPattern);
		    Matcher m = p.matcher(strTarget);
		    if(m.find())
		    {
		      return strTarget.replaceAll(strFrom, strTo);
		    }
		    else
		    {
		      return strTarget;
		    }
		  }
		  
		  public void makeTextToast(String str, boolean isLong)
		  {
//		    if(isLong==true)
//		    {
//		      Toast.makeText(QRDecoderActivity.this, str, Toast.LENGTH_LONG).show();
//		    }
//		    else
//		    {
//		      Toast.makeText(QRDecoderActivity.this, str, Toast.LENGTH_SHORT).show();
//		    }
		  }
		  
		  static final Vector<BarcodeFormat> PRODUCT_FORMATS;
		  static final Vector<BarcodeFormat> ONE_D_FORMATS;
		  static final Vector<BarcodeFormat> QR_CODE_FORMATS;
		  static final Vector<BarcodeFormat> DATA_MATRIX_FORMATS;
		  static {
		    PRODUCT_FORMATS = new Vector<BarcodeFormat>(5);
		    PRODUCT_FORMATS.add(BarcodeFormat.UPC_A);
		    PRODUCT_FORMATS.add(BarcodeFormat.UPC_E);
		    PRODUCT_FORMATS.add(BarcodeFormat.EAN_13);
		    PRODUCT_FORMATS.add(BarcodeFormat.EAN_8);
		    PRODUCT_FORMATS.add(BarcodeFormat.RSS14);
		    ONE_D_FORMATS = new Vector<BarcodeFormat>(PRODUCT_FORMATS.size() + 4);
		    ONE_D_FORMATS.addAll(PRODUCT_FORMATS);
		    ONE_D_FORMATS.add(BarcodeFormat.CODE_39);
		    ONE_D_FORMATS.add(BarcodeFormat.CODE_93);
		    ONE_D_FORMATS.add(BarcodeFormat.CODE_128);
		    ONE_D_FORMATS.add(BarcodeFormat.ITF);
		    QR_CODE_FORMATS = new Vector<BarcodeFormat>(1);
		    QR_CODE_FORMATS.add(BarcodeFormat.QR_CODE);
		    DATA_MATRIX_FORMATS = new Vector<BarcodeFormat>(1);
		    DATA_MATRIX_FORMATS.add(BarcodeFormat.DATA_MATRIX);
		  }
		  
		  private MultiFormatReader initFormatReader(String characterSet, ResultPointCallback resultPointCallback) {
			  MultiFormatReader multiFormatReader = new MultiFormatReader();
			  Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>(3);
			  Vector<BarcodeFormat> decodeFormats =new Vector<BarcodeFormat>();
			  decodeFormats.addAll(ONE_D_FORMATS);
			  decodeFormats.addAll(QR_CODE_FORMATS);
			  decodeFormats.addAll(DATA_MATRIX_FORMATS);
			  hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
			  if (characterSet != null) {
				  hints.put(DecodeHintType.CHARACTER_SET, characterSet);
			   }
			  //hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultPointCallback);
			  multiFormatReader.setHints(hints);
			  return multiFormatReader;
		  }
		  
		  public String decodeQRImage(Bitmap myBmp)
		  {

			  
		    String strDecodedData = "";
		    try
		    {
		      QRCodeDecoder decoder = new QRCodeDecoder();
		      strDecodedData  = new String(decoder.decode(new AndroidQRCodeImage(myBmp)), "utf-8");
		    }
		    catch(Exception e)
		    {
		    	Log.e(TAG, e.toString());
		      e.printStackTrace();
		    }
		    return strDecodedData; 
		  }

		@Override
		protected void onPause() {
			resetCamera();
			super.onPause();
		}
		
		
		private class FaceTask extends AsyncTask<Void, Void, Void> {
			private byte[] mData;
			
			FaceTask(byte[] data) {
				this.mData = data;
			}
			
			private void decodeImg() {
				Bitmap bmp;
				try {
					Log.d(TAG, "doInBackground begin");
					Size size = mCamera01.getParameters().getPreviewSize(); //��ȡԤ����С
					final int w = size.width;  //���
					final int h = size.height;
					//preview的YUV图像转bitmap的第一种方法
					final YuvImage image = new YuvImage(mData, ImageFormat.NV21, w, h, null);
					ByteArrayOutputStream os = new ByteArrayOutputStream(mData.length);
					if(!image.compressToJpeg(new Rect(0, 0, w, h), 100, os)){
					    return ;
					}
					byte[] tmp = os.toByteArray();
					Log.d(TAG, "BitmapFactory.decodeByteArray");
					bmp = BitmapFactory.decodeByteArray(tmp, 0,tmp.length);
					os.close();
					//preview的YUV图像转bitmap的第二种方法
					//bmp = rawByteArray2RGBABitmap2(mData, w,h);
					
					doSomethingNeeded(bmp, true, true);   //�Լ������ʵʱ����Ԥ��֡��Ƶ���㷨
					Log.d(TAG, "doInBackground end");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e(TAG, e.toString());
				} 
	            
				return ;
			
			}

			@Override
			protected Void doInBackground(Void... params) {
				Result rawResult = null;
				MultiFormatReader multiFormatReader = initFormatReader(null, null);
				Size size1 = mCamera01.getParameters().getPreviewSize(); 
				final int w1 = size1.width;  
				final int h1 = size1.height;

				PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(mData, w1, h1, 0, 0,
			            w1, h1);
				try {
				    	
				    	BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				        rawResult = multiFormatReader.decodeWithState(bitmap);
				      } catch (ReaderException re) {
				        // continue
				      } finally {
				        multiFormatReader.reset();
				      }
				if(rawResult != null) {
					mInfo = rawResult.getText();
					
					handler.post(runnableUi); 
				}
				
				//decodeImg();
				
				return null;
		}
}
			
		
		/**
	    * 将彩色图转换为灰度图
	    * @param img 位图
	    * @return 返回转换好的位图
	    */
	    public Bitmap convertGreyImg(Bitmap img) {
	     int width = img.getWidth();   //获取位图的宽
	     int height = img.getHeight();  //获取位图的高
	     
	     int []pixels = new int[width * height]; //通过位图的大小创建像素点数组
	     
	     img.getPixels(pixels, 0, width, 0, 0, width, height);
	     int alpha = 0xFF << 24; 
	     for(int i = 0; i < height; i++) {
	      for(int j = 0; j < width; j++) {
	       int grey = pixels[width * i + j];
	       
	       int red = ((grey  & 0x00FF0000 ) >> 16);
	       int green = ((grey & 0x0000FF00) >> 8);
	       int blue = (grey & 0x000000FF);
	       
	       grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
	       grey = alpha | (grey << 16) | (grey << 8) | grey;
	       pixels[width * i + j] = grey;
	      }
	     }
	     Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);
	     result.setPixels(pixels, 0, width, 0, 0, width, height);
	     
	     img.recycle();
	     
	     return result;
	    }
	
		
		private synchronized void doSomethingNeeded(Bitmap bm, boolean needShow, boolean isPreview){
              Log.d(TAG, "convert grey img begin");
			  bm = convertGreyImg(bm);
			  Log.d(TAG, "convert grey img end");
			   ///if(QRDecoderActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			   if(mIsPortrait) {
	            	Bitmap bitmapRotate;  
	                Matrix matrix = new Matrix();  
	                matrix.reset();  
	                matrix.postRotate(90);  
	                bitmapRotate = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),  
	                		bm.getHeight(), matrix, true); 
	                bm.recycle();
	                bm = bitmapRotate;
	            }
		        try {

					
//					int realSizeWidth = mIsPortrait?mOptimalPreviewSize.height:mOptimalPreviewSize.width;
//					int realSizeHeight = mIsPortrait?mOptimalPreviewSize.width:mOptimalPreviewSize.height;

					
					float scale = ((float) 1)/4;
//					float scaleWidth = scale * bm.getWidth();
//					float scaleHeight = scale * bm.getHeight();
					
					Matrix matrix = new Matrix();
					matrix.postScale(scale, scale);
					Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0,
							0, bm.getWidth(), bm.getHeight(), matrix,
							true);

					int resizedBitmapWidth = resizedBitmap.getWidth();
					int resizedBitmapHeight = resizedBitmap.getHeight();
					int squareBitmapSideLength = (int) ((resizedBitmapWidth>resizedBitmapHeight)?resizedBitmapHeight:resizedBitmapWidth);
					//Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, (resizeWidth-resizedBitmapSideLength)/2, (resizeHeight-resizedBitmapSideLength)/2, resizedBitmapSideLength, resizedBitmapSideLength);
					Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 
							(int)(resizedBitmapWidth-squareBitmapSideLength)/2, 
							(int)(resizedBitmapHeight-squareBitmapSideLength)/2, 
							squareBitmapSideLength, 
							squareBitmapSideLength);
					//Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 30, 10, 100, 100);
					//Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 21, 1, 118, 118);
					//Bitmap resizedBitmapSquare = Bitmap.createBitmap(resizedBitmap, 60, 20, 200, 200);
					Log.d(TAG, "begin decodeQRImage");
					String strQR2 = decodeQRImage(resizedBitmapSquare);
					Log.d(TAG, "end decodeQRImage:" + strQR2);
					
					Bitmap showingImg, recycleImg;
					if(isPreview) {
						showingImg = resizedBitmapSquare;
						recycleImg = bm;
					}
					else {
						showingImg = bm;
						recycleImg = resizedBitmapSquare;
					}
					
					recycleImg.recycle();
					if (strQR2 != "") {
						String info = DECODEFLAG + strQR2 ;
						freshTextInfo(info);
						
						needShow = true;
//						mImg = showingImg;
//						handler.post(runnableUi); 
						
						resetCamera();
						
				          if (URLUtil.isNetworkUrl(strQR2))
				          {
//				            makeTextToast(strQR2, true);
//				            Uri mUri = Uri.parse(strQR2);
//				            Intent intent = new Intent(Intent.ACTION_VIEW, mUri);
//				            startActivity(intent);
				          }
				          else if(eregi("wtai://",strQR2))
				          {
				            String[] aryTemp01 = strQR2.split("wtai://");
				            Intent myIntentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+aryTemp01[1]));
				            startActivity(myIntentDial); 
				          }
				          else if(eregi("TEL:",strQR2))
				          {
				            String[] aryTemp01 = strQR2.split("TEL:");
				            Intent myIntentDial = new Intent("android.intent.action.CALL",Uri.parse("tel:"+aryTemp01[1]));
				            startActivity(myIntentDial);
				          }
				          else
				          {
				            makeTextToast(strQR2, true);
				          }
				         
					}
					if(needShow){
						if(mImg != null){
							mImg.recycle();
						}
						mImg = showingImg;
						handler.post(runnableUi); 
					}
					
					
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG,e.toString());
				}
		}
		
		public static Bitmap rawByteArray2RGBABitmap2(byte[] data, int width, int height) {
	        int frameSize = width * height;
	        int[] rgba = new int[frameSize];

	            for (int i = 0; i < height; i++)
	                for (int j = 0; j < width; j++) {
	                    int y = (0xff & ((int) data[i * width + j]));
	                    int u = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 0]));
	                    int v = (0xff & ((int) data[frameSize + (i >> 1) * width + (j & ~1) + 1]));
	                    y = y < 16 ? 16 : y;

	                    int r = Math.round(1.164f * (y - 16) + 1.596f * (v - 128));
	                    int g = Math.round(1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
	                    int b = Math.round(1.164f * (y - 16) + 2.018f * (u - 128));

	                    r = r < 0 ? 0 : (r > 255 ? 255 : r);
	                    g = g < 0 ? 0 : (g > 255 ? 255 : g);
	                    b = b < 0 ? 0 : (b > 255 ? 255 : b);

	                    rgba[i * width + j] = 0xff000000 + (b << 16) + (g << 8) + r;
	                }

	        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	        bmp.setPixels(rgba, 0 , width, 0, 0, width, height);
	        return bmp;
	    }
		
		
		static public void decodeYUV420SP(byte[] rgbBuf, byte[] yuv420sp, int width, int height) {
	    	final int frameSize = width * height;
			if (rgbBuf == null)
				throw new NullPointerException("buffer 'rgbBuf' is null");
			if (rgbBuf.length < frameSize * 3)
				throw new IllegalArgumentException("buffer 'rgbBuf' size "
						+ rgbBuf.length + " < minimum " + frameSize * 3);

			if (yuv420sp == null)
				throw new NullPointerException("buffer 'yuv420sp' is null");

			if (yuv420sp.length < frameSize * 3 / 2)
				throw new IllegalArgumentException("buffer 'yuv420sp' size " + yuv420sp.length
						+ " < minimum " + frameSize * 3 / 2);

	    	int i = 0, y = 0;
	    	int uvp = 0, u = 0, v = 0;
	    	int y1192 = 0, r = 0, g = 0, b = 0;

	    	for (int j = 0, yp = 0; j < height; j++) {
	    		uvp = frameSize + (j >> 1) * width;
	    		u = 0;
	    		v = 0;
	    		for (i = 0; i < width; i++, yp++) {
	    			y = (0xff & ((int) yuv420sp[yp])) - 16;
	    			if (y < 0) y = 0;
	    			if ((i & 1) == 0) {
	    				v = (0xff & yuv420sp[uvp++]) - 128;
	    				u = (0xff & yuv420sp[uvp++]) - 128;
	    			}

	    			y1192 = 1192 * y;
	    			r = (y1192 + 1634 * v);
	    			g = (y1192 - 833 * v - 400 * u);
	    			b = (y1192 + 2066 * u);

	    			if (r < 0) r = 0; else if (r > 262143) r = 262143;
	    			if (g < 0) g = 0; else if (g > 262143) g = 262143;
	    			if (b < 0) b = 0; else if (b > 262143) b = 262143;

	    			rgbBuf[yp * 3] = (byte)(r >> 10);
	    			rgbBuf[yp * 3 + 1] = (byte)(g >> 10);
	    			rgbBuf[yp * 3 + 2] = (byte)(b >> 10);
	    		}
	    	}
	    }
		

}
