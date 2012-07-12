package com.github.pickncrop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;

public class MainActivity extends Activity {
	private ImageView imageView;
	private ViewManager viewManager;
	private final int outputSize = 100;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Display display = getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		int height = display.getHeight();
		int size = width < height ? width : height;

		imageView = (ImageView) findViewById(R.id.imageViewCrop);
		imageView.getLayoutParams().width = size - 25;
		imageView.getLayoutParams().height = size - 25;
		viewManager = (ViewManager) imageView.getParent();

		if (getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH)) {
			createZoomControls();
		}

		imageView.setOnTouchListener(new OnTouchListener() {
			float initX;
			float initY;
			float scale = 1.0f;
			float initDistance;
			float currentDistance;
			boolean isMultitouch;
			Matrix matrix = new Matrix();

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					initX = event.getX();
					initY = event.getY();
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					isMultitouch = true;
					initDistance = (float) Math.sqrt(Math.pow(event.getX(0)
							- event.getX(1), 2)
							+ Math.pow(event.getY(0) - event.getY(1), 2));
					break;
				case MotionEvent.ACTION_MOVE:
					if (isMultitouch) {
						currentDistance = (float) Math.sqrt(Math.pow(
								event.getX(0) - event.getX(1), 2)
								+ Math.pow(event.getY(0) - event.getY(1), 2));
						scale = currentDistance / initDistance;
						matrix.setScale(scale, scale);
						imageView.setImageMatrix(matrix);
					} else {
						imageView.scrollBy((int) (initX - event.getX()),
								(int) (initY - event.getY()));
						initX = event.getX();
						initY = event.getY();
					}
					break;
				case MotionEvent.ACTION_UP:
					isMultitouch = false;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					isMultitouch = false;
					break;
				}
				return true;
			}
		});
	}

	public void createZoomControls() {
		ZoomButtonsController zoomButtonsController = new ZoomButtonsController(
				imageView);
		zoomButtonsController.setVisible(true);
		zoomButtonsController.setAutoDismissed(false);
		zoomButtonsController.setOnZoomListener(new OnZoomListener() {
			float scale = 1.0f;
			Matrix matrix = new Matrix();

			@Override
			public void onZoom(boolean zoomIn) {
				if (zoomIn) {
					scale += 0.05f;
					matrix.setScale(scale, scale);
					imageView.setImageMatrix(matrix);
				} else {
					scale -= 0.05f;
					matrix.setScale(scale, scale);
					imageView.setImageMatrix(matrix);
				}
			}

			@Override
			public void onVisibilityChanged(boolean visible) {
			}
		});
		RelativeLayout.LayoutParams zoomLayoutParams = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		zoomLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		zoomLayoutParams.addRule(RelativeLayout.BELOW, R.id.imageViewCrop);
		viewManager.addView(zoomButtonsController.getContainer(),
				zoomLayoutParams);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Toast.makeText(this, R.string.info, 5).show();
		return super.onMenuItemSelected(featureId, item);
	}

	public void buttonPickClick(View view) {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, 123);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (resultCode) {
		case RESULT_OK:
			Uri targetUri = data.getData();
			imageView.setImageURI(targetUri);
			imageView.setScaleType(ScaleType.MATRIX);
			break;
		}
	}

	public void buttonCropClick(View view) throws IOException {
		imageView.setDrawingCacheEnabled(true);
		imageView.buildDrawingCache(true);
		File imageFile = new File(Environment.getExternalStorageDirectory(),
				"Pictures/" + UUID.randomUUID().toString() + ".jpg");
		FileOutputStream fileOutputStream = new FileOutputStream(imageFile);
		Bitmap.createScaledBitmap(imageView.getDrawingCache(true), outputSize,
				outputSize, false).compress(CompressFormat.JPEG, 100,
				fileOutputStream);
		fileOutputStream.close();
		imageView.setDrawingCacheEnabled(false);
	}
}
