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

import static mms.project.qr_attendance.ActivityLogin.KEY_MATRNR;
import static mms.project.qr_attendance.ActivityLogin.KEY_NAME;

public class ActivityMain extends AppCompatActivity {
    private static final int REQUEST_LOGIN = 1234;
    private static final int REQUEST_PICTURE = 5678;

    private static final int REQUEST_CAMERA = 100;
    private FloatingActionButton fab;
    private TextView txt;

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
        txt = findViewById(R.id.text_view);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null && bundle.containsKey(KEY_MATRNR)) {
                        matrNr = bundle.getString(KEY_MATRNR);
                        name = bundle.getString(KEY_NAME);
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

    private void setAppState(boolean logged_in) {
        if (logged_in) {
            txt.setText(String.format(getString(R.string.welcome_format), name, matrNr));
            fab.setImageResource(R.drawable.ic_scan);
            fab.setOnClickListener((v) -> takePicture());
        } else {
            txt.setText(R.string.logged_out);
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

    private void startGenerateActivity() {
        Intent generateIntent = new Intent(getApplicationContext(), ActivityGenerate.class);
        startActivity(generateIntent);
    }

    private void takePicture() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
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
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(txt, "camera permission granted", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(txt, "camera permission denied", Snackbar.LENGTH_SHORT).show();
            }

        }
    }

    private void processPicture(Bitmap imageBitmap) {
        //TODO: scan image for qr code
        Snackbar.make(txt, "image recieved, scan it now", Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_logout).setVisible(matrNr != null);
        menu.findItem(R.id.action_generate).setVisible(matrNr != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
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
            case R.id.action_generate:
                startGenerateActivity();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
