package com.ajeybk.boxit;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class BoxitActivity extends Activity {
	
	private static final String ZEROZERO="00";
	private static final String OPTIONS="Options";
	private static final String SOUNDOPTION="Sound";
	private static final String SCORES="Scores";
	private static final int MAXSCORES = 20;
	private static final int SCOREACTIVITY = 1;
		
	public static final String SCOREFILE="boxit.ini";
	public static final String PLAYERNAME="Player";
	public static final String SCOREPOSITION="scoreposition";
	public static final String SCORESLIST="scoreslist";
	public static final String RESETDONE="resetdone";
	
	public static final String GAMEFLAG="gameover";
	public static final String RUNFLAG="rungame";
	public static final String SOUNDFLAG="playsound";
	  
	public static final int GETPLAYERNAME = 1;
	public static final int SAVESCORES = 2;
	public static final int GAMEOVERSCOREUPDATE = 3;
	
	static final private int OPEN_SCORES = Menu.FIRST;
	static final private int ABOUT = Menu.FIRST  + 1 ;
	static final private int QUIT = Menu.FIRST  + 2 ;
	
	int mrkScore = -1;
	String strName = PLAYERNAME;
	ArrayList<String> dataScores;
	private boolean restoredGame;
	
	Button playBtn;
	Button pictureBtn;
	TextView scoreView;
	BoxitView bv;
	
	boolean runGame = false;
	boolean gameOver = false;

	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case GETPLAYERNAME:
			    getPlayerName();
				break;
			case SAVESCORES:
				calcAndAddScore();
				break;
			case GAMEOVERSCOREUPDATE:
				openGameOverDialog();
				break;
			}	
		}
	};
	private Runnable mUpdateTimeTask = new Runnable() {   
		public void run() {       
			bv.countTimeS++;
			if( bv.countTimeS == 60){
				bv.countTimeM++;
				bv.countTimeS = 0;
				if( bv.countTimeM == 60 )
					bv.countTimeM = 0;
			}
			showScore();
			mHandler.postDelayed(this,1000);
		}
	};
	OnClickListener mStartListener = new OnClickListener() {   
		public void onClick(View v) {
			int upTime = bv.countTimeM * 60 + bv.countTimeS;
			if (upTime == 0) {
				showScore();
				mHandler.removeCallbacks(mUpdateTimeTask);
				startNewGame();
				mHandler.postDelayed(mUpdateTimeTask, 1000);
			}else{
				if( runGame ){
					setGamePaused();
					mHandler.removeCallbacks(mUpdateTimeTask);
				}else{
					if( gameOver ){
						mrkScore = -1;
						gameOver  = false;
						runGame = true;
						bv.jumbleUp(true);
						mHandler.postDelayed(mUpdateTimeTask, 1000);
						bv.startANewGame();
						showScore();
						playBtn.setText(R.string.pause);
						pictureBtn.setText(R.string.undo);
						pictureBtn.setEnabled(true);
					}else{
						mHandler.postDelayed(mUpdateTimeTask, 1000);
						setGameRunning();
					}
				}
			}
		}
	};
	
	OnClickListener mPictureListener = new OnClickListener() {   
		public void onClick(View v) {
			if( !runGame ){
				if( !gameOver)
					finish();
				else{
					Intent  it  = new Intent(BoxitActivity.this, BoxitScoreActivity.class);
					it.putExtra(SCOREPOSITION, mrkScore);
					it.putStringArrayListExtra(SCORESLIST,dataScores);
					startActivityForResult(it,SCOREACTIVITY);
				}
			}else{
				bv.performUndo();
				showScore();
			}
		}
	};

	DialogInterface.OnClickListener yesListen = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			mHandler.removeCallbacks(mUpdateTimeTask);
			setGameover();
			postGetPlayerName();
		}
	};
	
	DialogInterface.OnClickListener noListen = new DialogInterface.OnClickListener() {	
		public void onClick(DialogInterface dialog,int which) {
			mHandler.removeCallbacks(mUpdateTimeTask);
		}
	};
	
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    int mode = getResources().getConfiguration().orientation;
    if( mode == Configuration.ORIENTATION_LANDSCAPE){
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    }
    setContentView(R.layout.boxitview);
    bv = (BoxitView)this.findViewById(R.id.boxitView);
    playBtn = (Button)findViewById(R.id.play);
    pictureBtn = (Button)findViewById(R.id.picture);
    scoreView = (TextView)findViewById(R.id.score);
    scoreView.setGravity(android.view.Gravity.CENTER);
    playBtn.setOnClickListener(mStartListener);
    pictureBtn.setOnClickListener(mPictureListener);
    if (icicle != null) {
    	gameOver = icicle.getBoolean(GAMEFLAG,false);
    	runGame = icicle.getBoolean(RUNFLAG,false);
    	restoredGame = true;
    }
    new Thread(new Runnable() {
        public void run() {
        	 openTheScores();
        }
    }).start();
  }
  
  public void showScore(){
	  DecimalFormat leadZero = new DecimalFormat(ZEROZERO);
	  String val = leadZero.format(bv.countTimeM) + ":" + leadZero.format(bv.countTimeS);
	  scoreView.setText(val);
  }
  
  public void openGameOverDialog(){
	  AlertDialog.Builder ad = new AlertDialog.Builder(this);
	  ad.setTitle(R.string.gameover);
	  ad.setMessage(R.string.updatescorequestion);
	  ad.setPositiveButton(R.string.yesstr,yesListen);
	  ad.setNegativeButton(R.string.nostr,noListen).show();
  }
  
  private void createEmptyScoreFile(){
	  try{
		  FileOutputStream scoreOutFile = this.openFileOutput(SCOREFILE,MODE_PRIVATE);
		  BoxitSaveFile iniFile = new BoxitSaveFile(scoreOutFile);
          iniFile.WriteInteger(OPTIONS,SOUNDOPTION, 0);
          iniFile.WriteInteger(OPTIONS,SCORES, 0);
          iniFile.UpdateFile();
	  }catch(IOException ex){
		  
	  }
  }
  
  public void postGameOver(){
  	mHandler.sendMessage(Message.obtain(mHandler,GAMEOVERSCOREUPDATE));
  }
  
  public void postGetPlayerName(){
  	mHandler.sendMessage(Message.obtain(mHandler,GETPLAYERNAME));
  }
  
  public void postSaveTheScores(){
	  mHandler.sendMessage(Message.obtain(mHandler,SAVESCORES));
  }
  
  private void openTheScores(){
      dataScores = new ArrayList<String>();
	  int lenScore = 0;
	  String fileList[] = this.fileList();
	  boolean filePresent = false;
	  if( fileList != null ){  
		  for(int i= 0; i<fileList.length; i++){
			  if( fileList[i].equals(SCOREFILE)){
				  filePresent = true;
				  break;
			  }
		  }
	  }
	  if( !filePresent){
		  createEmptyScoreFile();
	  }  
	  try {
		  FileInputStream scoreFile = this.openFileInput(SCOREFILE);
		  BoxitSaveFile iniFile = new BoxitSaveFile(scoreFile);
		  if( !iniFile.readFile() ){
			  iniFile = null;
			  return;
		  }
		  lenScore = iniFile.ReadInteger(OPTIONS,SCORES, 0);
	      if (lenScore > 0) {
	            String[] setScore = new String[3];
	            for (int i = 0; i < lenScore; i++) {
	                setScore[0] = iniFile.ReadString(SCORES, Integer.toString(i + 1) + "-Name", "");
	                setScore[1] = iniFile.ReadString(SCORES, Integer.toString(i + 1) + "-Tokens", "");
	                setScore[2] = iniFile.ReadString(SCORES, Integer.toString(i + 1) + "-Time", "");
	                if ((setScore[0].length() > 0) && (setScore[1].length() > 0) && (setScore[2].length() > 0)) {
	                	String val = setScore[0] + ";" + setScore[1] + ";" + setScore[2];
	                    dataScores.add(val);
	                }
	            }
	     }
	     iniFile = null;
	  }catch(Exception e){
	  }
    }
    
    private void writeTheScores(){
		 int lenScore = dataScores.size();
	     try{
			  FileOutputStream scoreOutFile = this.openFileOutput(SCOREFILE,MODE_PRIVATE);
			  BoxitSaveFile iniFile = new BoxitSaveFile(scoreOutFile);
			  iniFile.WriteInteger(OPTIONS,SOUNDOPTION, 0);
	          iniFile.WriteInteger(OPTIONS,SCORES, lenScore);
			  if (lenScore > 0) {
	                for (int i = 0; i < lenScore; i++) {
	                	String val = dataScores.get(i);
	                    String setScore[] = val.split(";");
	                    iniFile.WriteString(SCORES, Integer.toString(i + 1) + "-Name", setScore[0]);
	                    iniFile.WriteString(SCORES, Integer.toString(i + 1) + "-Tokens", setScore[1]);
	                    iniFile.WriteString(SCORES, Integer.toString(i + 1) + "-Time", setScore[2]);
	                }
	                iniFile.UpdateFile();
	         }
		  }catch(FileNotFoundException ex){
			  
		  }
	}
    
    private void calcAndAddScore () {
    	  if( dataScores.size() > MAXSCORES) {
			while(dataScores.size() > MAXSCORES )
				dataScores.remove(MAXSCORES);
    	  }
		  DecimalFormat leadZero = new DecimalFormat(ZEROZERO);
	      int lenScore = dataScores.size();
	      String val = strName + ";" + leadZero.format(bv.countToken) + ";" + 
	                   leadZero.format(bv.countTimeM) + ":" + leadZero.format(bv.countTimeS);
	      if (lenScore == 0) {
	          mrkScore = 1;
	      }else { 
	          // sort the highscores
	          mrkScore = lenScore + 1;
	          for (int i = (lenScore - 1); i >= 0; i--) {
	          	String oldScore = dataScores.get(i);
	          	String getScore[] = oldScore.split(";");
	          	int getToken = Integer.parseInt(getScore[1]);
	          	int countTime = (bv.countTimeM * 60) + bv.countTimeS; 
	          	int getTime = (Integer.parseInt(getScore[2].substring(0, 2)) * 60) + Integer.parseInt(getScore[2].substring(3, 5));
	          	if ((bv.countToken < getToken) || ((bv.countToken == getToken) && (countTime < getTime))) {
	          		mrkScore = i + 1;
	          	}
	          }
	      }
	      dataScores.add(mrkScore - 1,val);
	}
  
  private void getPlayerName(){
	  AlertDialog.Builder alert = new AlertDialog.Builder(this);

	  alert.setTitle(R.string.playername);
	  alert.setMessage(R.string.enterplayername);

	  // Set an EditText view to get user input 
	  final EditText input = new EditText(this);
	  input.setText(strName);
	  alert.setView(input);

	  alert.setPositiveButton(R.string.okstr, new DialogInterface.OnClickListener() {
	  public void onClick(DialogInterface dialog, int whichButton) {
		  String value = input.getText().toString();
		  if( value != null && !value.equals("") ){
			  strName = value;
		  }
		  postSaveTheScores();
	    }
	  });
	  alert.setNegativeButton(R.string.cancelstr, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int whichButton) {
	      // Canceled.
	    }
	  });

	  alert.show();
  }
  
  public boolean onCreateOptionsMenu(Menu menu){
  	super.onCreateOptionsMenu(menu);
  	menu.add(0,OPEN_SCORES,Menu.NONE,R.string.scoresmenu);
  	menu.add(1,ABOUT,Menu.NONE,R.string.aboutstr);
  	menu.add(2,QUIT,Menu.NONE,R.string.quitstr);
  	return true;
  }
  
  public boolean onOptionsItemSelected(MenuItem item){
  	super.onOptionsItemSelected(item);
  	switch(item.getItemId()){
  		case OPEN_SCORES:
  			synchronized( dataScores ){
	  			if( dataScores.size() > MAXSCORES) {
	  				while(dataScores.size() > MAXSCORES )
	  					dataScores.remove(MAXSCORES);
	  	    	}
	  			Intent it = new Intent(this, BoxitScoreActivity.class);
	  			it.putExtra(SCOREPOSITION, mrkScore);
	  			it.putStringArrayListExtra(SCORESLIST,dataScores);
	  			startActivityForResult(it,SCOREACTIVITY);
  			}
  			return true;
  		case ABOUT:
  			startActivity(new Intent(this, BoxitAboutActivity.class));
  			return true;
  		case QUIT:
  			if( runGame || gameOver ){
  				AlertDialog.Builder builder = new AlertDialog.Builder(this);
  				builder.setMessage(R.string.exitquestion)
  			    .setCancelable(false)
  			    .setPositiveButton(R.string.yesstr, new DialogInterface.OnClickListener() {
  			    	public void onClick(DialogInterface dialog, int id) {
  			    		BoxitActivity.this.finish();
  			        }
  			    })
  			    .setNegativeButton(R.string.nostr, new DialogInterface.OnClickListener() {
  			        public void onClick(DialogInterface dialog, int id) {
  			        	dialog.cancel();
  			        }
  			    });
  				AlertDialog alert = builder.create();
  				alert.show();
  			} else{
  				finish();
  			}
  			return true;
  	}
  	return false;
  }
  
  @Override
  protected void onStart() {
      super.onStart();
      if( !restoredGame ){
      	bv.jumbleUp(true);
      	restoredGame = true;
      }
  }
  
  @Override
  protected void onStop() {
      super.onStop();
      if( runGame ){
        	mHandler.removeCallbacks(mUpdateTimeTask);
      }
  }
  
  @Override
  protected void onDestroy() {
      super.onDestroy();
      writeTheScores();
  }
  
  @Override
  public void onSaveInstanceState(Bundle outState) {
    //Store the game state
  	if( runGame ){
      	mHandler.removeCallbacks(mUpdateTimeTask);
    }
  	outState.putBoolean(GAMEFLAG, gameOver);
  	outState.putBoolean(RUNFLAG,runGame);
  	super.onSaveInstanceState(outState); 
  }
  
  public void onRestoreInstanceState(Bundle inState){
  	super.onRestoreInstanceState(inState);
  	int upTime = bv.countTimeM * 60 + bv.countTimeS;
  	if( gameOver ){
  		playBtn.setText(R.string.newstr);
		pictureBtn.setText(R.string.viewscores);
  	}else if( runGame ){
  		playBtn.setText(R.string.pause);
  		pictureBtn.setText(R.string.undo);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
	}else if ( upTime > 0 ) {
		playBtn.setText(R.string.resumestr);
		pictureBtn.setText(R.string.quitstr);
	}
  	showScore();
  }
  
  private void startNewGame(){
	  mrkScore = -1;
	  playBtn.setText(R.string.pause);
	  runGame = true;
	  bv.startANewGame();
	  pictureBtn.setText(R.string.undo);
  }
  
  private void setGameRunning(){
	  playBtn.setText(R.string.pause);
	  runGame = true;
	  bv.runGame = true;
	  bv.runPause = false;
	  pictureBtn.setText(R.string.undo);
  }
  
  private void setGamePaused(){
	  playBtn.setText(R.string.resumestr);
	  pictureBtn.setText(R.string.quitstr);
	  runGame = false;
	  bv.runGame = true;
	  bv.runPause = true;
  }
  
  private void setGameover(){
	  gameOver = true;	
	  playBtn.setText(R.string.newstr);
	  pictureBtn.setText(R.string.viewscores);
	  runGame = false;
	  bv.runGame = false;
	  bv.runPause = false;
  }
  
  @Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
  		if (resultCode == RESULT_OK)     {
			if( requestCode == SCOREACTIVITY ){
				boolean resetDone = data.getBooleanExtra(RESETDONE,false);
				if( resetDone )
					dataScores.clear();
				else if( dataScores.size() > MAXSCORES) {
					while(dataScores.size() > MAXSCORES )
						dataScores.remove(MAXSCORES);
				}
				mrkScore = -1;
			}
  		}
		super.onActivityResult(requestCode, resultCode, data); 
	}
}
