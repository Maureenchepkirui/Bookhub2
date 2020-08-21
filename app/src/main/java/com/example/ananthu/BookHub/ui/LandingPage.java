package com.example.ananthu.BookHub.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ananthu.BookHub.R;
import com.example.ananthu.BookHub.adapters.BookRecyclerViewAdapter;
import com.example.ananthu.BookHub.model.Book;
import com.example.ananthu.BookHub.model.BookBuilder;
import com.example.ananthu.BookHub.network.GoodreadRequest;
import com.example.ananthu.BookHub.network.SuccessFailedCallback;
import com.example.ananthu.BookHub.search.SearchActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class LandingPage extends AppCompatActivity {
    private static final String TAG = LandingPage.class.getSimpleName();

    private final int INTERNET_PERMISSION = 1;
    private GoodreadRequest mGoodreadRequest;
    private InternalStorage cache;

    private BookRecyclerViewAdapter bookRecyclerViewAdapter;
    private RecyclerView bookRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_landing_page);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        InternalStorage.init(getApplicationContext());
        cache = InternalStorage.getInstance();

        bookRecyclerView = findViewById(R.id.recyclerViewLandingPage);

        // for smooth scrolling in recycler view
        bookRecyclerView.setNestedScrollingEnabled(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        bookRecyclerView.setLayoutManager(layoutManager);


//        bookRecyclerViewAdapter = new BookRecyclerViewAdapter(new ArrayList<>());
//        bookRecyclerView.setAdapter(bookRecyclerViewAdapter);


        requestInternetPermission();

        mGoodreadRequest = new GoodreadRequest(getString(R.string.GR_API_Key), this);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), SearchActivity.class)));


    }

    @Override
    protected void onResume() {
        loadFavBooks();
        super.onResume();
        Toast.makeText(
                getApplicationContext(),
                "Reading List",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadFavBooks() {
        final List<Integer> favBookIds = cache.getFavListCache();
        Log.d(TAG, "loadFavBooks: loading fav books");
        bookRecyclerViewAdapter = new BookRecyclerViewAdapter(new ArrayList<>());

        for (int i = 0; i < favBookIds.size(); i++) {
            Log.d(TAG, "loadFavBooks: fav book" + favBookIds.get(i));
            if (cache.getCachedBookById(favBookIds.get(i)) == null) {

                mGoodreadRequest.getBook(favBookIds.get(i), new SuccessFailedCallback() {
                    @Override
                    public void success(String response) {

                        Book book = BookBuilder.getBookFromXML(response);
                        cache.cacheBook(book);
                        bookRecyclerViewAdapter.add(book);

                    }

                    @Override
                    public void failed() {
                        Toast.makeText(
                                getApplicationContext(),
                                "some error occurred",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                bookRecyclerViewAdapter.add(cache.getCachedBookById(favBookIds.get(i)));
            }

        }
        bookRecyclerView.setAdapter(bookRecyclerViewAdapter);
        bookRecyclerView.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_landing_page, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    private void requestInternetPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.INTERNET)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.INTERNET},
                        INTERNET_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case INTERNET_PERMISSION: {
                if (grantResults.length <= 0
                        || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Internet Access denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}