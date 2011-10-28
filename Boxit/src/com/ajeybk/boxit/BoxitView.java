package com.ajeybk.boxit;

import java.util.Random;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/* BoxitView: a View-variant designed for handling arrays of "icons" or other drawables. */
public class BoxitView extends View {
	private static final String TAG = "TileView";
	public static final byte NUMROWS = 5;
	public static final byte NUMCOLS = 5;
	public static final int OFFSET = 15;
	public static final int INVALIDTOKEN = -1;
	public static final int INVALIDOFFSET = -1;
	
	private static final String RUNGAME = "runGame"; 
	private static final String PAUSEGAME = "pauseGame";
	private static final String TOKENCOUNT = "Count";
	private static final String SECONDS = "Seconds";
	private static final String MINUTES = "Minutes";
	private static final String UNDOSTRING = "UndoString";
	public static final String CURRENTTILES="currenttiles";
	private static final String VIEW_STATE = "viewState";

	private int mXOffset = OFFSET;
	private int mYOffset = OFFSET;
	private byte mXTileCount = NUMROWS;
    private byte mYTileCount = NUMCOLS;
    private Bitmap[][] mTileMap = new Bitmap[mXTileCount][mYTileCount]; 
    private Rect[][] mTileRect = new Rect[mXTileCount][mYTileCount];
    private int mTileSize;    
    private int mMainW;
    private int mMainH;
    private Context ctxt;
    private Bitmap backImg;
    private Bitmap noDropImg;
    private final Paint mPaint = new Paint();
    private final Paint mHilite = new Paint();
    boolean moveValid = false;
    private int heldRect = INVALIDTOKEN;
    private int overRect = INVALIDTOKEN;
    private int movingRect = INVALIDTOKEN;
    private int movingCx = INVALIDOFFSET;
    private int movingCy = INVALIDOFFSET;
    private Rect movingR;
    
    private int[][] mTileGrid = new int[mXTileCount][mYTileCount];
    boolean runGame = false;
    boolean runPause = false;
    String strUndo = "";
    int countToken = mXTileCount * mYTileCount;
    byte countTimeM = 0;
    byte countTimeS = 0;
    
    public BoxitView(Context context) {
        super(context);
        ctxt = context;
    }

