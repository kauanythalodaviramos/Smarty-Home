package com.kauan.proj_lvb_dankau;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private ImageView ivProfile, ivThemeIcon;
    private FrameLayout btnChangeAvatar;
    private EditText etNewName, etNewPassword;
    private ImageButton btnToggleNewPassword;
    private CheckBox cbTheme;
    private TextView tvThemeLabel, tvProfileSubtitle, tvPasswordSubtitle, tvNameSubtitle;
    private Button btnSave, btnDiscard, btnBack;
    private View rootSettings;

    private String currentUserEmail;
    private String base64Image = "";
    private boolean isDarkMode = true;
    private boolean isPasswordVisible = false;
    private String originalName = "";
    private String originalPassword = "";
    private String originalImage = "";
    private boolean originalDarkMode = true;

    private static class DotPasswordTransformationMethod extends PasswordTransformationMethod {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }
        private static class PasswordCharSequence implements CharSequence {
            private final CharSequence mSource;
            public PasswordCharSequence(CharSequence source) { mSource = source; }
            public int length() { return mSource.length(); }
            public char charAt(int index) { return '.'; }
            public CharSequence subSequence(int start, int end) {
                return new PasswordCharSequence(mSource.subSequence(start, end));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        currentUserEmail = getSharedPreferences("app_prefs", MODE_PRIVATE).getString("current_user", "");

        ivProfile = findViewById(R.id.iv_profile);
        ivThemeIcon = findViewById(R.id.iv_theme_icon);
        btnChangeAvatar = findViewById(R.id.btn_change_avatar);
        etNewName = findViewById(R.id.et_new_name);
        etNewPassword = findViewById(R.id.et_new_password);
        btnToggleNewPassword = findViewById(R.id.btn_toggle_new_password);
        cbTheme = findViewById(R.id.cb_theme);
        tvThemeLabel = findViewById(R.id.tv_theme_label);
        tvProfileSubtitle = findViewById(R.id.tv_profile_subtitle);
        tvNameSubtitle = findViewById(R.id.tv_name_subtitle);
        tvPasswordSubtitle = findViewById(R.id.tv_password_subtitle);
        btnSave = findViewById(R.id.btn_save);
        btnDiscard = findViewById(R.id.btn_discard);
        btnBack = findViewById(R.id.btn_back_to_dashboard);
        rootSettings = findViewById(R.id.root_settings);

        ivProfile.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        ivProfile.setClipToOutline(true);

        etNewName.setTextColor(Color.BLACK);
        etNewName.setHintTextColor(Color.GRAY);
        etNewPassword.setTextColor(Color.BLACK);
        etNewPassword.setHintTextColor(Color.GRAY);

        loadUserData();

        btnChangeAvatar.setOnClickListener(v -> {
            animatePress(v);
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
        });

        btnToggleNewPassword.setOnClickListener(v -> {
            animatePress(v);
            togglePasswordVisibility();
        });

        cbTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isDarkMode = !isChecked;
            updateThemeUI();
        });

        btnSave.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::saveChanges, 150);
        });

        btnDiscard.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::discardChanges, 150);
        });

        btnBack.setOnClickListener(v -> {
            animatePress(v);
            new Handler().postDelayed(this::finish, 150);
        });
        
        etNewPassword.setTransformationMethod(new DotPasswordTransformationMethod());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etNewPassword.setTransformationMethod(new DotPasswordTransformationMethod());
            btnToggleNewPassword.setImageResource(R.drawable.ic_eye_closed);
            isPasswordVisible = false;
        } else {
            etNewPassword.setTransformationMethod(null);
            btnToggleNewPassword.setImageResource(R.drawable.ic_eye_open);
            isPasswordVisible = true;
        }
        etNewPassword.setText(etNewPassword.getText());
        etNewPassword.setSelection(etNewPassword.getText().length());
    }

    private void loadUserData() {
        try {
            File file = new File(getFilesDir(), "accounts.json");
            if (!file.exists()) return;

            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) sb.append(buffer, 0, len);
            reader.close();

            JSONArray accounts = new JSONArray(sb.toString());
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                if (acc.getString("email").equalsIgnoreCase(currentUserEmail)) {
                    originalName = acc.optString("name", "");
                    originalPassword = acc.getString("password");
                    originalImage = acc.optString("profile_image", "");
                    originalDarkMode = acc.optBoolean("dark_mode", true);

                    etNewName.setText(originalName);
                    base64Image = originalImage;
                    isDarkMode = originalDarkMode;
                    
                    if (!base64Image.isEmpty()) {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        ivProfile.setImageBitmap(decodedByte);
                    }
                    
                    cbTheme.setChecked(!isDarkMode);
                    updateThemeUI();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateThemeUI() {
        if (isDarkMode) {
            rootSettings.setBackgroundColor(Color.parseColor("#1e2124"));
            tvThemeLabel.setText("Dark Mode");
            ivThemeIcon.setImageResource(android.R.drawable.star_big_off);
            tvThemeLabel.setTextColor(Color.BLACK);
            tvProfileSubtitle.setTextColor(Color.parseColor("#cab2fb"));
            tvNameSubtitle.setTextColor(Color.parseColor("#cab2fb"));
            tvPasswordSubtitle.setTextColor(Color.parseColor("#cab2fb"));
        } else {
            rootSettings.setBackgroundColor(Color.parseColor("#f0f0f0"));
            tvThemeLabel.setText("Light Mode");
            ivThemeIcon.setImageResource(android.R.drawable.star_big_on);
            tvThemeLabel.setTextColor(Color.BLACK);
            tvProfileSubtitle.setTextColor(Color.BLACK);
            tvNameSubtitle.setTextColor(Color.BLACK);
            tvPasswordSubtitle.setTextColor(Color.BLACK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivProfile.setImageBitmap(bitmap);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                byte[] b = baos.toByteArray();
                base64Image = Base64.encodeToString(b, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveChanges() {
        String newName = etNewName.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        if (newPassword.isEmpty()) newPassword = originalPassword;

        try {
            File file = new File(getFilesDir(), "accounts.json");
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader reader = new InputStreamReader(fis);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) sb.append(buffer, 0, len);
            reader.close();

            JSONArray accounts = new JSONArray(sb.toString());
            for (int i = 0; i < accounts.length(); i++) {
                JSONObject acc = accounts.getJSONObject(i);
                if (acc.getString("email").equalsIgnoreCase(currentUserEmail)) {
                    acc.put("name", newName);
                    acc.put("password", newPassword);
                    acc.put("profile_image", base64Image);
                    acc.put("dark_mode", isDarkMode);
                    break;
                }
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(accounts.toString().getBytes());
            fos.close();

            originalName = newName;
            originalPassword = newPassword;
            originalImage = base64Image;
            originalDarkMode = isDarkMode;

            Toast.makeText(this, "Changes saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving changes", Toast.LENGTH_SHORT).show();
        }
    }

    private void discardChanges() {
        etNewName.setText(originalName);
        base64Image = originalImage;
        isDarkMode = originalDarkMode;
        etNewPassword.setText("");
        
        if (!base64Image.isEmpty()) {
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ivProfile.setImageBitmap(decodedByte);
        } else {
            ivProfile.setImageResource(R.drawable.ic_launcher_background);
        }
        
        cbTheme.setChecked(!isDarkMode);
        updateThemeUI();
        Toast.makeText(this, "Changes discarded", Toast.LENGTH_SHORT).show();
    }

    private void animatePress(View view) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator sx = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.93f, 1f);
        ObjectAnimator sy = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.93f, 1f);
        set.playTogether(sx, sy);
        set.setDuration(200);
        set.setInterpolator(new OvershootInterpolator(3f));
        set.start();
    }
}
