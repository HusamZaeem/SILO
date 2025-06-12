package com.silo;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

public class TypingAnimator {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int index = 0;
    private String text = "";
    private TextView textView;
    private long delay = 100; // Delay between characters

    private final Runnable typingRunnable = new Runnable() {
        @Override
        public void run() {
            if (textView != null && index <= text.length()) {
                textView.setText(text.substring(0, index));
                index++;
                handler.postDelayed(this, delay);
            }
        }
    };

    public void startTyping(TextView targetView, String fullText, long startDelayMillis, long charDelayMillis) {
        // Cancel any previous typing animation
        cancel();

        this.textView = targetView;
        this.text = fullText != null ? fullText : "";
        this.delay = charDelayMillis;
        this.index = 0;

        if (textView != null) {
            textView.setText("");
        }

        handler.postDelayed(typingRunnable, startDelayMillis);
    }

    public void cancel() {
        handler.removeCallbacks(typingRunnable);
    }
}
