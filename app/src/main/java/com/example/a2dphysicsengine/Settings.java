package com.example.a2dphysicsengine;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Settings extends AppCompatActivity {

    static SharedPreferences sharedPreferences;
    public int fpsValue;
    public double rotationFactorValue;
    public double movingForceValue;
    public double gValue;
    public boolean frictionSwitch;
    public boolean gravitySwitch;
    private EditText editTextFPS;
    private EditText editTextRotationFactor;
    private EditText editTextMovingForce;
    private EditText editTextG;
    private Switch switchFriction;
    private Switch switchGravity;
    int i = 0;

    public void setpref(Context context) {
        sharedPreferences = context.getSharedPreferences("Engine_Settings", MODE_PRIVATE);
        //loadSavedValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("Engine_Settings", MODE_PRIVATE);

        // Initialize EditText fields
        editTextFPS = findViewById(R.id.editTextFPS);
        editTextRotationFactor = findViewById(R.id.editTextRotationFactor);
        editTextMovingForce = findViewById(R.id.editTextMovingForce);
        editTextG = findViewById(R.id.editTextg);
        switchFriction = findViewById(R.id.switchFriction);
        switchGravity = findViewById(R.id.switchGravity);

        // Set listeners for Switch buttons
        switchFriction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveFrictionState(isChecked);
            }
        });

        switchGravity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveGravityState(isChecked);
            }
        });

        // Set TextWatcher listeners
        editTextFPS.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateFPSValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveValueToSharedPreferences("fpsValue", editable.toString());
            }
        });

        editTextRotationFactor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateRotationFactorValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveValueToSharedPreferences("rotationFactorValue", editable.toString());
            }
        });

        editTextMovingForce.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateMovingForceValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveValueToSharedPreferences("movingForceValue", editable.toString());
            }
        });

        editTextG.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updateGValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                saveValueToSharedPreferences("gValue", editable.toString());
            }
        });

        // Load saved values from SharedPreferences
        loadSavedValues();
    }

    public void loadSavedValues() {
        // Retrieve saved values from SharedPreferences
        fpsValue = sharedPreferences.getInt("fpsValue", 300);
        rotationFactorValue = Double.longBitsToDouble(sharedPreferences.getLong("rotationFactorValue", Double.doubleToLongBits(16)));
        movingForceValue = Double.longBitsToDouble(sharedPreferences.getLong("movingForceValue", Double.doubleToLongBits(800)));
        gValue = Double.longBitsToDouble(sharedPreferences.getLong("gValue", Double.doubleToLongBits(9.8 * 70)));
        frictionSwitch = sharedPreferences.getBoolean("frictionSwitch", true);
        gravitySwitch = sharedPreferences.getBoolean("gravitySwitch", true);

        // Set EditText fields with the loaded values
        editTextFPS.setText(String.valueOf(fpsValue));
        editTextRotationFactor.setText(String.valueOf(rotationFactorValue));
        editTextMovingForce.setText(String.valueOf(movingForceValue));
        editTextG.setText(String.valueOf(gValue));
        switchFriction.setChecked(frictionSwitch);
        switchGravity.setChecked(gravitySwitch);
    }

    private void saveValueToSharedPreferences(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (key) {
            case "fpsValue":
                editor.putInt("fpsValue", Integer.parseInt(value));
                break;
            case "rotationFactorValue":
                editor.putLong("rotationFactorValue", Double.doubleToRawLongBits(Double.parseDouble(value)));
                break;
            case "movingForceValue":
                editor.putLong("movingForceValue", Double.doubleToRawLongBits(Double.parseDouble(value)));
                break;
            case "gValue":
                editor.putLong("gValue", Double.doubleToRawLongBits(Double.parseDouble(value)));
                break;
            case "frictionSwitch":
                editor.putBoolean("frictionSwitch", Boolean.parseBoolean(value));
                break;
            case "gravitySwitch":
                editor.putBoolean("gravitySwitch", Boolean.parseBoolean(value));
                break;
        }
        editor.apply();
    }

    private void updateFPSValue(String input) {
        try {
            fpsValue = Integer.parseInt(input);
            Log.d("Settings", "Updated FPS value: " + fpsValue);
        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    private void updateRotationFactorValue(String input) {
        try {
            rotationFactorValue = Double.parseDouble(input);
            Log.d("Settings", "Updated rotation factor value: " + rotationFactorValue);
        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    private void updateMovingForceValue(String input) {
        try {
            movingForceValue = Double.parseDouble(input);
            Log.d("Settings", "Updated moving force value: " + movingForceValue);
        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    private void updateGValue(String input) {
        try {
            gValue = Double.parseDouble(input);
            Log.d("Settings", "Updated g value: " + gValue);
        } catch (NumberFormatException e) {
            // Handle invalid input
        }
    }

    private void saveFrictionState(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("frictionSwitch", value);
        frictionSwitch = value;
        editor.apply();
    }

    private void saveGravityState(boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("gravitySwitch", value);
        gravitySwitch = value;
        editor.apply();
    }
}