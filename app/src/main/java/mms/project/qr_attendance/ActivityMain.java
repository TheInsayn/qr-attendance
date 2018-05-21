package mms.project.qr_attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class ActivityMain extends AppCompatActivity {
    private static final int REQUEST_LOGIN = 1234;
    private static final int REQUEST_PICTURE = 5678;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private FloatingActionButton fab;
    private static String matrNr = null;
    private static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (fab == null) {
            fab = findViewById(R.id.fab);
            fab.setOnClickListener(v -> startLoginActivity());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null && bundle.containsKey("MatrNr")) {
                        matrNr = bundle.getString("MatrNr");
                        name = bundle.getString("Name");
                        setAppState(true);
                    }
                }
                break;
            case REQUEST_PICTURE:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    processPicture(imageBitmap);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void processPicture(Bitmap imageBitmap) {
        //TODO: scan image for qr code
        Snackbar.make(findViewById(R.id.text_view), "image recieved, scan it now", Snackbar.LENGTH_LONG).show();
    }

    private void setAppState(boolean logged_in) {
        TextView txt = findViewById(R.id.text_view);
        if (logged_in) {
            txt.setText(String.format("Hallo, %s\nMatrNr: %s", name, matrNr));
            fab.setImageResource(R.drawable.ic_scan);
            fab.setOnClickListener((v) -> takePicture());
        } else {
            txt.setText("logged out.\nto sign in again, use login-button.");
            fab.setImageResource(R.drawable.ic_login);
            matrNr = null;
            name = null;
            fab.setOnClickListener((v) -> startLoginActivity());
        }
        invalidateOptionsMenu();
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(getApplicationContext(), ActivityLogin.class);
        startActivityForResult(loginIntent, REQUEST_LOGIN);
    }

    private void takePicture() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_PICTURE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_logout).setVisible(matrNr != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                TextView txt = findViewById(R.id.text_view);
                Snackbar.make(txt, "Not yet implemented", Snackbar.LENGTH_LONG).show();
                //TODO: remove (only used for faster testing)
                name = "Username";
                matrNr = "k12345678";
                setAppState(true);
                // TODO: till here
                return true;
            case R.id.action_logout:
                setAppState(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
