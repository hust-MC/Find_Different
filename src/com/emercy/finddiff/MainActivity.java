package com.emercy.finddiff;

import java.io.IOException;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class MainActivity extends Activity
{
	static ImageView imageView;
	static Button takePic_bt;

	private ListView listView;
	private SeekBar seekbar;
	private EditText inputThreshold;

	private boolean isMenuVisible = false;

	Animation menuVisible, menuInvisible;

	GetScreen getScreen;

	public static int whichToDisplay;
	public static int thresholdValue = 240;

	private void init_widget()
	{
		// 初始化
		imageView = (ImageView) findViewById(R.id.preview);
		listView = (ListView) findViewById(R.id.list_view);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		inputThreshold = (EditText) findViewById(R.id.input_threshold);

		// 处理ListView
		ArrayAdapter<?> menuAdapter = new ArrayAdapter<String>(this,
				R.layout.list, getResources()
						.getStringArray(R.array.menu_array));
		listView.setAdapter(menuAdapter);
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				if (position == 3)			// 灰度图则打开进度条
				{
					seekbar.setVisibility(View.VISIBLE);
					inputThreshold.setVisibility(View.VISIBLE);
				}
				else
				{
					seekbar.setVisibility(View.GONE);
					inputThreshold.setVisibility(View.GONE);
				}
				whichToDisplay = position;				// 切换算法
			}
		});

		// 处理SeekBar
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			@Override
			public void onStopTrackingTouch(SeekBar arg0)
			{
				thresholdValue = arg0.getProgress() > 255 ? 255 : arg0
						.getProgress();
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0)
			{

			}

			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2)
			{
				inputThreshold.setText(arg0.getProgress() + "");
			}
		});

		// 处理EditText
		inputThreshold.setOnEditorActionListener(new OnEditorActionListener()
		{

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event)
			{
				seekbar.setProgress(Integer.parseInt(inputThreshold.getText()
						.toString()));
				return false;
			}
		});
	}

	/*
	 * 初始化动画效果
	 */
	private void init_animation()
	{
		menuInvisible = AnimationUtils.loadAnimation(this,
				R.anim.listview_invisible);
		menuVisible = AnimationUtils.loadAnimation(this,
				R.anim.listview_visible);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		init_widget();
		init_animation();

		try
		{
			getScreen = new GetScreen(this);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public static void setImageView(Bitmap bm)
	{
		imageView.setImageBitmap(bm);
		imageView.setScaleType(ScaleType.CENTER_CROP);
	}

	public void onClick_menu(View view)
	{
		if (isMenuVisible)
		{
			listView.startAnimation(menuInvisible);
			listView.setVisibility(View.GONE);
			isMenuVisible = false;
		}
		else
		{
			listView.startAnimation(menuVisible);
			listView.setVisibility(View.VISIBLE);
			isMenuVisible = true;
		}
	}

	public void onClick_takePic(View view) throws IOException
	{
		getScreen.takePic();
	}

	@Override
	protected void onDestroy()
	{
		getScreen.stopCamera();
		super.onDestroy();
	}
}
