package com.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

interface Helper {
  void helper(int a);
}

class BaseActivity extends Activity implements Helper {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public void helper(int a) {
    if (a > 0) {
      Log.d("tag", "helper");
    }
    Log.d("tag", "helper");
  }
}

class HelperSub implements Helper {
  @Override
  public void helper(int a) {
    Log.d("tag", "helper");
    Log.d("tag", "helper");
  }
}

class Helper2Sub implements Helper {
  @Override
  public void helper(int a) {
    Log.d("tag", "helper");
    Log.d("tag", "helper");
  }
}

public class MainActivity extends BaseActivity {
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    int a = 10;
    for (int i = 0; i < a; i++) {
      helper(3);
    }
    helper(3);
    foo(this);
  }
  
  void foo(Helper helper) {
    helper.helper(0);
    helper.helper(0);
    helper.helper(0);
  }
}
