package com.aai.camera2demo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class Camera2Activity extends AppCompatActivity implements View.OnClickListener {

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    /// display photo vertically
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private ImageView iv_show;
    private TextView tv_category, tv_name, tv_alias, tv_english_name;
    private ImageButton ib_takephoto_cancel;
    private Handler childHandler;
    private ImageReader mImageReader;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);
        initView();
    }

    private void initView() {

        iv_show = findViewById(R.id.iv_show_camera2_activity);
        tv_category = findViewById(R.id.tv_category);
        tv_name = findViewById(R.id.tv_name);
        tv_alias = findViewById(R.id.tv_alias);
        tv_english_name = findViewById(R.id.tv_english_name);

        iv_show.setVisibility(View.GONE);
        tv_category.setVisibility(View.GONE);
        tv_name.setVisibility(View.GONE);
        tv_alias.setVisibility(View.GONE);
        tv_english_name.setVisibility(View.GONE);

        ib_takephoto_cancel = findViewById(R.id.ib_takephoto_cancel);
        ib_takephoto_cancel.setImageResource(R.drawable.ic_photo_camera_black_24dp);
        ib_takephoto_cancel.setOnClickListener(this);
        // mSurfaceView
        mSurfaceView = findViewById(R.id.surface_view_camera2_activity);
//        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setKeepScreenOn(true);
        // mSurfaceView add callback
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // init camera2
                initCamera2();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Toast.makeText(Camera2Activity.this, "surfaceChanged", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // release camera resource
                if (null != mCameraDevice) {
                    mCameraDevice.close();
                    Camera2Activity.this.mCameraDevice = null;
                }
            }
        });
    }

    /**
     * init camera2
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initCamera2() {
        HandlerThread handlerThread = new HandlerThread("Camera2");
        handlerThread.start();

        childHandler = new Handler(handlerThread.getLooper());
        Handler mainHandler = new Handler(getMainLooper());

        String mCameraID = "" + CameraCharacteristics.LENS_FACING_FRONT;
        mImageReader = ImageReader.newInstance(1080, 1920, ImageFormat.JPEG, 1);

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                mCameraDevice.close();
//                mSurfaceView.setVisibility(View.GONE);
                iv_show.setVisibility(View.VISIBLE);
                // get photo bitmap
                Image image = reader.acquireNextImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bitmap != null) {
                    iv_show.setImageBitmap(bitmap);
                    tv_category.setText("害虫啊");
                    tv_name.setText("蚜虫");
                    tv_alias.setText("坏蚜虫");
                    tv_english_name.setText("bad aphid");
                    tv_category.setVisibility(View.VISIBLE);
                    tv_name.setVisibility(View.VISIBLE);
                    tv_alias.setVisibility(View.VISIBLE);
                    tv_english_name.setVisibility(View.VISIBLE);
                    ib_takephoto_cancel.setVisibility(View.GONE);
                }
            }
        }, mainHandler);

        // get camera manager
        CameraManager mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int grantResult = 0;
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, grantResult);
                return;
            }
            // open Camera
            assert mCameraManager != null;
            mCameraManager.openCamera(mCameraID, stateCallback, mainHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;
            // active preview
            takePreview();
        }

        @Override
        public void onDisconnected( @NonNull CameraDevice camera) {
            if (null != mCameraDevice) {
                Toast.makeText(Camera2Activity.this, "close camera onDisconnected", Toast.LENGTH_SHORT).show();
                mCameraDevice.close();
                Camera2Activity.this.mCameraDevice = null;
            }
        }

        @Override
        public void onError( @NonNull CameraDevice camera, int error) {
            Toast.makeText(Camera2Activity.this, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * takePreview
     */
    private void takePreview() {
        try {
            final CaptureRequest.Builder previewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            // let SurfaceView's surface as the target of CaptureRequest.Builder
            previewRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            // create CameraCaptureSession, this obj deals preview request and take picture request
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface(), mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured( @NonNull CameraCaptureSession session) {
                    if (null == mCameraDevice) return;

                    // when the camera ready, show preview
                    mCameraCaptureSession = session;
                    try {
                        // auto focus
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        // open flash
                        previewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        // show preview
                        CaptureRequest previewRequest = previewRequestBuilder.build();
                        mCameraCaptureSession.setRepeatingRequest(previewRequest, null, childHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed( @NonNull CameraCaptureSession session) {
                    Toast.makeText(Camera2Activity.this, "Failed Configure", Toast.LENGTH_SHORT).show();
                }
            }, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        takePicture();
    }

    private void takePicture() {
        if (mCameraDevice == null) return;
        final CaptureRequest.Builder captureRequestBuilder;
        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            // let imageReader's surface as the target of CaptureRequest.Builder
            captureRequestBuilder.addTarget(mImageReader.getSurface());
            // auto focus
            captureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            // get rotaion of phone
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            //
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            // take photo
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();
            mCameraCaptureSession.capture(mCaptureRequest, null, childHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
