/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cai.zxingportrait;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import zxingportrait.CaptureFragment;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public class CaptureActivity extends Activity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(android.R.id.content, new CaptureFragment());
        fragmentTransaction.commitAllowingStateLoss();
    }

    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // switch (keyCode) {
    // case KeyEvent.KEYCODE_FOCUS:
    // case KeyEvent.KEYCODE_CAMERA:
    // // Handle these events so they don't launch the Camera app
    // return true;
    // // Use volume up/down to turn on light
    // case KeyEvent.KEYCODE_VOLUME_DOWN:
    // cameraManager.setTorch(false);
    // return true;
    // case KeyEvent.KEYCODE_VOLUME_UP:
    // cameraManager.setTorch(true);
    // return true;
    // }
    // return super.onKeyDown(keyCode, event);
    // }
}
