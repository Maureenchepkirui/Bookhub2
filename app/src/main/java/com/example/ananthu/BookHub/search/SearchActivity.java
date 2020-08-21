package com.example.ananthu.BookHub.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.ananthu.BookHub.R;
import com.example.ananthu.BookHub.adapters.BookRecyclerViewAdapter;
import com.example.ananthu.BookHub.model.Book;
import com.example.ananthu.BookHub.network.GoodreadRequest;
import com.example.ananthu.BookHub.ui.Constants;
import com.example.ananthu.BookHub.ui.InternalStorage;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements com.example.ananthu.BookHub.search.SearchView {
    private static final String TAG = SearchActivity.class.getName();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private DatabaseReference mSearchedBookReference;

    private List<Book> books = new ArrayList<>();
    private GoodreadRequest mGoodreadRequest;
    private BookRecyclerViewAdapter bookRecyclerViewAdapter;
    private SearchView bookSearch;
    private RecyclerView bookRecyclerView;
    private ProgressBar loadingIcon;
    private InternalStorage cache;
    private SearchPresenter searchPresenter;
    private ValueEventListener mSearchedBookReferenceListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSearchedBookReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(Constants.FIREBASE_CHILD_SEARCHED_BOOK);
        mSearchedBookReferenceListener = mSearchedBookReference.addValueEventListener(new ValueEventListener() { //attach listener

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { //something changed!
                for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                    String book = bookSnapshot.getValue().toString();
                    Log.d("Books updated", "book: " + book); //log
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { //update UI here if error occurred.

            }

        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        cache = InternalStorage.getInstance();
        mGoodreadRequest = new GoodreadRequest(getString(R.string.GR_API_Key), this);
        searchPresenter = new SearchPresenter(this);


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mSharedPreferences.edit();

        bookRecyclerView = findViewById(R.id.book_recycler_view);
        loadingIcon = findViewById(R.id.loading_icon);

        // for smooth scrolling in recycler view
        bookRecyclerView.setNestedScrollingEnabled(false);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        bookRecyclerView.setLayoutManager(layoutManager);


        bookRecyclerViewAdapter = new BookRecyclerViewAdapter(books);
        bookRecyclerView.setAdapter(bookRecyclerViewAdapter);

        bookSearch = findViewById(R.id.book_search);


        bookSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String book =query;
                saveBookToFirebase(book);
                loadingIcon.setVisibility(View.VISIBLE);
                bookRecyclerView.setVisibility(View.GONE);
                bookRecyclerViewAdapter.clear();
                searchPresenter.searchQuery(query, mGoodreadRequest, cache);
                addToSharedPreferences(book);
                if(!(book).equals("")) {
                    addToSharedPreferences(book);
                }
                return false;

            }

            public void saveBookToFirebase(String book) {
                mSearchedBookReference.setValue(book);
            }
            private void addToSharedPreferences(String book) {
                mEditor.putString(Constants.PREFERENCES_LOCATION_KEY, book).apply();

            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        bookSearch.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                bookSearch.setIconified(true);
            }
        });
    }

    @Override
    public void showBookResult(Book book) {

        loadingIcon.setVisibility(View.GONE);
        bookRecyclerView.setVisibility(View.VISIBLE);
        bookRecyclerViewAdapter.add(book);

    }

    @Override
    public void showToast(String t) {
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearchedBookReference.removeEventListener(mSearchedBookReferenceListener);
    }
}
