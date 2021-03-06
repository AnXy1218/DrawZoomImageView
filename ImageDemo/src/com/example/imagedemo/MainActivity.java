package com.example.imagedemo;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.example.imagedemo.DrawZoomImageView.ModeEnum;

public class MainActivity extends Activity implements OnClickListener {

	private DrawZoomImageView iv_photo;
	private RelativeLayout rl_contrl;
	private ImageView iv_result;
	private LinearLayout ll_color;
	private TextView tv_red, tv_green, tv_blue, tv_yellow;
	private Button btn_ty, btn_xp, btn_open, btn_revoke, btn_recovery,
			btn_finish;
	private boolean isBack = true;
	private SeekBar seekBar; // 控制画笔宽度

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		iv_photo = (DrawZoomImageView) findViewById(R.id.iv_photo);
		iv_result = (ImageView) findViewById(R.id.iv_result);
		rl_contrl = (RelativeLayout) findViewById(R.id.rl_contrl);
		rl_contrl.setVisibility(View.VISIBLE);
		iv_result.setVisibility(View.GONE);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		ll_color = (LinearLayout) findViewById(R.id.ll_color);
		iv_photo.setMode(ModeEnum.TY);

		seekBar.setMax(iv_photo.lineStrokeWidthMax);
		seekBar.setProgress(iv_photo.getTyStrokeWidth());
		iv_photo.setTyStrokeWidth(seekBar.getProgress());

		ll_color.setVisibility(View.VISIBLE);
		tv_red = (TextView) findViewById(R.id.tv_red);
		tv_green = (TextView) findViewById(R.id.tv_green);
		tv_blue = (TextView) findViewById(R.id.tv_blue);
		tv_yellow = (TextView) findViewById(R.id.tv_yellow);

		btn_ty = (Button) findViewById(R.id.btn_ty);
		btn_xp = (Button) findViewById(R.id.btn_xp);
		btn_open = (Button) findViewById(R.id.btn_open);
		btn_revoke = (Button) findViewById(R.id.btn_revoke);
		btn_recovery = (Button) findViewById(R.id.btn_recovery);
		btn_finish = (Button) findViewById(R.id.btn_finish);

		tv_red.setOnClickListener(this);
		tv_green.setOnClickListener(this);
		tv_blue.setOnClickListener(this);
		tv_yellow.setOnClickListener(this);
		btn_ty.setOnClickListener(this);
		btn_xp.setOnClickListener(this);
		btn_open.setOnClickListener(this);
		btn_revoke.setOnClickListener(this);
		btn_recovery.setOnClickListener(this);
		btn_finish.setOnClickListener(this);

		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (iv_photo.getMode() == ModeEnum.TY) {
					iv_photo.setTyStrokeWidth(seekBar.getProgress());
				} else if (iv_photo.getMode() == ModeEnum.XP) {
					iv_photo.setXpStrokeWidth(seekBar.getProgress());
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) {
			Uri uri = data.getData();
			try {
				String[] proj = {MediaStore.Images.Media.DATA};
	            //好像是android多媒体数据库的封装接口，具体的看Android文档
	            Cursor cursor = managedQuery(uri, proj, null, null, null); 
	            //按我个人理解 这个是获得用户选择的图片的索引值
	            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	            //将光标移至开头 ，这个很重要，不小心很容易引起越界
	            cursor.moveToFirst();
	            //最后根据索引值获取图片路径
	            String path = cursor.getString(column_index);
				
				Bitmap bitmap = readBitmapAutoSize(path, iv_photo.getWidth(), iv_photo.getHeight());
				iv_photo.setImageBitmap(bitmap);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_open: // 打开图片
			Intent intent2 = new Intent(Intent.ACTION_GET_CONTENT);
			intent2.setType("image/*");
			startActivityForResult(intent2, 2);
			break;
		case R.id.btn_ty: // 模式为涂鸦
			iv_photo.setMode(ModeEnum.TY);
			ll_color.setVisibility(View.VISIBLE);
			seekBar.setMax(iv_photo.lineStrokeWidthMax); // 设置最大
			seekBar.setProgress(iv_photo.getTyStrokeWidth());
			break;
		case R.id.btn_xp: // 模式为橡皮
			iv_photo.setMode(ModeEnum.XP);
			ll_color.setVisibility(View.GONE);
			seekBar.setMax(iv_photo.xpStrokeWidthMax); // 设置最大
			seekBar.setProgress(iv_photo.getXpStrokeWidth());
			break;
		case R.id.btn_revoke: // 撤销
			iv_photo.revoke();
			break;
		case R.id.btn_recovery: // 恢复
			iv_photo.recovery();
			break;
		case R.id.btn_finish: // 查看编辑好的图片
			rl_contrl.setVisibility(View.GONE);
			iv_result.setVisibility(View.VISIBLE);
			Bitmap bitmap = iv_photo.getImageBitmap();
			iv_result.setImageBitmap(bitmap);
			isBack = false;
			break;
		case R.id.tv_red:
			iv_photo.setTyColor(0xFFFF0000);
			break;
		case R.id.tv_green:
			iv_photo.setTyColor(0xFF00FF00);
			break;
		case R.id.tv_blue:
			iv_photo.setTyColor(0xFF0000FF);
			break;
		case R.id.tv_yellow:
			iv_photo.setTyColor(0xFFFFFF00);
			break;
		default:
			break;
		}
	}

	public Bitmap readBitmapAutoSize(String filePath, int outWidth, int outHeight) {
		// outWidth和outHeight是目标图片的最大宽度和高度，用作限制
		FileInputStream fs = null;
		BufferedInputStream bs = null;
		try {
			fs = new FileInputStream(filePath);
			bs = new BufferedInputStream(fs);
			BitmapFactory.Options options = setBitmapOption(filePath, outWidth,
					outHeight);
			return BitmapFactory.decodeStream(bs, null, options);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				bs.close();
				fs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private BitmapFactory.Options setBitmapOption(String file, int width, int height) {
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		// 设置只是解码图片的边距，此操作目的是度量图片的实际宽度和高度
		BitmapFactory.decodeFile(file, opt);

		int outWidth = opt.outWidth; // 获得图片的实际高和宽
		int outHeight = opt.outHeight;
		opt.inDither = false;
		opt.inPreferredConfig = Bitmap.Config.RGB_565;
		// 设置加载图片的颜色数为16bit，默认是RGB_8888，表示24bit颜色和透明通道，但一般用不上
		opt.inSampleSize = 1;
		// 设置缩放比,1表示原比例，2表示原来的四分之一....
		// 计算缩放比
		if (outWidth != 0 && outHeight != 0 && width != 0 && height != 0) {
			int sampleSize = (outWidth / width + outHeight / height) / 2;
			opt.inSampleSize = sampleSize;
		}
		opt.inJustDecodeBounds = false;// 最后把标志复原
		return opt;
	}

	@Override
	public void onBackPressed() {
		if (isBack) {
			super.onBackPressed();
		} else {
			iv_result.setVisibility(View.GONE);
			rl_contrl.setVisibility(View.VISIBLE);
			isBack = true;
		}

	}
}
