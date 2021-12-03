package com.example.mytools;

import android.os.CountDownTimer;

public class tools {

    private void kutas(int czasOpoznienie, final int UstaWartoscKroku){
        // 120 seconds coundowntimer
        new CountDownTimer(czasOpoznienie*1000,1000) {

            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
            }
        }.start();
    }
}
