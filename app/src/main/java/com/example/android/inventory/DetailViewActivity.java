package com.example.android.inventory;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;
import com.example.android.inventory.data.ProductContract.ProductEntry;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

import static java.lang.Integer.parseInt;


/**
 * Allows user to edit an existing product.
 */
public class DetailViewActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the product data loader */
    private static final int EXISTING_PET_LOADER = 0;

    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's quantity
     */
    private TextView mQuantityTextView;

    /**
     * EditText field to decrease the product's quantity
     */
    private EditText mQuantityIncreaseEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mQuantityDecreaseEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the product's supplier
     */
    private EditText mSupplierEditText;

    // Button field to upload the new product's image
    private Button mImageUploadButton;

    // ImageView field to display stored product image
    private ImageView mImageView;

    // Content URI for the existing pet (null if it's a new pet)
    private Uri mCurrentProductUri;

    private boolean mPetHasChanged = false;

    private Button mDecreaseQuantityButton;

    private Button mIncreaseQuantityButton;

    private Button mOrderSupplierButton;

    private ImageView mUploadedImageView;

    public static final int PICK_IMAGE = 1;

    // OnTouchListener that listen for any user touches on a View, implying that they are
    // modifying the view, and we change the mPetHasChanged boolean to true
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mPetHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_view);

        // Examine the intent that was clicked on launch this activity
            // in order to figure out we're editing an existing product.
            Intent intent = getIntent();
            mCurrentProductUri = intent.getData();

            // Find all relevant views that we will need to populate data from database
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mQuantityTextView = (TextView) findViewById(R.id.current_product_quantity);
        mQuantityIncreaseEditText = (EditText) findViewById(R.id.increase_product_quantity);
        mQuantityDecreaseEditText = (EditText) findViewById(R.id.decrease_product_quantity);
        mDecreaseQuantityButton = (Button) findViewById(R.id.decrease_product_quantity_button);
        mIncreaseQuantityButton = (Button) findViewById(R.id.increase_product_quantity_button);
        mOrderSupplierButton = (Button) findViewById(R.id.order_more_product_supplier);
        mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
        mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
        mImageUploadButton = (Button) findViewById(R.id.button_new_image_upload);


        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mQuantityDecreaseEditText.setOnTouchListener(mTouchListener);
        mQuantityIncreaseEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mImageUploadButton.setOnTouchListener(mTouchListener);
        mOrderSupplierButton = (Button) findViewById(R.id.order_more_product_supplier);

        mDecreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseQuantity();
            }
        });

        mIncreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseQuantity();
            }
        });

        mImageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(DetailViewActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailViewActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        });

        mOrderSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nameString = mNameEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // Only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "Product order request for " + nameString);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                    }
            }
        });
        // Kick off the loader
        getSupportLoaderManager().initLoader(EXISTING_PET_LOADER, null, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String picturePath;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode== Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                picturePath = cursor.getString(columnIndex);
                cursor.close();
                ImageView mUploadedImageView = (ImageView) findViewById(R.id.view_uploaded_image);
                mUploadedImageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
        }
    }

    // Decrease product quantity with value provided when decrease button submitted & update current quantity field.
    private void decreaseQuantity() {

        mQuantityDecreaseEditText = (EditText) findViewById(R.id.decrease_product_quantity);
        String decreaseQuantityString = mQuantityDecreaseEditText.getText().toString().trim();
        int decreaseQuantity = parseInt(decreaseQuantityString);

        mQuantityTextView = (TextView) findViewById(R.id.current_product_quantity);
        String quantityStr = mQuantityTextView.getText().toString().trim();
        int currentQuantity = parseInt(quantityStr);
        int quantityInt = currentQuantity - decreaseQuantity;
        String updatedQuantity = String.valueOf(quantityInt);
        mQuantityTextView.setText(updatedQuantity);
}

    // Increase product quantity with value provided when increase button submitted & update current quantity field.
    private void increaseQuantity() {

        mQuantityIncreaseEditText = (EditText) findViewById(R.id.increase_product_quantity);
        String increaseQuantityString = mQuantityIncreaseEditText.getText().toString().trim();
        int increaseQuantity = parseInt(increaseQuantityString);

        mQuantityTextView = (TextView) findViewById(R.id.current_product_quantity);
        String quantityStr = mQuantityTextView.getText().toString().trim();
        int currentQuantity = parseInt(quantityStr);
        int quantityInt = currentQuantity + increaseQuantity;
        String updatedQuantity = String.valueOf(quantityInt);
        mQuantityTextView.setText(updatedQuantity);
    }

    /**
         * Get user input from editor and update product into database.
         */
        private void saveProduct() {


            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String nameString = mNameEditText.getText().toString().trim();
            String quantityString = mQuantityTextView.getText().toString().trim();
            String priceString = mPriceEditText.getText().toString().trim();
            String supplierString = mSupplierEditText.getText().toString().trim();

            ImageView mUploadedImageView = (ImageView) findViewById(R.id.view_uploaded_image);
            Bitmap bitmap = ((BitmapDrawable) mUploadedImageView.getDrawable()).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream);

            byte[] imageInByte = byteArrayOutputStream.toByteArray();

            if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString)
                    && TextUtils.isEmpty(priceString) && TextUtils.isEmpty(supplierString)) {
                // Since no fields were entered, so no need to create ContentValues object and no need to do any ContentProvider operations.
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and pet attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageInByte);

            if (mCurrentProductUri != null) {
            // This is an existing product, so update the product into the provider
            // returning the content URI for the new product.
           int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            // Show a toast message depending on whether or not the insertion was successful
            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.detailView_error_with_updating_product),
                        Toast.LENGTH_SHORT).show();
            } else {

                // Otherwise update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.detailView_product_updated_successfully),
                        Toast.LENGTH_SHORT).show();
            }
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu options from the res/menu/menu_editor.xml file.
            // This adds menu items to the app bar.
            getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // User clicked on a menu option in the app bar overflow menu
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_save:

                 if (mNameEditText == null || mQuantityTextView == null
                        || mPriceEditText == null || mSupplierEditText == null ||  mUploadedImageView == null) {
                    Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show();

                } else {
                     // Save pet to database
                     saveProduct();
                     // Exit activity
                     finish();
                 }

                 return true;
                // Respond to a click on the "Delete" menu option
                case R.id.action_delete:

                  // Popup confirmation dialog for deletion
                    showDeleteConfirmationDialog();
                    return true;

                // Respond to a click on the "Up" arrow button in the app bar
                case android.R.id.home:
                    // If the pet hasn't changed, continue with navigating up to parent activity
                    // which is the {@link CatalogActivity}.
                    if (!mPetHasChanged) {
                        // Navigate back to parent activity (CatalogActivity)
                        NavUtils.navigateUpFromSameTask(DetailViewActivity.this);
                        return true;
                    }

                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked the "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(DetailViewActivity.this);
                        }
                    };

                    // Show dialog that there are unsaved changes
                    showUnsavedChangesDialog(discardButtonClickListener);
                    return true;
            }

            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onBackPressed() {
            // If the pet hasn't changed, continue handling back button press
            if (!mPetHasChanged) {
                super.onBackPressed();
                return;
            }

            // Otherwise if there are unsaved changes, setup a dialog to warn the user.
            // Create a click listener to handle the user confirming that changes should be discarded.
            DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // User clicked the "Discard" button, close the current activity.
                    finish();
                }
            };

            // Show dialog that there are unsaved changes
            showUnsavedChangesDialog(discardButtonClickListener);
        }


        @NonNull
        @Override
        public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle bundle) {

            // Define a projection that specifies which columns from the table we care about
            String[] projection = {
                    ProductEntry._ID,
                    ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductEntry.COLUMN_PRODUCT_QUANTITY,
                    ProductEntry.COLUMN_PRODUCT_PRICE,
                    ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                    ProductEntry.COLUMN_PRODUCT_IMAGE,
            };

            // This loader will execute the ContentProvider's query method on a background thread.
            // Perform a query on the provider using the ContentResolver.
            // Use the {@link ProductEntry#CONTENT_URI} to access the pet data.
            return new CursorLoader(this, // Parent activity context
                    mCurrentProductUri,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
            // Bail early if the cursor is null or there is less than 1 row in the cursor
            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            // Proceed with moving to the first row of the cursor and reading data from it.
            // This should be the only row in the cursor
            if (cursor.moveToFirst()) {

                // Find the columns of product attributes that we're interested in.
                int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
                int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
                int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
                int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
                int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

                // Read the product attributes from the cursor for the current product
                String name = cursor.getString(nameColumnIndex);
                int quantity = cursor.getInt(quantityColumnIndex);
                int price = cursor.getInt(priceColumnIndex);
                String supplier = cursor.getString(supplierColumnIndex);
                byte[] image = cursor.getBlob(imageColumnIndex);
                ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                // Update views on the screen with the values from the database
                mNameEditText.setText(name);
                mQuantityTextView.setText(Integer.toString(quantity));
                mPriceEditText.setText(Integer.toString(price));
                mSupplierEditText.setText(supplier);
                mUploadedImageView = (ImageView) findViewById(R.id.view_uploaded_image);
                mUploadedImageView.setImageBitmap(bitmap);
                }
            }

            @Override
        public void onLoaderReset(@NonNull Loader<Cursor> loader) {

            // If the loader is invalidated, clear out all the data from the input fields.
                mNameEditText.setText("");
                mQuantityTextView.setText("");
                mPriceEditText.setText("");
                mSupplierEditText.setText("");
                mUploadedImageView.setImageBitmap(null);
        }

        private void showUnsavedChangesDialog(
                DialogInterface.OnClickListener discardButtonClickListener) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the positive and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changes_dialog_msg);
            builder.setPositiveButton(R.string.discard, discardButtonClickListener);
            builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Keep editing" button, so dismiss the dialog
                    // and continue editing the pet.
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        private void showDeleteConfirmationDialog() {

            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the positive and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_product_dialog_msg);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the pet.
                    deletePet();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog.
                    // and continue editing the pet
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        /**
         * Perform the deletion of the pet in the database.
         */
        private void deletePet() {

            // Only perform the delete if this is an existing product.
            if (mCurrentProductUri != null) {
                // Call the ContentResolver to delete the pet at the given content URI.
                // Pass in null for the selection and selection args
                // because mCurrentPetUri already identifies the product we want.
                int rowDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

                // Show a toast message depending on whether or not the insertion was successful
                if (rowDeleted == 0) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.detailView_error_with_deleting_product),
                            Toast.LENGTH_SHORT).show();
                } else {

                    // Otherwise update was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.detailView_product_deleted_successfully),
                            Toast.LENGTH_SHORT).show();
                }

            }
            // Close the activity
            finish();
        }
    }
