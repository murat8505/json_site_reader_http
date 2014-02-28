package com.teamtreehouse.ribbit;

import android.app.Application;

import com.parse.Parse;

public class RibbitApplication extends Application {
	
	@Override
	public void onCreate() {
		super.onCreate();
		  Parse.initialize(this, "iockqzYqOlg5XEgreBm6sazoOemO4w5T6gPjvpoL", "90x3nZ6NnEeOIPjwBevc3kuA8goQWPpmM4rNaLfC");
		}
}
