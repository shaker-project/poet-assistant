/*
 * Copyright (c) 2016 Carmen Alvarez
 *
 * This file is part of Poet Assistant.
 *
 * Poet Assistant is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Poet Assistant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Poet Assistant.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.rmen.android.poetassistant.main;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;

import java.util.Locale;

import ca.rmen.android.poetassistant.Constants;

/**
 * Glue between the fragments, activity, and view pager, for executing searches.
 *
 * The activity calls this class to perform searches.  This class retrieves the fragments from
 * the Viewpager, and calls the fragments (which call the adapters) to perform the search and
 * display the search results.
 *
 * This class also configures the SearchView widget, and intercepts searches to add them to
 * the list of suggested words.
 */
class Search {
    private static final String TAG = Constants.TAG + Search.class.getSimpleName();
    private SearchView mSearchView;
    private final ViewPager mViewPager;
    private final Suggestions.SuggestionsCursorAdapter mSuggestionsCursorAdapter;
    private final Activity mSearchableActivity;

    public Search(Activity searchableActivity, ViewPager viewPager) {
        mSearchableActivity = searchableActivity;
        mViewPager = viewPager;
        mSuggestionsCursorAdapter = new Suggestions.SuggestionsCursorAdapter(mSearchableActivity);
    }

    public void setSearchView(SearchView searchView) {
        mSearchView = searchView;
        SearchManager searchManager = (SearchManager) mSearchableActivity.getSystemService(Context.SEARCH_SERVICE);
        ComponentName searchableActivityComponentName = new ComponentName(mSearchableActivity, mSearchableActivity.getClass());
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(searchableActivityComponentName));
        mSearchView.setOnQueryTextListener(mOnQueryTextListener);
        mSearchView.setSuggestionsAdapter(mSuggestionsCursorAdapter);
        mSearchView.setOnSuggestionListener(mOnSuggestionListener);
    }

    /**
     * Search for the given word in the given dictionary, and set the current tab
     * to that dictionary (if it's not already the case).
     */
    void search(String word, Tab tab) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "], tab = [" + tab + "]");
        mViewPager.setCurrentItem(tab.ordinal());
        word = word.trim().toLowerCase(Locale.US);
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        ((ResultListFragment) mViewPager.getAdapter().instantiateItem(mViewPager, tab.ordinal())).query(word);
    }

    /**
     * Search for the given word in both dictionaries
     */
    void search(String word) {
        Log.d(TAG, "search() called with: " + "word = [" + word + "]");
        word = word.trim().toLowerCase(Locale.US);
        // Not intuitive: instantiateItem will actually return an existing Fragment, whereas getItem() will always instantiate a new Fragment.
        // We want to retrieve the existing fragment.
        ((ResultListFragment) mViewPager.getAdapter().instantiateItem(mViewPager, Tab.RHYMER.ordinal())).query(word);
        ((ResultListFragment) mViewPager.getAdapter().instantiateItem(mViewPager, Tab.THESAURUS.ordinal())).query(word);
        if(mViewPager.getCurrentItem() == Tab.READER.ordinal()) mViewPager.setCurrentItem(0);
    }

    public void clearSearchHistory() {
        mSuggestionsCursorAdapter.clear();
    }

    private final SearchView.OnQueryTextListener mOnQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            mSuggestionsCursorAdapter.addSuggestion(query.trim().toLowerCase(Locale.US));
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private final SearchView.OnSuggestionListener mOnSuggestionListener = new SearchView.OnSuggestionListener() {
        @Override
        public boolean onSuggestionSelect(int position) {
            String suggestion = mSuggestionsCursorAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, false);
            return false;
        }

        @Override
        public boolean onSuggestionClick(int position) {
            String suggestion = mSuggestionsCursorAdapter.getSuggestion(position);
            mSearchView.setQuery(suggestion, false);
            return false;
        }
    };
}
