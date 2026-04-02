package com.kauan.proj_lvb_dankau;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private ImageButton btnTogglePassword;
    private Button btnLogin, btnCreateAccount;
    private boolean isPasswordVisible = false;

    // Cute emotes for success
    private final String[] successEmotes = {"⭐", "😊", "💜", "✨", "🌸", "💫", "🎀", "🌟"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnTogglePassword = findViewById(R.id.btn_toggle_password);
        btnLogin = findViewById(R.id.btn_login);
        btnCreateAccount = findViewById(R.id.btn_create_account);

        // Toggle password visibility
        btnTogglePassword.setOnClickListener(v -> {
            animatePress(v);
            togglePassword();
        });

        // Login button
        btnLogin.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::attemptLogin, 150);
        });

        // Create account button
        btnCreateAccount.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }, 150);
        });
    }

    private void togglePassword() {
        if (isPasswordVisible) {
            // Hide password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_closed);
            isPasswordVisible = false;
        } else {
            // Show password
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            btnTogglePassword.setImageResource(R.drawable.ic_eye_open);
            isPasswordVisible = true;
        }
        // Keep cursor at end
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
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

        // Read accounts from JSON file
        try {
            File file = new File(getFilesDir(), "accounts.json");
            if (!file.exists()) {
                showErrorPopup("No accounts found. Please create an account first.");
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) sb.append(buffer, 0, len);
            reader.close();

            JSONArray accounts = new JSONArray(sb.toString());
            boolean found = false;

            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                if (acc.getString("email").equalsIgnoreCase(email)
                        && acc.getString("password").equals(password)) {
                    found = true;
                    break;
                }
            }

            if (found) {
                showSuccessEmote();
                new Handler().postDelayed(() -> {
                    // TODO: Navigate to your main screen
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                    // Intent intent = new Intent(this, MainActivity.class);
                    // startActivity(intent);
                    // finish();
                }, 800);
            } else {
                showErrorPopup("Incorrect email or password. Please try again.");
            }

        } catch (Exception e) {
            showErrorPopup("An error occurred while reading accounts.");
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

        // Animate: scale in + fade out
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
        new Handler().postDelayed(() ->
                fadeOut.start(), 350
        );
        new Handler().postDelayed(() ->
                rootLayout.removeView(emoteView), 900
        );
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
