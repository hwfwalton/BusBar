package com.example.guitest;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import static com.example.guitest.Constants.*;


public class ListViewActivity extends ListActivity {
	//private int whichComponent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<String> displayList;
		displayList = getIntent().getStringArrayListExtra(ARRAYLIST_EXTRA);
				
		ArrayAdapter<String> myAdapter = new ArrayAdapter <String>(this, 
				R.layout.row_layout, R.id.listText, displayList);
		setListAdapter(myAdapter);
		setContentView(R.layout.activity_list_view);
		
	}
	
	@Override
	protected void onListItemClick(ListView list, View v, int pos, long id) {
		super.onListItemClick(list, v, pos, id);
		Intent resultsIntent = new Intent();
		resultsIntent.putExtra(USER_SELECTION_INDEX_EXTRA, (int) id);
		
		setResult(RESULT_OK, resultsIntent);
		finish();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);		
		
		//Toast.makeText(getApplicationContext(), "You clicked: "+selectedItem , Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	}
}
