package com.android.cjcc.qrcoder;

import com.swetake.util.Qrcode;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class QRCoderActivity extends Activity implements SurfaceHolder.Callback {

	private Button mButton01;
	private TextView mTextView01;
	private EditText mEditText01;
	private String TAG = "HIPPO";
	private SurfaceView mSurfaceView01;
	private SurfaceHolder mSurfaceHolder01;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_qrcoder);
		
	    DisplayMetrics dm = new DisplayMetrics();
	    getWindowManager().getDefaultDisplay().getMetrics(dm);
	    
	    mTextView01 = (TextView) findViewById(R.id.myTextView1);
	    //mTextView01.setText(R.string.str_qr_gen);
	    
	    mSurfaceView01 = (SurfaceView) findViewById(R.id.mSurfaceView1);
	    
	    mSurfaceHolder01 = mSurfaceView01.getHolder();
	    
	    mSurfaceHolder01.addCallback(QRCoderActivity.this);
	    
	    mButton01 = (Button)findViewById(R.id.myButton0);
	    mButton01.setOnClickListener(new OnClickListener()
	    {
	      @Override
	      public void onClick(View arg0)
	      {
	        // TODO Auto-generated method stub
	        if(mEditText01.getText().toString()!="")
	        {
	          AndroidQREncode(mEditText01.getText().toString(), 4);
	        }
	      }
	    });
	    
	    mEditText01 = (EditText)findViewById(R.id.myEditText1);
	    mEditText01.setText("������Ϣ");
	    mEditText01.setOnKeyListener(new OnKeyListener()
	    {
	      @Override
	      public boolean onKey(View v, int keyCode, KeyEvent event)
	      {
	        // TODO Auto-generated method stub
	        return false;
	      }
	    });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_qrcoder, menu);
		return true;
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
		
	}
	
	public void AndroidQREncode(String strEncoding, int qrcodeVersion)
	  {
	    try
	    {
	      Qrcode testQrcode = new Qrcode();
	      testQrcode.setQrcodeErrorCorrect('M');
	      testQrcode.setQrcodeEncodeMode('B');
	      /* 0-20 */
	      testQrcode.setQrcodeVersion(qrcodeVersion);
	      
	      // getBytes
	      byte[] bytesEncoding = strEncoding.getBytes("utf-8");
	      
	      if (bytesEncoding.length>0 && bytesEncoding.length <120)
	      {
	        boolean[][] bEncoding = testQrcode.calQrcode(bytesEncoding);
	        drawQRCode(bEncoding, getResources().getColor(R.drawable.black));
	      }
	    }
	    catch (Exception e)
	    {
	      Log.i(TAG, Integer.toString(mEditText01.getText().length()) );
	      e.printStackTrace();
	    }
	  }
	private void drawQRCode(boolean[][] bRect, int colorFill)
	  {
	    /* test Canvas*/
	    int intPadding = 20;
	    
	    Canvas mCanvas01 = mSurfaceHolder01.lockCanvas();
	    
	    mCanvas01.drawColor(getResources().getColor(R.drawable.white));
	    
	    Paint mPaint01 = new Paint();
	    
	    mPaint01.setStyle(Paint.Style.FILL);
	    mPaint01.setColor(colorFill);
	    mPaint01.setStrokeWidth(1.0F);
	    
	    for (int i=0;i<bRect.length;i++)
	    {
	      for (int j=0;j<bRect.length;j++)
	      {
	        if (bRect[j][i])
	        {
	          mCanvas01.drawRect(new Rect(intPadding+j*3+2, intPadding+i*3+2, intPadding+j*3+2+3, intPadding+i*3+2+3), mPaint01);
	        }
	      }
	    }
	    mSurfaceHolder01.unlockCanvasAndPost(mCanvas01);
	  }

	
//		    /** 
//		     * 生成二维码(QRCode)图片的公共方法 
//		     * @param content 存储内容 
//		     * @param imgType 图片类型 
//		     * @param size 二维码尺寸 
//		     * @return 
//		     */  
//		    private BufferedImage qRCodeCommon(String content, String imgType, int size) {  
//		        BufferedImage bufImg = null;  
//		        try {  
//		            Qrcode qrcodeHandler = new Qrcode();  
//		            // 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小  
//		            qrcodeHandler.setQrcodeErrorCorrect('M');  
//		            qrcodeHandler.setQrcodeEncodeMode('B');  
//		            // 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大  
//		            qrcodeHandler.setQrcodeVersion(size);  
//		            // 获得内容的字节数组，设置编码格式  
//		            byte[] contentBytes = content.getBytes("utf-8");  
//		            // 图片尺寸  
//		            int imgSize = 67 + 12 * (size - 1);  
//		            bufImg = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);  
//		            Graphics2D gs = bufImg.createGraphics();  
//		            // 设置背景颜色  
//		            gs.setBackground(Color.WHITE);  
//		            gs.clearRect(0, 0, imgSize, imgSize);  
//		  
//		            // 设定图像颜色> BLACK  
//		            gs.setColor(Color.BLACK);  
//		            // 设置偏移量，不设置可能导致解析出错  
//		            int pixoff = 2;  
//		            // 输出内容> 二维码  
//		            if (contentBytes.length > 0 && contentBytes.length < 800) {  
//		                boolean[][] codeOut = qrcodeHandler.calQrcode(contentBytes);  
//		                for (int i = 0; i < codeOut.length; i++) {  
//		                    for (int j = 0; j < codeOut.length; j++) {  
//		                        if (codeOut[j][i]) {  
//		                            gs.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);  
//		                        }  
//		                    }  
//		                }  
//		            } else {  
//		                throw new Exception("QRCode content bytes length = " + contentBytes.length + " not in [0, 800].");  
//		            }  
//		            gs.dispose();  
//		            bufImg.flush();  
//		        } catch (Exception e) {  
//		            e.printStackTrace();  
//		        }  
//		        return bufImg;  
//		    }  

}
