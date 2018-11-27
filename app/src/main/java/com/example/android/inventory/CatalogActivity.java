package com.example.android.inventory;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.data.ProductContract;

import java.io.ByteArrayOutputStream;

import static java.lang.Integer.parseInt;

/**
 * Displays list of products that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    ProductCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                    startActivity(intent);
                }
            });

            // Find ListView to populate
            ListView productListView = (ListView) findViewById(R.id.list);

            // Find and set empty view on the ListView so that it only shows when the list has 0 items.
            View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

            // Setup an cursor adapter to create a list item for each row of pet data in the Cursor.
            mCursorAdapter = new ProductCursorAdapter(this, null);

            // Attach cursor adapter to the ListView
        productListView.setAdapter(mCursorAdapter);

            // Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    // Create a new intent to go to {@link EditorActivity}
                    Intent intent = new Intent(CatalogActivity.this, DetailViewActivity.class);

                    // From the content URI that represents the specific pet that was clicked on,
                    // by appending the "id" (passed as input to this method) onto the
                    // {@link PetEntry#CONTENT_URI}.
                    // For example, the URI would be "content://com.example.android.pets/pets/2"
                    // if the pet with ID 2 was clicked on.
                   final Uri currentProductUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
                    intent.setData(currentProductUri);
                    Button saleButton = (Button) findViewById(R.id.sale_button);
                    // Launch the {@link EditorActivity} to display the product data
                    startActivity(intent);
                }
                });

        // Kick off the loader
            getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);

            }

    public void onButtonClick(long id) {

        final Uri buttonClickUri = ContentUris.withAppendedId(ProductContract.ProductEntry.CONTENT_URI, id);
        saveUpdatedQuantity(buttonClickUri);
    }

        // Decrease product quantity with one when sale button is clicked & update current quantity field.
        public void saveUpdatedQuantity(Uri buttonClickUri) {
         TextView quantityTextView = (TextView) findViewById(R.id.quantity);
         String quantityString = quantityTextView.getText().toString().trim();

        int currentQuantity = parseInt(quantityString);
        currentQuantity = currentQuantity - 1;
        String updatedQuantity = String.valueOf(currentQuantity);

        // Create a ContentValues object where column names are the keys,
        // and product attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, updatedQuantity);

        if (buttonClickUri != null) {
            // This is an existing product, so update the changed quantity into the provider
            // returning the content URI for the new product.
            int rowsAffected = getContentResolver().update(buttonClickUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.quantity_update_failed),
                        Toast.LENGTH_SHORT).show();
            } else {

                // Otherwise update was successful and we can display a toast
                Toast.makeText(this, getString(R.string.quantity_update_success),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
        /**
         * Helper method to insert hardcoded product data into the database. For debugging purposes only.
         */
        private void insertProduct() {

            Bitmap bitmap = ((BitmapDrawable) getDrawable(R.drawable.ic_empty_shelter)).getBitmap();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, byteArrayOutputStream);
            byte[] imageInByte = byteArrayOutputStream.toByteArray();

            // Create a ContentValues object where column names are the keys,
            // and Toto's pet attributes are the values.
            ContentValues values = new ContentValues();
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, "Lehenga Choli");
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 5);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, 24);
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER, "OMTatSat");
            values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_IMAGE, imageInByte);

            // Insert a new row for Toto into the provider using the ContentResolver.
            // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
            // into the pets database table.
            // Receive the new content URI that will allow us to access Toto's data in the future.
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu options from the res/menu/menu_catalog.xml file.
            // This adds menu items to the app bar.
            getMenuInflater().inflate(R.menu.menu_catalog, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // User clicked on a menu option in the app bar overflow menu
            switch (item.getItemId()) {
                // Respond to a click on the "Insert dummy data" menu option
                case R.id.action_insert_dummy_data:
                    insertProduct();
                    return true;
                // Respond to a click on the "Delete all entries" menu option
                case R.id.action_delete_all_entries:
                    showDeleteConfirmationDialog();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

            // Define a projection that specifies which columns from the table we care about
            String[] projection = {
                    ProductContract.ProductEntry._ID,
                    ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                    ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                    ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE};

            // This loader will execute the ContentProvider's query method on a background thread.
            // Perform a query on the provider using the ContentResolver.
            // Use the {@link ProductEntry#CONTENT_URI} to access the product data.
            return new CursorLoader(this, // Parent activity context
                    ProductContract.ProductEntry.CONTENT_URI,   // Provider content URI to query
                    projection,             // Columns to include in the resulting cursor
                    null,                   // No selection clause
                    null,               // No selection arguments
                    null);                 // Default sort order
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            // Update {@link ProductCursorAdapter} with this new cursor containing updated product data.
            mCursorAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            // Callback called when the data needs to be deleted
            mCursorAdapter.swapCursor(null);
            }

        private void showDeleteConfirmationDialog() {

            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the positive and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.delete_all_products_dialog_msg);
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the products.
                    deleteAllProducts();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Cancel" button, so dismiss the dialog.
                    // and continue editing the product
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                }
            });

            // Create and show the AlertDialog
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }

        // Helper method to delete all products in the database.
        private void deleteAllProducts() {

            // Only perform the delete if table or URI for all products exists.
            if (ProductContract.ProductEntry.CONTENT_URI != null) {
                // Call the ContentResolver to delete all the products at the given content URI.
                // Pass in null for the selection and selection args
                // because the content URI is for the whole table
                int deleteAllPets = getContentResolver().delete(ProductContract.ProductEntry.CONTENT_URI, null, null);
                Log.v("CatalogActivity", deleteAllPets + "rows deleted from pet database");

                // Show a toast message depending on whether or not the insertion was successful
                if (deleteAllPets == 0) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.error_with_deleting_products),
                            Toast.LENGTH_SHORT).show();
                } else {

                    // Otherwise update was successful and we can display a toast
                    Toast.makeText(this, getString(R.string.products_deleted_successfully),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        }

