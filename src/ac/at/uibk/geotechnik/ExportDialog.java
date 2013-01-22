package ac.at.uibk.geotechnik;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Dialog providing export functionality for a single bitmap. The bitmap is
 * created by {@link Calculation}.
 * 
 * @author phoenix
 * 
 */
public class ExportDialog extends Dialog {

	/** Default format if no extension is given */
	private static final CompressFormat defaultFormat = CompressFormat.PNG;
	
	/** PNG compression level */
	private static final int PNG_COMPRESSION_LEVEL = 90;
	
	/** Bitmap to be exported */
	private Bitmap exportBitmap = null;
	/** Current context */
	private final Context context;

	/** Filename text input */
	private EditText edFilename = null;
	/** Status */
	private TextView txtStatus = null;
	/** Export button */
	private Button btExport = null;
	/** Cancel button */
	private Button btCancel = null;

	private ExportDialog(Context context, final Bitmap bitmap) {
		super(context);
		this.context = context;
		this.exportBitmap = bitmap;
	}

	public synchronized static void showExport(final Bitmap bitmap, final Context context) {
		final ExportDialog instance = new ExportDialog(context, bitmap);
		instance.show();
	}

	@Override
	public void onCreate(Bundle savedInstance) {
		setContentView(R.layout.dialog_export);
		initComponents();
	}

	private void initComponents() {
		btCancel = (Button) findViewById(R.id.btnCancel);
		btExport = (Button) findViewById(R.id.btnExport);
		edFilename = (EditText) findViewById(R.id.edFilename);
		txtStatus = (TextView) findViewById(R.id.txtStatus);

		btExport.setEnabled(false);
		edFilename.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				ExportDialog.this.afterFilenameChanged();
			}
		});
		btCancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				ExportDialog.this.dismiss();
			}
		});
		btExport.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				doExport();
			}
		});

	}

	/** Called when the filename has been changed */
	private void afterFilenameChanged() {
		txtStatus.setText(R.string.inputFilename);
		final String filename = getFilename();
		if (filename == null || filename.length() == 0) {
			txtStatus.setText(R.string.empty_filename);
			btExport.setEnabled(false);
			return;
		}
		
		final File file = new File(filename);
		if (file.exists()) {
			txtStatus.setText(R.string.file_exists);
			btExport.setEnabled(false);
			return;
		}

		if(!hasFileExtension(filename)) {
			txtStatus.setText(R.string.use_default_bitmap_format);
		} else if(!hasValidFileExtension(filename)) {
			txtStatus.setText(R.string.illegal_fileending);
			btExport.setEnabled(false);
			return;
		} else {
			// Check if the original input had a valid file extension
			if(hasValidFileExtension(edFilename.getText().toString()))
				txtStatus.setText(R.string.use_default_bitmap_format);
			else
			txtStatus.setText(R.string.inputFilename);
		}

		btExport.setEnabled(true);
	}
	
	/**
	 * Check the given file for the format. If the filename is null or empty, null is returned
	 * @param filename to be checked
	 * @return the FileFormat of the given file
	 */
	private CompressFormat getFileformat(final String filename) {
		if(!hasValidFileExtension(filename)) return null;
		
		int index = filename.lastIndexOf(".");
		if (index < 0) return defaultFormat;
		if(index == filename.length()) return defaultFormat;
		
		final String extension = filename.substring(index+1);
		if(extension.equalsIgnoreCase("png")) return CompressFormat.PNG;
		else if(extension.equalsIgnoreCase("jpeg")) return CompressFormat.JPEG;
		else return null;
	}
	
	/**
	 * Creates the export directory if not exists
	 */
	private void createExportDirectory() {
		final String dest = Environment.getExternalStorageDirectory() + File.separator + "einbindetiefe";
		final File dir = new File(dest);
		if(!dir.exists()) dir.mkdir();
	}
	
	/**
	 * @return the filename of the inputfield with the right extension
	 */
	private String getFilename() {
		String filename = edFilename.getText().toString();
		if(filename.length() <= 0 || filename.startsWith(".")) return "";
		
		// Check if absolute path otherwise add SD-Card directory
		if(!filename.startsWith("/"))
			filename = Environment.getExternalStorageDirectory() + File.separator + "einbindetiefe" + File.separator + filename;
		
		
		
		
		// Check if no extension is given and add it if necessary
		int index = filename.lastIndexOf(".");
		if (index < 0) return filename + (defaultFormat==CompressFormat.PNG?".png":".jpeg");
		if(index == filename.length()) return filename + (defaultFormat==CompressFormat.PNG?"png":"jpeg");
		
		// Check if the right extension is given
		final String extension = filename.substring(index+1);
		if(extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg")) return filename;
		
		// Illegal extension. Use default one
		return filename.substring(0, index) + (defaultFormat==CompressFormat.PNG?".png":".jpeg");
	}
	
	/**
	 * Checks if the given extension of the filename is accepted. If the filename is null or empty false is returned.
	 * If the file has no extension, false is returned 
	 * @param filename to be checked
	 * @return true if it has a valid file extension
	 */
	private boolean hasValidFileExtension(final String filename) {
		if(filename == null || filename.length() == 0) return false;
		
		int index = filename.lastIndexOf(".");
		if (index < 0 || index == filename.length()) return false;
		final String extension = filename.substring(index+1);
		return extension.equalsIgnoreCase("png") || extension.equalsIgnoreCase("jpeg");
	}
	
	/**
	 * Checks if the given filename has an extension
	 * @param filename to be checked
	 * @return true if it has an extension, false if not
	 */
	private boolean hasFileExtension(final String filename) {
		if(filename == null || filename.length() == 0) return false;
		int index = filename.lastIndexOf(".");
		return ! (index < 0 || index == filename.length());
		
	}

	
	/**
	 * Do the export process
	 */
	private void doExport() {
		txtStatus.setText(R.string.inputFilename);
		final String filename = getFilename();
		if (filename == null || filename.length() == 0) {
			txtStatus.setText(R.string.empty_filename);
			btExport.setEnabled(false);
			return;
		}
		final File file = new File(filename);

		// Check if file exists
		if (file.exists()) {
			txtStatus.setText(R.string.file_exists);
			btExport.setEnabled(false);
			return;
		}

		
		
		// Extract file format
		final Bitmap.CompressFormat format = getFileformat(filename);
		if(format == null) {
			txtStatus.setText(R.string.illegal_fileending);
			btExport.setEnabled(false);
			return;
		}
		
		
		btExport.setEnabled(false);
		FileOutputStream output = null;
		try {
			txtStatus.setText(R.string.status_compress);
			final ByteArrayOutputStream data = new ByteArrayOutputStream();
			exportBitmap.compress(format, PNG_COMPRESSION_LEVEL, data);

			txtStatus.setText(R.string.status_write);
			createExportDirectory();
			output = new FileOutputStream(file, false);
			output.write(data.toByteArray());
			output.flush();
			output.close();

			txtStatus.setText(R.string.status_done);
			this.dismiss();

			toast(context.getString(R.string.export_finished) + ":\n" + filename);

		} catch (IOException e) {
			txtStatus.setText(e.getLocalizedMessage());
		} finally {
			btExport.setEnabled(true);
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					// Ignore
				}
		}
	}

	/**
	 * Shows up a toast message with long duration
	 * 
	 * @param message
	 *            to be shown up
	 */
	private void toast(CharSequence message) {
		int duration = Toast.LENGTH_LONG;
		final Toast toast = Toast.makeText(context, message, duration);
		toast.show();
	}

}
