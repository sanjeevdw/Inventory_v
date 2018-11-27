package com.example.android.inventory;

import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.CursorAdapter;

import com.example.android.inventory.data.ProductContract;

import org.w3c.dom.Text;

import static java.lang.Integer.parseInt;

/**
 * {@link ProductCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of pet data as its data source. This adapter knows
 * how to create list items for each row of product data in the {@link Cursor}.
 */
public class ProductCursorAdapter extends CursorAdapter {

    /**
         * Constructs a new {@link ProductCursorAdapter}.
         *
         * @param context The context
         * @param c       The cursor from which to get the data.
         */

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
         * Makes a new blank list item view. No data is set (or bound) to the views yet.
         *
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already
         *                moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created list item view.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Fill out this method and return the list item view (instead of null)
            // The newView method is used to inflate a new view and return it.
            // we don't bind any data to the view at this point.
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        /**
         * This method binds the pet data (in the current row pointed to by cursor) to the given
         * list item layout. For example, the name for the current pet can be set on the name TextView
         * in the list item layout.
         *
         * @param view    Existing view, returned earlier by newView() method
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already moved to the
         *                correct row.
         */

        @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {

            // Fill out this method
            //Find individual views that we want to modify in the list item layout.
            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            TextView quantityLabelTextView = (TextView) view.findViewById(R.id.quantity_label);
            TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
            TextView priceLabelTextView = (TextView) view.findViewById(R.id.price_label);
            TextView priceTextView = (TextView) view.findViewById(R.id.price);
            Button saleButton = (Button) view.findViewById(R.id.sale_button);

            // Find the columns of products attributes that we're interested in.
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);

            // Read the pet attributes from the cursor for the current pet
            String productName = cursor.getString(nameColumnIndex);
            String productQuantity = cursor.getString(quantityColumnIndex);
            String productPrice = cursor.getString(priceColumnIndex);

            // Populate fields with extracted properties
            nameTextView.setText(productName);
            quantityTextView.setText(productQuantity);
            priceTextView.setText(productPrice);

            saleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int position = cursor.getPosition();
                    long id = getItemId(position);
                    ((CatalogActivity) context).onButtonClick(id);
                    }
            });
             }
    }