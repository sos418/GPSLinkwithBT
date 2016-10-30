package com.example.gpslinkwithbluetooth;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.view.View;

public class DrawView extends View {  
	  int width,height;
		ArrayList<SatData> realdata=new ArrayList<SatData>();
    public DrawView(Context context,int input1,int input2,ArrayList<SatData> inputdata) {  
        super(context);
        width=input1;
        height=input2;
        realdata=inputdata;
    }  
  
    protected void onDraw(Canvas canvas) {  
        super.onDraw(canvas);  
         
        // 创建画笔  
        Paint p = new Paint();  
        p.setColor(Color.rgb(256, 256, 187)); 
  
     
      
        p.setAntiAlias(true);
        int radius;
        if(height>width)
            radius=width;
        else
            radius=height;
        radius=radius/2-20;
        canvas.drawCircle(width/2, height/2,radius, p);
  
        p.setColor(Color.GREEN);
        for(int i=0;i<realdata.size();i++)
        {
        	p.setColor(Color.rgb(153, 256, 153)); 
        	double r=((radius)/90)*(90-realdata.get(i).elevation);
        	float x=(float) (r*Math.cos((realdata.get(i).azimuth-90)*Math.PI/180));
        	float y=(float) (r*Math.sin((realdata.get(i).azimuth-90)*Math.PI/180));
        	canvas.drawCircle((width/2)+x, (height/2)+y, 40, p);
        	p.setColor(Color.BLACK);
        	p.setTextSize(30);
        	canvas.drawText(realdata.get(i).getnumber()+"", (width/2)+x, (height/2)+y, p);
        }
        
        
        
        
        
        //画图片，就是贴图  
       // Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);  
        //canvas.drawBitmap(bitmap, 250,360, p);  
    }  

}  
