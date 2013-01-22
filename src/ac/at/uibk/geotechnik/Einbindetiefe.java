package ac.at.uibk.geotechnik;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Main app entrance point. This class implements the main display where the
 * user can input it's data
 * 
 * @author phoenix
 * 
 */
public class Einbindetiefe extends Activity {

	/* GUI Components */

	/** Input text field for B */
	private EditText edB = null;
	/** Input text field for H */
	private EditText edH = null;
	/** Hint for input if something went wrong */
	private TextView txtHint = null;
	/** Button to launch the calculation cycle */
	private Button btnCalculate = null;
	/** Indicating that the process is busy */
	private ProgressBar prgCalculate = null;
	/** Draft image */
	private ImageView imgDraft = null;

	/* END OF GUIT Components */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_input);
		initComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_einbindetiefe, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_exit:
			this.finish();
			return true;
		case R.id.menu_about:
			this.showAbout();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Shows the about dialog
	 */
	private void showAbout() {
		String version = "?";
		try {
			final PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
		}
		
		
		final String aboutMessage = "Version " + version + ", 2013; Licensed under the GPLv3\n\nWritten by: Felix Niederwanger\nIm Auftrag der Universität Innsbruck\n\n" +
		"See http://gplv3.fsf.org/\nhttps://github.com/grisu48/Hydraulische_Einbindetiefe";

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.about);
		builder.setMessage(aboutMessage);
		builder.setPositiveButton(R.string.OK, null);
		AlertDialog dialog = builder.show();

		// Must call show() prior to fetching text view
		TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
		messageView.setGravity(Gravity.CENTER);
	}

	/**
	 * Initialise all components. Should be once called in
	 * {@link #onCreate(Bundle)}
	 */
	private synchronized void initComponents() {
		btnCalculate = (Button) this.findViewById(R.id.btnCalculate);
		edH = (EditText) this.findViewById(R.id.edH);
		edB = (EditText) this.findViewById(R.id.edB);
		prgCalculate = (ProgressBar) this.findViewById(R.id.prgCalculate);
		txtHint = (TextView) this.findViewById(R.id.txtInputHint);
		imgDraft = (ImageView) this.findViewById(R.id.imgDraft);

		btnCalculate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doCalculation();
			}
		});
	}

	/** Invokes the calculation process */
	private synchronized void doCalculation() {
		try {
			imgDraft.setVisibility(View.INVISIBLE);
			prgCalculate.setVisibility(View.VISIBLE);
			btnCalculate.setEnabled(false);
			final double h, b;

			h = Double.parseDouble(edH.getText().toString());
			b = Double.parseDouble(edB.getText().toString());

			Intent intent = new Intent(Einbindetiefe.this, Calculation.class);
			intent.putExtra("H", h);
			intent.putExtra("B", b);
			startActivity(intent);

		} catch (NumberFormatException ex) {
			txtHint.setText(R.string.IllegalNumberFormat);
		} finally {
			btnCalculate.setEnabled(true);
			imgDraft.setVisibility(View.VISIBLE);
			prgCalculate.setVisibility(View.INVISIBLE);
		}

	}
}
