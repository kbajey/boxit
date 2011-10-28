package com.ajeybk.boxit;

import java.util.List;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BoxitScoreItemAdapter extends ArrayAdapter<BoxitScoreItem> {
  int resource;

  public BoxitScoreItemAdapter(Context _context, int _resource, List<BoxitScoreItem> _items) {
    super(_context, _resource, _items);
    resource = _resource;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout boxitScoreView;

    BoxitScoreItem item = getItem(position);

    String idx = item.getIndex();
    String name = item.getPlayer();
    String tkns = item.getTokens();
    String tm = item.getTime();
    boolean ht = item.getHighlight();

    if (convertView == null) {
    	boxitScoreView = new LinearLayout(getContext());
      String inflater = Context.LAYOUT_INFLATER_SERVICE;
      LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
      vi.inflate(resource, boxitScoreView, true);
    } else {
    	boxitScoreView = (LinearLayout) convertView;
    }

    TextView idView = (TextView)boxitScoreView.findViewById(R.id.rowIdx);
    TextView nameView = (TextView)boxitScoreView.findViewById(R.id.rowName);
    TextView tknView = (TextView)boxitScoreView.findViewById(R.id.rowToken);
    TextView tmView = (TextView)boxitScoreView.findViewById(R.id.rowTime);
    idView.setText(idx);
    nameView.setText(name);
    tknView.setText(tkns);
    tmView.setText(tm);
    if( ht ){
    	boxitScoreView.setBackgroundColor(Color.WHITE);
    }else{
    	boxitScoreView.setBackgroundColor(R.color.puzzle_light);
    }
    
    return boxitScoreView;
  }
}
