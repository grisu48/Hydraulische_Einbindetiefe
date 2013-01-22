package ac.at.uibk.geotechnik;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class implements the calculations and result display of the app
 * 
 * @author phoenix
 * 
 */
public class Calculation extends Activity {

	/** Value of H */
	private double h;
	/** Value of b */
	private double b;

	/** Value of T_K_Mod according to equation 25b of the appended script */
	private double t_k_mod = 0;

	/** Display value of H */
	private TextView txtH = null;
	/** Display value of B */
	private TextView txtB = null;
	/** Display value of T */
	private TextView txtT = null;
	/** Image of the resulting picture */
	private ImageView imgDraft = null;

	/** Bitmap of the current draft */
	private Bitmap bitmap = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calculation);
		getValues();
		initComponents();

		txtT.setText("T = " + String.format("%.2f", t_k_mod) + " m");
		paintDraft();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_display, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			this.finish();
			return true;
		case R.id.menu_export:
			doExport();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Show export dialog for the given bitmap
	 */
	private void doExport() {
		ExportDialog.showExport(bitmap, this);
	}

	/** Get values out of the intent */
	private void getValues() {
		final Intent intent = getIntent();
		b = intent.getDoubleExtra("B", 0.0);
		h = intent.getDoubleExtra("H", 0.0);

		/* ==== Do calculations ==== */
		// Constants given in script
		final double c1 = 0.165;
		final double c2 = 0.141;
		final double i_zul = 0.804;
		final double b_over_h;
		if (b/h > 7.0) {
			b_over_h = 7.0;
			b = 7 * h;
		} else {
			b_over_h = b/h;
			
		}

		final double A = c1 + c2 * b_over_h;
		t_k_mod = (h / i_zul - b / (8 * A)) / (1 + b_over_h / (8 * A));
	}

	/**
	 * Shows up a toast message with long duration
	 * 
	 * @param message
	 *            to be shown up
	 */
	@SuppressWarnings("unused")
	private void toast(CharSequence message) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_LONG;
		final Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	/**
	 * Shows up a toast message with short duration
	 * 
	 * @param message
	 *            to be shown up
	 */
	@SuppressWarnings("unused")
	private void toastShort(CharSequence message) {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;
		final Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

	/**
	 * Initialise all components. Should be once called in
	 * {@link #onCreate(Bundle)}
	 */
	private void initComponents() {
		txtH = (TextView) this.findViewById(R.id.txtH);
		txtB = (TextView) this.findViewById(R.id.txtB);
		txtT = (TextView) this.findViewById(R.id.txtT);
		imgDraft = (ImageView) this.findViewById(R.id.imgDraft);

		txtH.setText("H = " + String.format("%.2f", h) + " m");
		txtB.setText("B = " + String.format("%.2f", b) + " m");
		txtT.setText("T = ? m");
	}

	/** Paints the draft picture */
	private void paintDraft() {
		final int BACKGROUND = Color.LTGRAY;
		final float WIDTH = 400;
		final float HEIGHT = 400;
		final float TOP = HEIGHT * 1 / 4;
		final float MARGIN = WIDTH * 1 / 8;
		final float D = HEIGHT / 8;
		final float REAL_HEIGHT = HEIGHT - TOP - D;
		final float BAR_SIZE = 5;

		final float H;
		final float B;
		final float T;
		{
			final float ht = (float) (h + t_k_mod);
			H = (float) (REAL_HEIGHT * h / ht);
			T = (float) (REAL_HEIGHT * t_k_mod / ht);
			B = WIDTH - 2 * MARGIN;
		}

		bitmap = Bitmap.createBitmap((int) WIDTH, (int) HEIGHT, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(bitmap);

		imgDraft.setBackgroundColor(BACKGROUND);
		imgDraft.setImageBitmap(bitmap);

		final Paint pntEarth = new Paint();
		pntEarth.setColor(Color.rgb(255, 128, 0));
		final Paint pntBackground = new Paint();
		pntBackground.setColor(BACKGROUND);
		final Paint pntBackgroundFilled = new Paint();
		pntBackgroundFilled.setColor(Color.BLACK);
		pntBackgroundFilled.setStrokeWidth(1);
		pntBackgroundFilled.setStyle(Paint.Style.FILL_AND_STROKE);
		final Paint pntArrow = new Paint();
		pntArrow.setColor(Color.BLUE);

		// Areas without arrows
		canvas.drawRect(0, TOP, WIDTH, HEIGHT, pntEarth);
		canvas.drawRect(MARGIN, TOP, WIDTH - MARGIN, HEIGHT - D, pntBackground);
		canvas.drawRect(MARGIN, TOP + H, WIDTH - MARGIN, HEIGHT - D, pntEarth);
		canvas.drawRect(MARGIN - BAR_SIZE, TOP, MARGIN, HEIGHT - D, pntBackgroundFilled);
		canvas.drawRect(WIDTH - MARGIN, TOP, WIDTH - MARGIN + BAR_SIZE, HEIGHT - D, pntBackgroundFilled);

		// Draw arrows
		pntArrow.setColor(Color.BLUE);
		drawArrowBidirectional(canvas, 2 * MARGIN, TOP, 0, H, pntArrow);
		pntArrow.setColor(Color.RED);
		drawArrowBidirectional(canvas, 2 * MARGIN, TOP + H, 0, T, pntArrow);
		pntArrow.setColor(Color.MAGENTA);
		drawArrowBidirectional(canvas, MARGIN, TOP - MARGIN * 0.5F, B, 0, pntArrow);

		final Paint pntText = new Paint();
		final float TEXT_SIZE = 25;
		pntText.setColor(Color.BLUE);
		pntText.setTextSize(TEXT_SIZE);
		canvas.drawText("H = " + String.format("%.2f", h) + " m", 2.5F * MARGIN, TOP + H / 2.0F + TEXT_SIZE / 2.0F, pntText);
		pntText.setColor(Color.RED);
		canvas.drawText("T = " + String.format("%.2f", t_k_mod) + " m", 2.5F * MARGIN, TOP + H + T / 2.0F + TEXT_SIZE / 2.0F, pntText);

		final String textB = "B = " + String.format("%.2f", b) + " m";
		pntText.setColor(Color.MAGENTA);
		canvas.drawText(textB, WIDTH / 2.0F - TEXT_SIZE / 2.0F - pntText.measureText(textB) / 2.0F,
				TOP - -MARGIN * 0.5F - TEXT_SIZE * 2.2F, pntText);

	}

	/**
	 * Draw an arrow with two endpoints into the canvas with the default size of
	 * 15
	 * 
	 * @param canvas
	 *            the arrow is drawn to
	 * @param startX
	 *            starting x position
	 * @param startY
	 *            starting y position
	 * @param width
	 *            of the arrow
	 * @param height
	 *            of the arrow
	 * @param paint
	 *            to be used for drawing
	 */
	private void drawArrowBidirectional(final Canvas canvas, final float startX, final float startY, final float width, final float height,
			final Paint paint) {
		drawArrowBidirectional(canvas, startX, startY, width, height, paint, 15);
	}

	/**
	 * Draw an arrow with two endpoints into the canvas
	 * 
	 * @param canvas
	 *            the arrow is drawn to
	 * @param startX
	 *            starting x position
	 * @param startY
	 *            starting y position
	 * @param width
	 *            of the arrow
	 * @param height
	 *            of the arrow
	 * @param paint
	 *            to be used for drawing
	 * @param size
	 *            of the arrow
	 */
	private void drawArrowBidirectional(final Canvas canvas, final float startX, final float startY, final float width, final float height,
			final Paint paint, final float size) {
		drawArrow(canvas, startX, startY, width, height, paint, size);
		drawArrow(canvas, startX + width, startY + height, -width, -height, paint, size);
	}

	/**
	 * Draw an arrow into the canvas with the default size
	 * 
	 * @param canvas
	 *            the arrow is drawn to
	 * @param startX
	 *            starting x position
	 * @param startY
	 *            starting y position
	 * @param width
	 *            of the arrow
	 * @param height
	 *            of the arrow
	 * @param paint
	 *            to be used for drawing
	 */
	@SuppressWarnings("unused")
	private void drawArrow(final Canvas canvas, final float startX, final float startY, final float width, final float height,
			final Paint paint) {
		drawArrow(canvas, startX, startY, width, height, paint, 15);
	}

	/**
	 * Draw an arrow into the canvas
	 * 
	 * @param canvas
	 *            the arrow is drawn to
	 * @param startX
	 *            starting x position
	 * @param startY
	 *            starting y position
	 * @param width
	 *            of the arrow
	 * @param height
	 *            of the arrow
	 * @param paint
	 *            to be used for drawing
	 * @param size
	 *            of the arrow
	 */
	private void drawArrow(final Canvas canvas, final float startX, final float startY, final float width, final float height,
			final Paint paint, final float size) {

		final float endX = startX + width;
		final float endY = startY + height;

		canvas.drawLine(startX, startY, endX, endY, paint);

		// TODO: Draw arrows
		final float sizeX = size;
		final float sizeY = size * 1.2F;
		if (width == 0) {
			if (height > 0) {
				canvas.drawLine(endX, endY, endX + sizeX, endY - sizeY, paint);
				canvas.drawLine(endX, endY, endX - sizeX, endY - sizeY, paint);
			} else {
				canvas.drawLine(endX, endY, endX + sizeX, endY + sizeY, paint);
				canvas.drawLine(endX, endY, endX - sizeX, endY + sizeY, paint);
			}
		} else if (height == 0) {
			if (width > 0) {
				canvas.drawLine(endX, endY, endX - sizeY, endY + sizeX, paint);
				canvas.drawLine(endX, endY, endX - sizeY, endY - sizeX, paint);
			} else {
				canvas.drawLine(endX, endY, endX + sizeY, endY + sizeX, paint);
				canvas.drawLine(endX, endY, endX + sizeY, endY - sizeX, paint);
			}
		} else {
			// XXX: Currently we only support straight arrows
			System.err.println("Currently only straight arrorws are supported");
		}
	}
}
