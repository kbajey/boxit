package com.ajeybk.boxit;

import java.util.ArrayList;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class BoxitScoreActivity extends ListActivity implements OnClickListener {
    private Button mResetButton;
    private Button mCloseButton;
    private BoxitScoreItemAdapter mAdapter;
    private ArrayList<BoxitScoreItem> boxitScoreItems = new ArrayList<BoxitScoreItem>();
    boolean resetDone = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        List<String> scoreList = getIntent().getStringArrayListExtra(BoxitActivity.SCORESLIST);
        populateTheScoresList(scoreList);
        mAdapter = new BoxitScoreItemAdapter(getApplicationContext(), R.layout.boxitscorelist_item, boxitScoreItems);    
        setListAdapter(mAdapter);
        setContentView(R.layout.boxitscoreview);
        int k = getIntent().getIntExtra(BoxitActivity.SCOREPOSITION, -1);
        	if( k > 0 && k <= boxitScoreItems.size() ){
        	this.getListView().setSelection(k);
        	BoxitScoreItem obj = (BoxitScoreItem)getListView().getItemAtPosition(k-1);
            obj.setHighlight();
        }
        mResetButton = (Button) findViewById(R.id.resetButton);
        mResetButton.setOnClickListener(this);
        mCloseButton = (Button) findViewById(R.id.closeScore);
        mCloseButton.setOnClickListener(this);
        if( boxitScoreItems.size() == 0 ){
        	mResetButton.setEnabled(false);
        }
    }

    public void onClick(View v) {
    	if( v == mResetButton ){
	    	DialogInterface.OnClickListener yesListen = new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int which) {
	    			boxitScoreItems.clear();
	    			resetDone = true;
	    	        mAdapter.notifyDataSetChanged();
	    	        mResetButton.setEnabled(false);
	    		}
	    	};
	    	
	    	DialogInterface.OnClickListener noListen = new DialogInterface.OnClickListener() {	
	    		public void onClick(DialogInterface dialog,int which) {
	    			;//
	    		}
	    	};
	    	AlertDialog.Builder ad = new AlertDialog.Builder(this);
	  	  	ad.setTitle(R.string.deletescores);
	  	  	ad.setMessage(R.string.resetscoresquestion);
	  	  	ad.setPositiveButton(R.string.yesstr,yesListen);
	  	  	ad.setNegativeButton(R.string.nostr,noListen).show();
    	}else{
    		Intent intent = getIntent();
    		intent.putExtra(BoxitActivity.RESETDONE,resetDone); 
    		setResult(RESULT_OK, intent); 
        	finish();
    	}
    }
    
    @Override
    public void onBackPressed() {
    	Intent intent = getIntent();
		intent.putExtra(BoxitActivity.RESETDONE,resetDone); 
		setResult(RESULT_OK, intent); 
    	super.onBackPressed();
    }
    
    private void populateTheScoresList(List<String> scoreList){
    	if( scoreList != null ){
    		int lenScore = scoreList.size();
    		if (lenScore > 0) {
    			for (int i = 0; i < lenScore; i++) {
    				String val = scoreList.get(i);
    				String setScore[] = val.split(";");
    				if ((setScore[0].length() > 0) && (setScore[1].length() > 0)  && (setScore[2].length() > 0)) {
    					int k  = i+1;
    					BoxitScoreItem bvs = new BoxitScoreItem(""+k, setScore[0],setScore[1],setScore[2]);
    					boxitScoreItems.add(bvs);
    				}
    			}
    		}
    	}
    }
}
