package com.example.android.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.sql.Blob;

/**
 * Defines table and column names for the inventory database.
 */

public final class ProductContract {

    private ProductContract() {
        }

        /**
         * The "Content authority" is a name for the entire content provider, similar to the
         * relationship between a domain name and its website.  A convenient string to use for the
         * content authority is the package name for the app, which is guaranteed to be unique on the
         * device.
         */
        public static final String CONTENT_AUTHORITY = "com.example.android.inventory";

        /**
         * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
         * the content provider.
         */
        public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

        /**
         * Possible path (appended to base content URI for possible URI's)
         * For instance, content://com.example.android.inventory/inventory/ is a valid path for
         * looking at inventory data. content://com.example.android.inventory/staff/ will fail,
         * as the ContentProvider hasn't been given any information on what to do with "staff".
         */
        public static final String PATH_INVENTORY = "inventory";

        public static final class ProductEntry implements BaseColumns {

            /**
             * The content URI to access the inventory data in the provider
             */
            public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

            /**
             * The MIME type of the {@link #CONTENT_URI} for a list of products.
             */

            public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

            /**
             * The MIME type of the {@link #CONTENT_URI} for a single product.
             */

            public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

            public static final String TABLE_NAME = "inventory";

            public static final String _ID = BaseColumns._ID;
            public static final String COLUMN_PRODUCT_NAME = "name";
            public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
            public static final String COLUMN_PRODUCT_PRICE = "price";
            public static final String COLUMN_PRODUCT_IMAGE = "image";
            public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";

            }
}
