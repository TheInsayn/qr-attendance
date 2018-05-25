package mms.project.qr_attendance;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActivityGenerate extends AppCompatActivity {

    private static final int QR_SIZE = 300;

    EditText txtLVA;
    EditText txtDate;
    EditText txtAttendees;
    Button btnGenerate;
    Button btnPresent;
    Button btnCSV;
    ImageView imageView;
    ProgressBar progress;

    String lvaInstance;
    List<String> attendees;

    private final ValueEventListener valueListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String value = dataSnapshot.getValue(String.class);
            Log.d("Value is: ", value);
            if (value != null) {
                String[] valuePair = value.split("; ");
                if (valuePair[0].equals(lvaInstance) && valuePair.length > 1) {
                    attendees.add(valuePair[1]);
                    Log.d("attendees", Arrays.toString(attendees.toArray()));
                    progress.setProgress(attendees.size(), true);
                    btnPresent.setText(
                            String.format(getString(R.string.format_dialog_present),
                            attendees.size(), progress.getMax()));

                }
            }
        }

        @Override
        public void onCancelled(DatabaseError error) {
            Log.w("Failed to read value.", error.toException());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        txtLVA = findViewById(R.id.txtLVANr);
        txtDate = findViewById(R.id.txtDate);
        txtAttendees = findViewById(R.id.txtAttendees);
        btnGenerate = findViewById(R.id.btn_generate);
        btnPresent = findViewById(R.id.btn_present);
        btnCSV = findViewById(R.id.btn_csv);
        imageView = findViewById(R.id.imageView);
        progress = findViewById(R.id.progressBar);

        btnGenerate.setOnClickListener(v -> generateCode());
        btnCSV.setOnClickListener(v -> generateCSV());
        btnPresent.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Present:")
                    .setMessage(TextUtils.join("\n", attendees))
                    .show();
        });

        attendees = new ArrayList<>();
    }

    private boolean generateCode() {
//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
//        }
        if (validData()) {
            String lva = txtLVA.getText().toString();
            String date = txtDate.getText().toString();
            int expectedAttendees = Integer.valueOf(txtAttendees.getText().toString());
            String qrString = lva + "-" + date;
            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bm = writer.encode(qrString, BarcodeFormat.QR_CODE, QR_SIZE, QR_SIZE);
                Bitmap bmp = Bitmap.createBitmap(QR_SIZE, QR_SIZE, Bitmap.Config.RGB_565);
                for (int x = 0; x < QR_SIZE; x++) {
                    for (int y = 0; y < QR_SIZE; y++) {
                        bmp.setPixel(x, y, bm.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
                imageView.setImageBitmap(bmp);
            } catch (WriterException e) {
                e.printStackTrace();
            }
            this.lvaInstance = qrString;
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference();
            ref.setValue("");
            ref.addValueEventListener(valueListener);
            progress.setMax(expectedAttendees);
            return true;
        }
        return false;
    }

    private boolean validData() {
        boolean valid = true;
        if (!txtLVA.getText().toString().matches("^[0-9]{3}\\.[0-9]{3}")) {
            txtLVA.setError("invalid LVANr");
            valid = false;
        }
        if (!txtDate.getText().toString().matches("[0-9]{2}\\.[0-9]{2}.[0-9]{4}")) {
            txtDate.setError("invalid Date");
            valid = false;
        }
        if (!txtAttendees.getText().toString().matches("[0-9]*")) {
            txtAttendees.setError("invalid amount of expected attendees");
            valid = false;
        }
        return valid;
    }

    private boolean generateCSV() {
        Snackbar.make(btnGenerate, "Generating CSV of attendees", Snackbar.LENGTH_SHORT).show();
        //TODO: generate csv-file out of list of successfully scanned students
        return true;
    }
}
