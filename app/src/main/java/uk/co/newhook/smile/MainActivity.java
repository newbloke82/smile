package uk.co.newhook.smile;

import org.json.JSONObject;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Build;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hiai.vision.barcode.BarcodeDetector;
import com.huawei.hiai.vision.common.VisionBase;
//import com.huawei.hiai.vision.visionkit.barcode.Barcode;
import com.huawei.hiai.vision.visionkit.common.Frame;
import com.huawei.hiai.vision.common.ConnectionCallback;

import java.io.File;
import java.util.Date;
import java.io.IOException;

import static android.content.Intent.URI_INTENT_SCHEME;
//import java.util.List;



public class MainActivity extends AppCompatActivity {
    private static final int PHOTO_REQUEST_GALLERY = 2;
    private ImageView barcodeImage;
    private Button selectFileButton;
    private Button startButton;
    private TextView jsonResTextView;
    private TextView titleTextView;
    private boolean isImageSet;
    private String imagePath;
    private String imageSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,}, 5);
        }

        setContentView(R.layout.activity_barcode_demo);

        titleTextView = (TextView) findViewById(R.id.title);
        selectFileButton = (Button) findViewById(R.id.filebutton);
        startButton = (Button) findViewById(R.id.startbutton);
        barcodeImage = (ImageView) findViewById(R.id.barcodeIV);
        jsonResTextView = (TextView) findViewById(R.id.jsonResTV);

        jsonResTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        startButton.setText("2. Detect Barcode");
        selectFileButton.setText("1. Select a image contains a barcode in it");
        titleTextView.setText("BarcodeDemo: ");
        jsonResTextView.setText("Select input file first,\nthen detect barcode.");

        selectFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                int requestCode = PHOTO_REQUEST_GALLERY;
                startActivityForResult(intent, requestCode);
            }
        });


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isImageSet) {
                    Toast.makeText(getApplicationContext(), "Select a image first!",
                            Toast.LENGTH_SHORT).show();
                } else {

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            VisionBase.init(getApplicationContext(), ConnectManager.getInstance().getmConnectionCallback());   //try to start AIEngine
                            if (!ConnectManager.getInstance().isConnected()) {  //wait for AIEngine service
                                ConnectManager.getInstance().waitConnect();
                            }

                            File file = new File(imagePath);
                            final Frame frame = new Frame();
                            if (file.isFile()) {
                                frame.setFilePath(file.getAbsolutePath());  //set the target image to frame
                                BarcodeDetector detector = new BarcodeDetector(getApplicationContext());    //get the BarcodeDetector
                                Date tempTimeEnd, tempTimeStart;
                                tempTimeStart = new Date();
                                JSONObject jsonRes = detector.detect(frame, null);  //detect barcode from the given image
                         requestPermissions();       tempTimeEnd = new Date();
                                if (jsonRes != null) {
                                    jsonResTextView.setText("Input image file:\n" + imagePath + "\n\nJson Result:\n" + jsonRes.toString() + "\n\nUse Time:\n" + (tempTimeEnd.getTime() - tempTimeStart.getTime()) + "ms;\n\nImage Size:\n" + imageSize);
                                } else {
                                    jsonResTextView.setText("No barcode detected!");
                                }

                            }
                        }
                    });
                    thread.start();
                }
            }
        });
        requestPermissions();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                return;
            }

            Uri selectedImage = data.getData();
            // Get the intent that started this activity
            //Intent intent = getIntent();
            //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //Uri imageUri = Uri.parse(intent.toUri(URI_INTENT_SCHEME));
            String[] pathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, pathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(pathColumn[0]);
            imagePath = cursor.getString(columnIndex);
            jsonResTextView.setText(imagePath);
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                barcodeImage.setImageBitmap(bitmap);
                imageSize = bitmap.getWidth() + " * " + bitmap.getHeight() + ".";
                isImageSet = true;
            } catch(IOException ioEx) {
                ioEx.printStackTrace(); // or what ever you want to do with it
            }
            //Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            //barcodeImage.setImageBitmap(bitmap);
            //imageSize = bitmap.getWidth() + " * " + bitmap.getHeight() + ".";
            //isImageSet = true;
        }
    }

    private void requestPermissions(){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA},0x0010);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}