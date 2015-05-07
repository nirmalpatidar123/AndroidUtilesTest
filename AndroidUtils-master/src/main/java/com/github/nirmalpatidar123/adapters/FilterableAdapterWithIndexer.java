package com.github.nirmalpatidar123.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Comparator;
import java.util.Collections;

import com.github.nirmalpatidar123.utils.StringMatcher;

public abstract class FilterableAdapterWithIndexer extends BaseAdapter
		implements Filterable, SectionIndexer {
	/**
	 * Contains the list of objects that represent the data of this
	 * ArrayAdapter. The content of this list is referred to as "the array" in
	 * the documentation.
	 */
	private List<HashMap<String, String>> mObjects;

	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is
	 * also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	private final Object mLock = new Object();

	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called
	 * whenever {@link #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;

	private Context mContext;

	private ArrayList<HashMap<String, String>> mOriginalValues;
	private ArrayFilter mFilter;

	private String hashMapKeyForFiltering, hashMapKeyForIndexing;
	private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public FilterableAdapterWithIndexer(Context context,
			List<HashMap<String, String>> objects,
			final String hashMapKeyForFiltering,
			final String hashMapKeyForIndexing) {
		mContext = context;
		this.hashMapKeyForFiltering = hashMapKeyForFiltering;
		this.hashMapKeyForIndexing = hashMapKeyForIndexing;
		mObjects = objects;
	}

	/**
	 * Adds the specified object at the end of the array.
	 * 
	 * @param object
	 *            The object to add at the end of the array.
	 */
	public void add(HashMap<String, String> object) {
		if (mOriginalValues != null) {
			synchronized (mLock) {
				mOriginalValues.add(object);
				if (mNotifyOnChange)
					notifyDataSetChanged();
			}
		} else {
			mObjects.add(object);
			if (mNotifyOnChange)
				notifyDataSetChanged();
		}
	}

	/**
	 * Inserts the specified object at the specified index in the array.
	 * 
	 * @param object
	 *            The object to insert into the array.
	 * @param index
	 *            The index at which the object must be inserted.
	 */
	public void insert(HashMap<String, String> object, int index) {
		if (mOriginalValues != null) {
			synchronized (mLock) {
				mOriginalValues.add(index, object);
				if (mNotifyOnChange)
					notifyDataSetChanged();
			}
		} else {
			mObjects.add(index, object);
			if (mNotifyOnChange)
				notifyDataSetChanged();
		}
	}

	/**
	 * Removes the specified object from the array.
	 * 
	 * @param object
	 *            The object to remove.
	 */
	public void remove(HashMap<String, String> object) {
		if (mOriginalValues != null) {
			synchronized (mLock) {
				mOriginalValues.remove(object);
			}
		} else {
			mObjects.remove(object);
		}
		if (mNotifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		if (mOriginalValues != null) {
			synchronized (mLock) {
				mOriginalValues.clear();
			}
		} else {
			mObjects.clear();
		}
		if (mNotifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 * 
	 * @param comparator
	 *            The comparator used to sort the objects contained in this
	 *            adapter.
	 */
	public void sort(Comparator<? super HashMap<String, String>> comparator) {
		Collections.sort(mObjects, comparator);
		if (mNotifyOnChange)
			notifyDataSetChanged();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Control whether methods that change the list ({@link #add},
	 * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
	 * {@link #notifyDataSetChanged}. If set to false, caller must manually call
	 * notifyDataSetChanged() to have the changes reflected in the attached
	 * view.
	 * 
	 * The default is true, and calling notifyDataSetChanged() resets the flag
	 * to true.
	 * 
	 * @param notifyOnChange
	 *            if true, modifications to the list will automatically call
	 *            {@link #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 * 
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCount() {
		return mObjects.size();
	}

	/**
	 * {@inheritDoc}
	 */
	public HashMap<String, String> getItem(int position) {
		return mObjects.get(position);
	}

	/**
	 * Returns the position of the specified item in the array.
	 * 
	 * @param item
	 *            The item to retrieve the position of.
	 * 
	 * @return The position of the specified item.
	 */
	public int getPosition(HashMap<String, String> item) {
		return mObjects.indexOf(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getItemId(int position) {
		return position;
	}

	// /**
	// * {@inheritDoc}
	// */
	// public View getView(int position, View convertView, ViewGroup parent) {
	//
	// HashMap<String,String> libMap = mObjects.get(position);
	//
	// View view = mInflater.inflate(R.layout.layout_strainlib_list_row, null);
	//
	// TextView strainNameTv = (TextView) view
	// .findViewById(R.id.strainNameTv);
	// TextView speciesTv = (TextView) view.findViewById(R.id.speciesTv);
	// TextView reviewsCountTv = (TextView) view
	// .findViewById(R.id.reviewsCountTv);
	//
	// strainNameTv.setText(libMap.get(KEYS.STRAIN_LIB.STRAIN_NAME));
	// speciesTv.setText(libMap.get(KEYS.STRAIN_LIB.SPECIES));
	// String reviewsCount = libMap.get(KEYS.STRAIN_LIB.REVIEW_COUNT);
	// reviewsCountTv.setText("Reviews["+reviewsCount+"]");
	//
	// view.setTag(position);
	// return view;
	// }

	/**
	 * {@inheritDoc}
	 */
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * <p>
	 * An array filter constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	private class ArrayFilter extends Filter {
		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<HashMap<String, String>>(
							mObjects);
				}
			}

			if (prefix == null || prefix.length() == 0) {
				synchronized (mLock) {
					ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>(
							mOriginalValues);
					results.values = list;
					results.count = list.size();
				}
			} else {
				String prefixString = prefix.toString().toLowerCase();

				final ArrayList<HashMap<String, String>> values = mOriginalValues;
				final int count = values.size();

				final ArrayList<HashMap<String, String>> newValues = new ArrayList<HashMap<String, String>>(
						count);

				for (int i = 0; i < count; i++) {
					final HashMap<String, String> value = values.get(i);
					final String valueText = value.get(hashMapKeyForFiltering);

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					}

					// In Strain Library client dont want internal search
					// else {
					// final String[] words = valueText.split(" ");
					// final int wordCount = words.length;
					//
					// for (int k = 0; k < wordCount; k++) {
					// if (words[k].startsWith(prefixString)) {
					// newValues.add(value);
					// break;
					// }
					// }
					// }
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mObjects = (List<HashMap<String, String>>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	@Override
	public int getPositionForSection(int section) {
		// If there is no item for current section, previous section will be
		// selected
		int length = mObjects.size();
		for (int i = section; i >= 0; i--) {
			for (int j = 0; j < length; j++) {
				if (i == 0) {
					// For numeric section
					for (int k = 0; k <= 9; k++) {
						String strainName = mObjects.get(j).get(
								hashMapKeyForIndexing);
						if (StringMatcher.match(
                                String.valueOf(strainName.charAt(0)),
                                String.valueOf(k)))
							return j;
					}
				} else {
					String strainName = mObjects.get(j).get(
							hashMapKeyForIndexing);
					if (StringMatcher.match(
							String.valueOf(strainName.charAt(0)),
							String.valueOf(mSections.charAt(i))))
						return j;
				}
			}
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int arg0) {
		return 0;
	}

	@Override
	public Object[] getSections() {
		String[] sections = new String[mSections.length()];
		for (int i = 0; i < mSections.length(); i++)
			sections[i] = String.valueOf(mSections.charAt(i));
		return sections;
	}
}
