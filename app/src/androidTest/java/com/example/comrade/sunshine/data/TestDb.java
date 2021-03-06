/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.comrade.sunshine.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    /* Create db tests
       */
    public void testCreateDb() throws Throwable {
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(WeatherContract.LocationEntry.TABLE_NAME);
        tableNameHashSet.add(WeatherContract.WeatherEntry.TABLE_NAME);

        mContext.deleteDatabase(WeatherDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new WeatherDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database was created without both the location entry and weather entry tables",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + WeatherContract.LocationEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(WeatherContract.LocationEntry._ID);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_CITY_NAME);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LAT);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_COORD_LONG);
        locationColumnHashSet.add(WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required location
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    // Testing the Location table
    public void testLocationTable() {
        // First step: Get reference to writable database



        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        // Insert ContentValues into database and get a row ID back


        // return locationRowId;
    }

    /* Testing the Weather table */
    public void testWeatherTable() {
        // First step: Get reference to writable database
        long locationRowId = insertLocation();
        // if location id returned from insertData() is -1 then its a problem for us
        assertTrue("Error: Did not return any data", locationRowId != -1);


        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create weatherData using location id(foreign key).
        ContentValues weatherValues = TestUtilities.createWeatherValues(locationRowId);

        // Insert ContentValues into database and get a row ID back
        long weatherRowId = db.insert(WeatherContract.WeatherEntry.TABLE_NAME, null , weatherValues);

        // If no row is returned from DB
        assertTrue("Error: No row is returned from DB", locationRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
          WeatherContract.WeatherEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertTrue("Error: No data was inserted ", cursor.moveToFirst());
        // Move the cursor to a valid database row
        // Validate data in resulting Cursor with the original ContentValues
        TestUtilities.validateCurrentRecord("Error: Data does not match", cursor, weatherValues);

        // Finally, close the cursor and database
        cursor.close();
        db.close();
    }


    /*
        Location table data creation..
     */
    public long insertLocation() {
        WeatherDbHelper dbHelper = new WeatherDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Create ContentValues of what you want to insert
        // (you can use the createNorthPoleLocationValues if you wish)
        ContentValues testValues = TestUtilities.createNorthPoleLocationValues();

        // Insert ContentValues into database and get a row ID back
        long locationRowId = db.insert(WeatherContract.LocationEntry.TABLE_NAME, null , testValues);

        // Verify data is inserted into db.
        assertTrue(locationRowId != -1);

        // Query the database and receive a Cursor back
        Cursor cursor = db.query(
                WeatherContract.LocationEntry.TABLE_NAME,
                null, // all columns
                null, // Columns for where clause
                null, // Values for where clause
                null, // columns to group by
                null, // columns to filter by row group
                null  // sort order
        );

        // Move the cursor to a valid database row
        assertTrue("Error: No record returned from location query", cursor.moveToFirst());

        // Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord("Error: Location query validation failed ", cursor, testValues);
        // Finally, close the cursor and database
        assertFalse("Error: More than one record returned from the location query", cursor.moveToNext());

        // Close the cursor and db
        cursor.close();
        db.close();

        return locationRowId;
    }
}
