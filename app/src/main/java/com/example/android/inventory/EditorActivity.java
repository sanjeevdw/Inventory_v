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
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows user to create a new product.
 */
public class EditorActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 1;
    /**
     * EditText field to enter the product's name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the product's quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the product's price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the product's supplier
     */
    private EditText mSupplierEditText;


    // null for a new product
    private Uri mCurrentProductUri = null;

    private boolean mPetHasChanged = false;

    private ImageView mUploadedImageView;


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
        setContentView(R.layout.activity_editor);

        if (mCurrentProductUri == null) {

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        }

        // Find all relevant views that we will need to read user input from
            mNameEditText = (EditText) findViewById(R.id.edit_product_name);
            mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
            mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
            mSupplierEditText = (EditText) findViewById(R.id.edit_product_supplier);
            Button mImageUploadButton = (Button) findViewById(R.id.button_image_upload);

            mNameEditText.setOnTouchListener(mTouchListener);
            mQuantityEditText.setOnTouchListener(mTouchListener);
            mPriceEditText.setOnTouchListener(mTouchListener);
            mSupplierEditText.setOnTouchListener(mTouchListener);
            mImageUploadButton.setOnTouchListener(mTouchListener);

        mImageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (ActivityCompat.checkSelfPermission(EditorActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditorActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PICK_IMAGE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE);
                    }
                } catch (Exception e) {
                e.printStackTrace();
                    }
                    }
                    });
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

    /**
         * Get user input from editor and save product into database.
         */
        private void saveProduct() {

            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String nameString = mNameEditText.getText().toString().trim();
            String quantityString = mQuantityEditText.getText().toString().trim();
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

            // This is a new product, so insert a new product into the provider
                // returning the content URI for the new product.
                Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_error_with_saving_product),
                            Toast.LENGTH_SHORT).show();
                } else {

                    // Otherwise update was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.editor_product_saved_successfully),
                            Toast.LENGTH_SHORT).show();
                }

            }

            @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu options from the res/menu/menu_editor.xml file.
            // This adds menu items to the app bar.
            getMenuInflater().inflate(R.menu.menu_editor, menu);
            return true;
        }

        /**
         * This method is called after invalidateOptionsMenu(), so that the
         * menu can be updated (some menu items can be hidden or made visible)
         */
        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            super.onPrepareOptionsMenu(menu);
            // If this is a new pet, hide the "Delete" menu item.
            if (mCurrentProductUri == null) {

                MenuItem menuItem = menu.findItem(R.id.action_delete);
                menuItem.setVisible(false);
            }
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // User clicked on a menu option in the app bar overflow menu
            switch (item.getItemId()) {
                // Respond to a click on the "Save" menu option
                case R.id.action_save:

                    if (mNameEditText == null || mQuantityEditText == null
                            || mPriceEditText == null || mSupplierEditText == null || mUploadedImageView == null) {
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

                 // Do nothing for now
                    return true;

                // Respond to a click on the "Up" arrow button in the app bar
                case android.R.id.home:
                    // If the pet hasn't changed, continue with navigating up to parent activity
                    // which is the {@link CatalogActivity}.
                    if (!mPetHasChanged) {
                        // Navigate back to parent activity (CatalogActivity)
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                        return true;
                    }

                    // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that changes should be discarded.
                    DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // User clicked the "Discard" button, navigate to parent activity.
                            NavUtils.navigateUpFromSameTask(EditorActivity.this);
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
        }
