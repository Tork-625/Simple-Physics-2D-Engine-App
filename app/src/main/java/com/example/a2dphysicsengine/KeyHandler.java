package com.example.a2dphysicsengine;

import android.view.KeyEvent;

import android.view.KeyEvent;
        import android.view.View;

public class KeyHandler implements View.OnKeyListener {
    public boolean uppressed, downpressed, leftpressed, rightpressed, rotate;

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_W:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    uppressed = true;
                    downpressed = false;
                    leftpressed = false;
                    rightpressed = false;
                    rotate = false;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    uppressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_A:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    leftpressed = true;
                    downpressed = false;
                    rightpressed = false;
                    uppressed = false;
                    rotate = false;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    leftpressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_D:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    rightpressed = true;
                    downpressed = false;
                    leftpressed = false;
                    uppressed = false;
                    rotate = false;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    rightpressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_S:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    downpressed = true;
                    leftpressed = false;
                    rightpressed = false;
                    uppressed = false;
                    rotate = false;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    downpressed = false;
                }
                return true;
            case KeyEvent.KEYCODE_R:
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    downpressed = false;
                    leftpressed = false;
                    rightpressed = false;
                    uppressed = false;
                    rotate = true;
                }
                return true;
        }
        return false;
    }
}
