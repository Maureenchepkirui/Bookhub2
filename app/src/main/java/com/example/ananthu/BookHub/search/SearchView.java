package com.example.ananthu.BookHub.search;


import com.example.ananthu.BookHub.model.Book;
import com.example.ananthu.BookHub.util.Toastable;


public interface SearchView extends Toastable {

    /**
     * Adds a book relevant to the search query
     * @param book book object parsed from the response XML
     */
    void showBookResult(Book book);

}
