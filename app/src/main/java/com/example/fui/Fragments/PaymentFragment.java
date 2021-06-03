package com.example.fui.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.fui.R;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentFragment extends Fragment implements PaymentResultListener {

    Button btnPay;
    private static final String PREMIUM_AMOUNT = "50";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment, container, false);
        init(view);
        final int amount = Math.round(Float.parseFloat(PREMIUM_AMOUNT)*100);
        btnPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_iA9y02f2QQhBFv");
                checkout.setImage(R.drawable.recipeify);
                JSONObject obj = new JSONObject();
                try {
                    obj.put("name","Recipeify organisation");
                    obj.put("description", "Payment for upgrading to PREMIUM user");
                    obj.put("theme.color", "#1A242F");
                    obj.put("currency", "INR");
                    obj.put("amount", amount);
                    obj.put("prefill.contact","9876543210");
                    obj.put("prefill.email","ekal24767@gmail.com");
                    checkout.open(getActivity(), obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        return view;

    }

    private void init(View view)
    {
        btnPay = view.findViewById(R.id.btnPay);
    }

    @Override
    public void onPaymentSuccess(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Payment ID");
        builder.setMessage(s);
        builder.show();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(getContext(), "payment failed due to :\n"+s, Toast.LENGTH_LONG).show();
    }
}
