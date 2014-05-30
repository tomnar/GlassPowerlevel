package com.creativevikings.powerlevel;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import com.google.android.glass.timeline.DirectRenderingCallback;

public class LiveCardRenderer implements DirectRenderingCallback{
	
	private static final long FRAME_TIME_MILLIS = 33; //Rendering timer, ~30FPS
	
	private SurfaceHolder mHolder; //holding the surface
	private Boolean mPaused; //is paused?
	private RenderThread mRenderThread; //rendering the view in a thread

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub
		Log.d("lol","surfaceChanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d("lol","surfaceCreated");
		mPaused = false;
		mHolder = holder;
		updateRendering();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d("lol","surfaceDestroyed");
		mHolder = null;
        updateRendering();		
	}

	@Override
	public void renderingPaused(SurfaceHolder holder, boolean paused) {
		Log.d("lol","renderingPaused");
		mPaused = paused;
        updateRendering();		
	}
	
	// Start or stop rendering according to the timeline state.
    private synchronized void updateRendering() {
    	
    	//Check if it should render, if yes start a new thread
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;
        
        Log.d("lol","updateRendering, with: shouldRender = "  + shouldRender + ", rendering = " + rendering);

        if (shouldRender != rendering) {
            if (shouldRender) {
                mRenderThread = new RenderThread();
                mRenderThread.start();
            } else {
                mRenderThread.quit();
                mRenderThread = null;
            }
        }
    }    
    
    private void drawSomething(Canvas canvas){
    	Paint paint = new Paint(); 
        paint.setColor(Color.WHITE); 
        paint.setStyle(Style.FILL); 
        canvas.drawPaint(paint); 

        paint.setColor(Color.BLACK); 
        paint.setTextSize(20); 
        canvas.drawText("Some Text", 10, 25, paint); 
        
        // paint a rectangular shape that fill the surface.
        int border = 100;
        int width = 150;//canvas.getWidth()-20; not width
        int height = 150;//canvas.getHeight()-20; not height
        RectF r = new RectF(border, border, width, height);
        paint.setColor(Color.RED); 
        canvas.drawRect(r , paint );
    }

    // Draws the view in the SurfaceHolder's canvas. (called from thread)
    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
            
            
                        
        } catch (Exception e) {
        	Log.d("error", e.toString());
            return;
        }
        if (canvas != null) {
        	
        	drawSomething(canvas);
            
            // Draw on the canvas.
            mHolder.unlockCanvasAndPost(canvas);
        }
    }
    
    // Private anonymous class for rendering in the background
    private class RenderThread extends Thread {
        private boolean mShouldRun;

        // Initializes the background rendering thread.
        public RenderThread() {
            mShouldRun = true;
        }

        // Returns true if the rendering thread should continue to run.
        private synchronized boolean shouldRun() {
            return mShouldRun;
        }

        // Requests that the rendering thread exit at the next opportunity.
        public synchronized void quit() {
            mShouldRun = false;
        }

        @Override
        public void run() {
            while (shouldRun()) {
                draw();
                SystemClock.sleep(FRAME_TIME_MILLIS);
            }
        }
    }
}
