package com.creativevikings.powerlevel;

import android.graphics.Canvas;
import android.os.SystemClock;
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
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		 mHolder = holder;
	     updateRendering();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
        updateRendering();		
	}

	@Override
	public void renderingPaused(SurfaceHolder holder, boolean paused) {
		mPaused = paused;
        updateRendering();		
	}
	
	// Start or stop rendering according to the timeline state.
    private synchronized void updateRendering() {
    	
    	//Check if it should render, if yes start a new thread
        boolean shouldRender = (mHolder != null) && !mPaused;
        boolean rendering = mRenderThread != null;

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
    

    // Draws the view in the SurfaceHolder's canvas. (called from thread)
    private void draw() {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
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
