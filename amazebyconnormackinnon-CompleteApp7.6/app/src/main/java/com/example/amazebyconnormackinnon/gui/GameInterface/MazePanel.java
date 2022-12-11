
package com.example.amazebyconnormackinnon.gui.GameInterface;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;


public class MazePanel extends View implements P7PanelF22{
    private Paint paint = new Paint();
    private Bitmap bitmap;
    private Canvas canvas;

    //default constructors
    public MazePanel(Context context) {
        super(context);
        init(null);
    }
    public MazePanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }
    public MazePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }
    public MazePanel(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attr){
        bitmap = Bitmap.createBitmap( 1000,1000, Bitmap.Config.ARGB_8888);
        canvas = new Canvas();
        canvas.setBitmap(bitmap);
    }

    public Canvas getCanvas(){
        return(canvas);
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawBitmap(bitmap,0,0,null);
        //myTestImage(canvas);
    }

    private void myTestImage(Canvas c){
        addBackground(100);
        int xpoint[] = {0,100,150,100,0};
        int ypoint[] = {0,0,50,100,100};
        setColor(Color.BLUE);
        addFilledPolygon(xpoint,ypoint,5);
        for (int i = 0; i<2000; i+=5) {
            addLine(0, 0, 500, 500-i);
        }
        setColor(Color.BLACK);
        addFilledOval(100,100,400,300);
        setColor(Color.MAGENTA);
        addArc(0,0,1000,800,40,45);
        addMarker(500,400,"TESTING");
        commit();

    }


    @Override
    public void commit() {
        invalidate();
    }

    @Override
    public boolean isOperational() {
        //TODO judge when the MazePanel is operational
        return false;
    }

    @Override
    public void setColor(int argb) {
        paint.setColor(argb);
    }

    @SuppressLint("NewApi")
    public void setColor(Color color) {
        paint.setColor(color.toArgb());
    } //to deal with color vs argb complications

    @Override
    public int getColor() {
        return paint.getColor();
    }

    @Override @SuppressLint("NewApi")
    public void addBackground(float percentToExit) {
        int sky = ColorUtils.blendARGB(Constants.skyYellow.toArgb(), Constants.skyGold.toArgb(), percentToExit/100);
        int floor = ColorUtils.blendARGB(Constants.darkGreen.toArgb(), Constants.midGreen.toArgb(), percentToExit/100);
        setColor(sky);
        addFilledRectangle(0, 0, canvas.getWidth(),canvas.getHeight()/2);
        setColor(floor);
        addFilledRectangle(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);
    }


    @Override
    public void addFilledRectangle(int x, int y, int width, int height) {
        Path path = new Path();
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(x,y);
        path.lineTo(x+width, y);
        path.lineTo(x+width, y+height);
        path.lineTo(x, y+height);
        path.lineTo(x,y);
        canvas.drawPath(path,paint);
    }

    @Override
    public void addFilledPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path path = new Path();
        paint.setStyle(Paint.Style.FILL);
        path.moveTo(xPoints[0],yPoints[0]);
        for (int i = 1; i<nPoints; i++){
            path.lineTo(xPoints[i], yPoints[i]);
        }
        //Connect back to the beginning
        path.lineTo(xPoints[0],yPoints[0]);
        canvas.drawPath(path,paint);
    }

    @Override
    public void addPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Path path = new Path();
        paint.setStyle(Paint.Style.STROKE);
        path.moveTo(xPoints[0],yPoints[0]);
        for (int i = 1; i<nPoints; i++){
            path.lineTo(xPoints[i], yPoints[i]);
        }
        //Connect back to the beginning
        path.lineTo(xPoints[0],yPoints[0]);
        canvas.drawPath(path,paint);
    }

    @Override
    public void addLine(int startX, int startY, int endX, int endY) {
        Path path = new Path();
        paint.setStyle(Paint.Style.STROKE);
        path.moveTo(startX,startY);
        path.lineTo(endX, endY);
        canvas.drawPath(path,paint);
    }

    @Override
    public void addFilledOval(int x, int y, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        canvas.drawOval(x,y,width+x,height+y, paint);
    }

    @Override
    public void addArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(x,y,width,height,startAngle,arcAngle,false, paint);
    }

    @Override
    public void addMarker(float x, float y, String str) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(75);
        canvas.drawText(str,x-25,y+20,paint);
    }

    @Override
    public void setRenderingHint(P7RenderingHints hintKey, P7RenderingHints hintValue) {

    }
}
