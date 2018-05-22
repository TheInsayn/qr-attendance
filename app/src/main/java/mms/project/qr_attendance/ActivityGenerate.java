package mms.project.qr_attendance;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ActivityGenerate extends AppCompatActivity {

    private static final int QR_SIZE = 300;

    EditText txtLVA;
    EditText txtDate;
    EditText txtAttendees;
    Button btnGenerate;
    Button btnRefresh;
    Button btnCSV;
    ImageView imageView;
    ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate);
        txtLVA = findViewById(R.id.txtLVANr);
        txtDate = findViewById(R.id.txtDate);
        txtAttendees = findViewById(R.id.txtAttendees);
        btnGenerate = findViewById(R.id.btn_generate);
        btnRefresh = findViewById(R.id.btn_refresh);
        btnCSV = findViewById(R.id.btn_csv);
        imageView = findViewById(R.id.imageView);
        progress = findViewById(R.id.progressBar);

        btnGenerate.setOnClickListener(v -> generateCode());
        btnRefresh.setOnClickListener(v -> refreshAttendees());
        btnCSV.setOnClickListener(v -> generateCSV());
    }

    private boolean generateCode() {
//        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
//        }
        if (validData()) {
            String lva = txtLVA.getText().toString();
            String date = txtDate.getText().toString();
            String qrString = lva + "; " + date;
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

    private boolean refreshAttendees() {
        Snackbar.make(btnGenerate, "Refreshing amount of scans", Snackbar.LENGTH_SHORT).show();
        //TODO: refresh amount of scans of QR code, save MatrNrs to list
        return true;
    }

    private boolean generateCSV() {
        Snackbar.make(btnGenerate, "Generating CSV of attendees", Snackbar.LENGTH_SHORT).show();
        //TODO: generate csv-file out of list of successfully scanned students
        return true;
    }
}
