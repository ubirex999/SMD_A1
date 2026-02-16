package com.example.smd_assigment_1;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.Toast;


public class buy_page extends AppCompatActivity {
    Button confirm , Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buy_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        init();

        confirm.setOnClickListener((v)->
        {
            String number = "03104695189";
            String message = "Your Purchase has been successfull.";

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("sms" + number));
            i.putExtra("sms_body" , message);

            startActivity(i);

        });

        Cancel.setOnClickListener((v)->
        {
            finish();

        });




    }


    private void init()
    {
        confirm = findViewById(R.id.btnConfirm);
        Cancel = findViewById(R.id.btnCancel);
    }


}