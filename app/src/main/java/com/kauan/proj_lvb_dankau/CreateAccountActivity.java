package com.kauan.proj_lvb_dankau;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button btnCreateAccount, btnGetBack;
    private boolean isPasswordVisible = false;

    private final String[] successEmotes = {"⭐", "😊", "💜", "✨", "🌸", "💫", "🎀", "🌟"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnCreateAccount = findViewById(R.id.btn_create_account);
        btnGetBack = findViewById(R.id.btn_get_back);

        btnTogglePassword.setOnClickListener(v -> {
            animatePress(v);
            togglePassword();
        });

        btnCreateAccount.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::attemptCreateAccount, 150);
        });

        btnGetBack.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }, 150);
        });
    }

    private void togglePassword() {
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            isPasswordVisible = false;
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
            isPasswordVisible = true;
        }
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptCreateAccount() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showErrorPopup("Please fill in both email and password.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showErrorPopup("Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showErrorPopup("Password must be at least 6 characters long.");
            return;
        }

        try {
            File file = new File(getFilesDir(), "accounts.json");
            JSONArray accounts = new JSONArray();

            // Load existing accounts if file exists
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(fis);
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[1024];
                int len;
                while ((len = reader.read(buffer)) != -1) sb.append(buffer, 0, len);
                reader.close();
                accounts = new JSONArray(sb.toString());
            }

            // Check if email already exists
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                if (acc.getString("email").equalsIgnoreCase(email)) {
                    showErrorPopup("An account with this email already exists. Please use a different email.");
                    return;
                }
            }

            // Save new account
            JSONObject newAccount = new JSONObject();
            newAccount.put("email", email);
            newAccount.put("password", password);
            accounts.put(newAccount);

            // Write to hidden file in internal storage
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(accounts.toString().getBytes());
            fos.close();

            // Success!
            showSuccessEmote();
            new Handler().postDelayed(() -> {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }, 900);

        } catch (Exception e) {
            showErrorPopup("An error occurred while creating your account.");
            e.printStackTrace();
        }
    }

    private void showSuccessEmote() {
        String emote = successEmotes[new Random().nextInt(successEmotes.length)];

        FrameLayout rootLayout = findViewById(R.id.root_layout);
        TextView emoteView = new TextView(this);
        emoteView.setText(emote);
        emoteView.setTextSize(64f);
        emoteView.setGravity(Gravity.CENTER);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        );
        rootLayout.addView(emoteView, params);

        emoteView.setScaleX(0f);
        emoteView.setScaleY(0f);
        emoteView.setAlpha(1f);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(emoteView, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(emoteView, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(emoteView, "alpha", 1f, 0f);
        fadeOut.setStartDelay(400);
        fadeOut.setDuration(500);
        set.playTogether(scaleX, scaleY);
        set.setDuration(350);
        set.setInterpolator(new OvershootInterpolator());
        set.start();
        new Handler().postDelayed(() -> fadeOut.start(), 350);
        new Handler().postDelayed(() -> rootLayout.removeView(emoteView), 900);
    }

    private void showErrorPopup(String message) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_error);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView tvMessage = dialog.findViewById(R.id.tv_error_message);
        Button btnClose = dialog.findViewById(R.id.btn_close);

        // Set hint text color
        etEmail.setHintTextColor(Color.parseColor("#c0aab0"));
        etPassword.setHintTextColor(Color.parseColor("#c0aab0"));

        tvMessage.setText(message);

        btnClose.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> {
                dialog.dismiss();
                resetFields();
            }, 150);
        });

        dialog.show();
    }

    private void resetFields() {
        etEmail.setText("");
        etPassword.setText("");
        if (isPasswordVisible) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            isPasswordVisible = false;
        }
    }

    private void animatePress(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.93f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.93f, 1f);
        set.playTogether(scaleX, scaleY);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator(3f));
        set.start();
    }
}
