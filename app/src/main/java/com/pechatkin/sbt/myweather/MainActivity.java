package com.pechatkin.sbt.myweather;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.pechatkin.sbt.myweather.forecast.Forecast;

import java.math.BigDecimal;
import java.math.RoundingMode;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 101;
    public static final String BASE_URL = "http://api.openweathermap.org/";
    public static final String API_KEY = "725db862d67260f71863f53010c9c6c9";
    public static final String FAIL = "Fail";

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private TextView mTemp;
    private TextView mDesc;
    private TextView mAddr;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    private void initViews() {
        mTemp = findViewById(R.id.text_view_temp);
        mDesc = findViewById(R.id.text_view_desc);
        mAddr = findViewById(R.id.text_view_address);
        mButton = findViewById(R.id.button_show);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationService();

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePlayServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                } else {
                    finish();
                }
            }
        }
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int statusCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (statusCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = googleApiAvailability.getErrorDialog(this, statusCode,
                    0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            finish();
                        }
                    });

            errorDialog.show();
        } else {
            checkPermission();
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE);
    }

    @SuppressLint("MissingPermission")
    private void startLocationService() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        mFusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        Retrofit retrofit = new Retrofit.Builder()
                                .baseUrl(BASE_URL)
                                .addConverterFactory(GsonConverterFactory.create())
                                .build();
                         IApiHelper apiService = retrofit.create(IApiHelper.class);
                         Call<Forecast> call = apiService.getForecast(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), API_KEY);
                        call.enqueue(new Callback<Forecast>() {
                            @Override
                            public void onResponse(Call<Forecast> call, Response<Forecast> response) {
                                Forecast forecast = response.body();
                                double temp = new BigDecimal(forecast.getMain().getTemp() - 273.15f).setScale(1, RoundingMode.UP).doubleValue();
                                String desc = forecast.getWeather().get(0).getDescription();
                                String addr = forecast.getName();
                                mTemp.setText(String.valueOf(temp));
                                mDesc.setText(desc);
                                mAddr.setText(addr);
                            }

                            @Override
                            public void onFailure(Call<Forecast> call, Throwable t) {
                                Toast.makeText(MainActivity.this, FAIL, Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
    }
}
