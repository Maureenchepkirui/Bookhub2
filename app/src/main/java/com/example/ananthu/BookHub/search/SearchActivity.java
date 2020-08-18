package com.example.ananthu.BookHub.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.ananthu.BookHub.Constants;
import com.example.ananthu.BookHub.InternalStorage;
import com.example.ananthu.BookHub.R;
import com.example.ananthu.BookHub.adapters.BookRecyclerViewAdapter;
import com.example.ananthu.BookHub.model.Author;
import com.example.ananthu.BookHub.model.Book;
import com.example.ananthu.BookHub.network.GoodreadRequest;

import java.util.ArrayList;
import java.util.List;


public class SearchActivity extends AppCompatActivity implements com.example.ananthu.BookHub.search.SearchView {
    private static final String TAG = SearchActivity.class.getName();
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mEditor;
    private Book book;
    private List<Book> books = new ArrayList<>();
    private GoodreadRequest mGoodreadRequest;
    private BookRecyclerViewAdapter bookRecyclerViewAdapter;
    private SearchView bookSearch;
    private RecyclerView bookRecyclerView;
    private ProgressBar loadingIcon;
    private InternalStorage cache;
    private SearchPresenter searchPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                loadingIcon.setVisibility(View.VISIBLE);
                bookRecyclerView.setVisibility(View.GONE);
                bookRecyclerViewAdapter.clear();
                searchPresenter.searchQuery(query, mGoodreadRequest, cache);
                return false;

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
        Toast.makeText(
                getApplicationContext(),
                "showing results",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToast(String t) {
        Toast.makeText(this, t, Toast.LENGTH_SHORT).show();
    }
}