    public BoxitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ctxt = context;
    }
    
    public BoxitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctxt = context;
    }
    
    @Override
    protected Parcelable onSaveInstanceState() { 
       Parcelable p = super.onSaveInstanceState();
       //Log.d(TAG, "onSaveInstanceState");
       Bundle bundle = new Bundle();
       bundle.putBoolean(RUNGAME,runGame);
       bundle.putBoolean(PAUSEGAME,runPause);
       bundle.putInt(TOKENCOUNT, countToken);
       bundle.putByte(SECONDS,countTimeS);
       bundle.putByte(MINUTES, countTimeM);
       bundle.putString(UNDOSTRING,strUndo);
       int arry[] = new int[mXTileCount*mYTileCount];
       int k = 0;
       for (int x = 0; x < mXTileCount; x++) {
           for (int y = 0; y < mYTileCount; y++) {
        	   arry[k] = mTileGrid[x][y];
        	   k++;
           }
       }
       bundle.putIntArray(CURRENTTILES,arry);
       bundle.putParcelable(VIEW_STATE, p);
       return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) { 
	   //Log.d(TAG, "onRestoreInstanceState");
	   Bundle bundle = (Bundle) state;
	   runGame = bundle.getBoolean(RUNGAME, false);
	   runPause = bundle.getBoolean(PAUSEGAME,false);
	   countToken = bundle.getInt(TOKENCOUNT);
	   countTimeS = bundle.getByte(SECONDS);
	   countTimeM = bundle.getByte(MINUTES);
	   strUndo = bundle.getString(UNDOSTRING);
	   int arry[] = bundle.getIntArray(CURRENTTILES);
	   for(int k =0; k<arry.length; k++){
		   int i = k/mYTileCount;
		   int j = k%mYTileCount;
		   mTileGrid[i][j] = arry[k];
	   }
	   super.onRestoreInstanceState(bundle.getParcelable(VIEW_STATE));
    }

    void jumbleUp(boolean toDraw){
    	for (int i = 0; i < mXTileCount; i++) {
            for (int j = 0; j < mYTileCount; j++) {
            	mTileGrid[i][j] = ((i + 1) * 10) + (j + 1);
            	//Log.e(TAG,"mTileGrid: [i,j]:[" + i + ","+ j+"],val:" + mTileGrid[i][j]);
            }
        }
        // Randomize the tokens
        Random rangen = new Random();
        for (int i = 0; i < 50; i++) {
            int r1 = rangen.nextInt(mXTileCount);
            int r2 = rangen.nextInt(mYTileCount);
            int r3 = rangen.nextInt(mXTileCount);
            int r4 = rangen.nextInt(mYTileCount);
            int dummy = mTileGrid[r1][r2];
            mTileGrid[r1][r2] = mTileGrid[r3][r4];
            mTileGrid[r3][r4] = dummy;
        }
        if( toDraw )
        	invalidate();
    }
    
    private void initBoxitView() {
        setFocusable(true);
        Resources r = ctxt.getResources();
        Drawable backTile = r.getDrawable(R.drawable.back2);
        backImg = Bitmap.createBitmap(mMainW, mMainH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(backImg);
        backTile.setBounds(0, 0,mMainW,mMainH);
        backTile.draw(canvas);
        
        Drawable noDropTile = r.getDrawable(R.drawable.nodrop);
        noDropImg = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas noDropCanvas = new Canvas(noDropImg);
        noDropTile.setBounds(0, 0,mTileSize,mTileSize);
        noDropTile.draw(noDropCanvas);
        
        String pkgName = ((Activity)ctxt).getComponentName().getPackageName();
        for (int x = 0; x < mXTileCount; x++) {
            for (int y = 0; y < mYTileCount; y++) {
            	int lx = x + 1;
            	int ly = y + 1;
            	String resName = "token" + lx + ly;
            	int id = r.getIdentifier(resName,"drawable",pkgName);
            	if( id != 0 ){
            		Drawable tile = r.getDrawable(id);
            		Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(bitmap);
                    tile.setBounds(0, 0,mTileSize,mTileSize);
                    tile.draw(canvas);
                    mTileMap[x][y] = bitmap;
                    //Log.e(TAG,"mTileMap: [x,y]:[" + x + ","+ y+"],val:" + resName);
            	}
            }
        }
        
        for (int x = 0; x < mXTileCount; x += 1) {
        	int xoffset = (x+1) * mXOffset + x * mTileSize;
            for (int y = 0; y < mYTileCount; y += 1) {
            	int yoffset = mYOffset + (y * mYOffset) + y * mTileSize;
            	mTileRect[y][x] = new Rect(xoffset,yoffset,xoffset+mTileSize,yoffset+mTileSize);
            	//int val = (x+1)*10 + y + 1;
            	//Log.e(TAG,"Rect:" + val + ", start: "+xoffset+","+yoffset+",w,h:" + mTileSize); 
            }
        }
        mHilite.setColor(getResources().getColor(R.color.puzzle_hilite)); 
    }
    
    public void resetTiles() {
    	mTileMap = new Bitmap[mXTileCount][mYTileCount];
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) { 
        int measuredWidth = measure(widthMeasureSpec);
        int measuredHeight = measure(heightMeasureSpec);
        int d = Math.min(measuredWidth, measuredHeight);
        setMeasuredDimension(d, d);    
    }
          
    private int measure(int measureSpec) {
    	int result = 0; 
    	// Decode the measurement specifications.
    	int specMode = MeasureSpec.getMode(measureSpec);
    	int specSize = MeasureSpec.getSize(measureSpec); 
    	if (specMode == MeasureSpec.UNSPECIFIED) {
    		result = 200;
    	} else if( specMode == MeasureSpec.AT_MOST  || specMode == MeasureSpec.EXACTLY ){
    		result = specSize;
    	}else {
    		// We want the available space
    		result = specSize;
    	} 
    	return result;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	if( w == h){
    		mTileSize = (w - ((mXTileCount+1)*OFFSET))/mYTileCount;  
    	}else{
    		int d = Math.min(w, h);
    		mTileSize = (d - ((mXTileCount+1) * OFFSET))/mYTileCount;
    	}
    	Log.e(TAG, "TileSize is :" + mTileSize);
    	mMainW = w;
    	mMainH = h;
    	initBoxitView();
    }

    private int calcXIndex (int key) {
        return (key / 10) - 1;
    }
    
    private int calcYIndex (int key) {
        return (key % 10) - 1;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(backImg,0,0,mPaint);
        for (int x = 0; x < mXTileCount; x += 1) {
        	int yoffset = mYOffset + (x * mYOffset);
            for (int y = 0; y < mYTileCount; y += 1) {
                if (mTileGrid[x][y] > 0) {
                	int key = mTileGrid[x][y];
                	int i = calcXIndex(key);
                    int j = calcYIndex(key);
                    int xoffset = (y+1) * this.mXOffset;
                    canvas.drawBitmap(mTileMap[i][j],xoffset + y * mTileSize,yoffset + x * mTileSize,	mPaint);
                }
            }
        }
        if( movingRect != INVALIDTOKEN ){
        	if( movingRect > 0 ){
        		int i = calcXIndex(movingRect);
                int j = calcYIndex(movingRect);
                int key = mTileGrid[i][j];
                if( key > 0){
                	int curOverRect = getRect(movingCx,movingCy+mTileSize+mTileSize/4);
	        		if( curOverRect != INVALIDTOKEN ){
	        			if( curOverRect == movingRect ){
	        				i = calcXIndex(key);
                        	j = calcYIndex(key);
                        	canvas.drawBitmap(mTileMap[i][j],movingCx,movingCy,mPaint);
	        			}else{
		        			checkMoveValidity(curOverRect,heldRect);
	                        // this move is valid
	                        if (moveValid) {
	                        	i = calcXIndex(key);
	                        	j = calcYIndex(key);
	                        	canvas.drawBitmap(mTileMap[i][j],movingCx,movingCy,mPaint);
	                        }else{
	                        	canvas.drawBitmap(noDropImg,movingCx,movingCy,mPaint);
	                        }
	                        moveValid = true;
	        			}
	        		}else{
	        			canvas.drawBitmap(noDropImg,movingCx,movingCy,mPaint);
	        		}
                }else{
                	Log.e(TAG,"moving rectangle id:" + movingRect + "with cx:"+movingCx+",cy:"+movingCy+",invalid value in Grid:" + key);
                }
        	}else{
        		canvas.drawBitmap(noDropImg,movingCx,movingCy,mPaint);
        	}
        	movingCx = INVALIDOFFSET;
        	movingCy = INVALIDOFFSET;
        	movingRect = INVALIDTOKEN;
        }
    }
    
    public boolean onTouchEvent(MotionEvent event){ 
        int cx=(int)event.getX(); 
        int cy=(int)event.getY(); 
        switch (event.getAction()){ 
        	case MotionEvent.ACTION_MOVE:
        		//int key = getRect(cx,cy);
        		//Log.e(TAG,"mouse move to "+cx+","+cy+",moved to rect:" + key);
        		if ((runGame) && (!runPause) && (heldRect != INVALIDTOKEN)) {
        			movingCx = cx;
        			movingCy = cy-mTileSize-mTileSize/4;
        			movingRect = heldRect;
        			if( movingR != null)
        				invalidate(movingR);
        			movingR = new Rect(movingCx,movingCy,movingCx+mTileSize,movingCy+mTileSize);
	        		invalidate(movingR);
        		}
                break; 
            case MotionEvent.ACTION_DOWN:
            	// The game should be running and not paused, also no token should be held.
                if ((runGame) && (!runPause) && (heldRect == INVALIDTOKEN)) {
                	heldRect = getRect(cx,cy);
                	if( heldRect != INVALIDTOKEN ){
                		int i = calcXIndex(heldRect);
                        int j = calcYIndex(heldRect);
                        if (mTileGrid[i][j] != 0) {
                        	overRect = INVALIDTOKEN;
                        	moveValid = true;
                        	//Log.e(TAG,"mouse down at "+cx+","+cy+ ",in rect:" + heldRect);
                        }else{
                        	heldRect = INVALIDTOKEN;
                        }
                	}
                }
                break; 
            case MotionEvent.ACTION_UP:
            	if ((runGame) && (!runPause) && (heldRect != INVALIDTOKEN)) {
	        		overRect = getRect(cx,cy);
	        		//Log.e(TAG,"mouse up at "+cx+","+cy+",moved to rect:" + overRect);
	        		if( overRect != INVALIDTOKEN ){
	        			checkMoveValidity(overRect,heldRect);
                        // this move is valid
                        if ((moveValid) && (overRect != heldRect)) {
                        	int ocx = calcXIndex(overRect);
                            int ocy = calcYIndex(overRect);
                            int hcx = calcXIndex(heldRect);
                            int hcy = calcYIndex(heldRect);
                        	strUndo = Integer.toString(heldRect) + Integer.toString(overRect) + Integer.toString(mTileGrid[ocx][ocy]) + strUndo;
                            // Place the held token on the over token.
                        	mTileGrid[ocx][ocy] = mTileGrid[hcx][hcy];
                            // No token should be there in the held token place.
                        	mTileGrid[hcx][hcy] = 0;
                        	// Let's invalidate only held token and the over token rectangles, for better results.
                        	invalidate(mTileRect[hcx][hcy]);
                        	invalidate(mTileRect[ocx][ocy]);
                        	if( movingR != null)
    	        				invalidate(movingR);
                            // count down the number of available tokens
                            countToken--;
                            //Update the score now.
                            ((BoxitActivity)ctxt).showScore();
                        }else{
                        	if( movingR != null)
    	        				invalidate(movingR);
                        }
	        		}else{
	        			if( movingR != null)
	        				invalidate(movingR);
	        		}
	        		movingRect = INVALIDTOKEN;
            	}
            	// Check for any valid moves after this token movement, otherwise signal end-of-the-game.
            	if ((runGame) && (!runPause)) {
                    moveValid = false;
                    for (int i = 0; i < mXTileCount; i++) {
                        for (int j = 0; j < mYTileCount; j++) {
                            if (mTileGrid[i][j] != 0) {
                                for (int k = i; k < mXTileCount; k++) {
                                    for (int l = j; l < mYTileCount; l++) {
                                        if (mTileGrid[k][l] != 0) {
                                            if (!moveValid){
                                                int token1 = ((i + 1) * 10) + (j + 1);
                                                int token2 = ((k + 1) * 10) + (l + 1);
                                                if (token1 != token2) {
                                                	checkMoveValidity(token1, token2);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (!moveValid) {
                        // inform the activity about end-of-game.
                    	((BoxitActivity)ctxt).postGameOver();
                    	runGame = false;
                    }
                }
            	moveValid = false;
            	heldRect = INVALIDTOKEN;
                overRect = INVALIDTOKEN;
                break;
        } 
        return true; 
    } 

    private int getRect(int x, int y){
    	int key = INVALIDTOKEN;
    	for (int i = 0; i < mXTileCount; i += 1) {
            for (int j = 0; j < mYTileCount; j += 1) {
            	if( mTileRect[i][j].contains(x,y) ){
            		key = (i+1)*10 + j + 1;
            		break;
            	}
            }
        }
    	return key;
    }
    
    void performUndo() {
        if ((runGame) && (!runPause) && (strUndo != "")) {
            // getting informations from undo string
            int valoc = Integer.parseInt(strUndo.substring(0, 2));
            int valhc = Integer.parseInt(strUndo.substring(2, 4));
            int valpv = Integer.parseInt(strUndo.substring(4, 6));
            // calculating the move to be undone
            int ocx = calcXIndex(valoc);
            int ocy = calcYIndex(valoc);
            int hcx = calcXIndex(valhc);
            int hcy = calcYIndex(valhc);
            // reset the moved token
            mTileGrid[ocx][ocy] = mTileGrid[hcx][hcy];
            // set the removed token
            mTileGrid[hcx][hcy] = valpv;
            // cut the undo string
            if (strUndo.length() > 6) { 
            	strUndo = strUndo.substring(6); 
            } else { 
            	strUndo = "";
            }
            // count up the number of available tokens
            countToken++;
            // setting the marker for move check
            moveValid = false;
            //Let's invalidate only the valoc and valhc rectangles.
            invalidate(mTileRect[hcx][hcy]);
            invalidate(mTileRect[ocx][ocy]);
        }
    }
    
    void startANewGame () {
        // start a new game
        if (!runGame) {
            // reset the counters and switches
            countToken = mXTileCount * mYTileCount ;
            countTimeM = 0;
            countTimeS = 0;
            strUndo = "";
            runPause = false;
            runGame = true;
        }
    }

    private void checkMoveValidity(int token1, int token2) {       
        // is a game running and is the game not paused?
        if ((runGame) && (!runPause)) {
            moveValid = false;
            // get the coordinates of the held token and the over token
            int ocx = calcXIndex(token1);
            int ocy = calcYIndex(token1);
            int hcx = calcXIndex(token2);
            int hcy = calcYIndex(token2);
            // Is the over token in the same column or in the same row as the held token?
            if ((ocx == hcx) || (ocy == hcy)) {
                // getting the values of the held token and the over token
                int ov = mTileGrid[ocx][ocy];
                int hv = mTileGrid[hcx][hcy];
                int ovx = calcXIndex(ov);
                int ovy = calcYIndex(ov);
                int hvx = calcXIndex(hv);
                int hvy = calcYIndex(hv);
                // Did you get an over token with a same number or the same color as the held token?
                if ((ovx == hvx) || (ovy == hvy)) {
                    moveValid = true;
                }
            }
        }
    }
}
