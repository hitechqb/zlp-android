package com.hoangld.motocare;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.hoangld.motocare.Api.CreateOrder;
import com.hoangld.motocare.Constant.AppInfo;

import org.json.JSONObject;

import vn.zalopay.listener.ZaloPayListener;
import vn.zalopay.sdk.ZaloPayErrorCode;
import vn.zalopay.sdk.ZaloPaySDK;

public class MainActivity extends AppCompatActivity {
    TextView lblZpTransToken, txtToken;
    Button btnCreateOrder, btnPay, btnToWevView;
    EditText txtAmount;

    private void BindView() {
        txtToken = findViewById(R.id.txtToken);
        lblZpTransToken = findViewById(R.id.lblZpTransToken);
        btnCreateOrder = findViewById(R.id.btnCreateOrder);
        txtAmount = findViewById(R.id.txtAmount);
        btnPay = findViewById(R.id.btnPay);
        btnToWevView = findViewById(R.id.btnToWebView);
        IsLoading();
    }

    private void IsLoading() {
        lblZpTransToken.setVisibility(View.INVISIBLE);
        txtToken.setVisibility(View.INVISIBLE);
        btnPay.setVisibility(View.INVISIBLE);
    }

    private void IsDone() {
        lblZpTransToken.setVisibility(View.VISIBLE);
        txtToken.setVisibility(View.VISIBLE);
        btnPay.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Khoi Tao SDK
        ZaloPaySDK.getInstance().initWithAppId(AppInfo.APP_ID);

        // bind components with ids
        BindView();

        // Todo: Demo WebView
        btnToWevView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebViewDemo();
            }
        });
        // handle CreateOrder
        btnCreateOrder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {
                CreateOrder orderApi = new CreateOrder();

                try {
                    JSONObject data = orderApi.createOrder(txtAmount.getText().toString());
                    lblZpTransToken.setVisibility(View.VISIBLE);
                    String code = data.getString("returncode");
                    Toast.makeText(getApplicationContext(), "return_code: " + code, Toast.LENGTH_LONG).show();

                    if (code.equals("1")) {
                        lblZpTransToken.setText("zptranstoken");
                        txtToken.setText(data.getString("zptranstoken"));
                        IsDone();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String token = txtToken.getText().toString();
                ZaloPaySDK.getInstance().payOrder(MainActivity.this, token, new ZaloPayListener() {

                    @Override
                    public void onPaymentSucceeded(final String transactionId, final String transToken) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Payment Success")
                                        .setMessage(String.format("TransactionId: %s - TransToken: %s", transactionId, transToken))
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        })
                                        .setNegativeButton("Cancel", null).show();
                            }
                        });

                        IsLoading();
                    }

                    @Override
                    public void onPaymentError(ZaloPayErrorCode zaloPayErrorCode, int paymentErrorCode, String zpTransToken) {
                        if (zaloPayErrorCode == ZaloPayErrorCode.ZALO_PAY_NOT_INSTALLED) {
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Error Payment")
                                            .setMessage("ZaloPay App not install on this Device.")
                                            .setPositiveButton("Open Market", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    ZaloPaySDK.getInstance().navigateToStore(MainActivity.this);
                                                }
                                            })
                                            .setNegativeButton("Back", null).show();


                                }
                            }, 500);
                            Log.d("CODE_NOT_INSTALL", "onError: <br> <b> <i> ZaloPay App not install on this Device. </i> </b>");
                        } else {
                            Log.d("CODE_PAY_ERROR", "onError: On onPaymentError with paymentErrorCode: " + paymentErrorCode + " - zpTransToken: " + zpTransToken);
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Payment Fail")
                                    .setMessage(String.format("ZaloPayErrorCode: %s \nPaymentErrorCode: %s \nTransToken: %s", zaloPayErrorCode.getValue(), paymentErrorCode, zpTransToken))
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .setNegativeButton("Cancel", null).show();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ZaloPaySDK.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    public void openWebViewDemo(){
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
    }
}
