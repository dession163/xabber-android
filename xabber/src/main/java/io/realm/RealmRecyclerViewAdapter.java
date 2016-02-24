package io.realm;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

public abstract class RealmRecyclerViewAdapter<T extends RealmObject, VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {
    protected LayoutInflater inflater;
    protected RealmResults<T> realmResults;
    protected Context context;
    private final RealmChangeListener listener;

    public RealmRecyclerViewAdapter(Context context, RealmResults<T> realmResults, boolean automaticUpdate) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
        this.realmResults = realmResults;
        this.inflater = LayoutInflater.from(context);
        this.listener = (!automaticUpdate) ? null : new RealmChangeListener() {
            @Override
            public void onChange() {
                RealmRecyclerViewAdapter.this.onChange();
            }
        };

        if (listener != null && realmResults != null) {
            this.realmResults.addChangeListener(listener);
        }
    }

    public void onChange() {
        notifyDataSetChanged();
    }

    public void release() {
        if (listener != null && realmResults != null) {
            realmResults.removeChangeListener(listener);
        }
    }

    /**
     * Returns how many items are in the data set.
     *
     * @return count of items.
     */
    @Override
    public int getItemCount() {
        if (realmResults == null) {
            return 0;
        }
        return realmResults.size();
    }

    /**
     * Returns the item associated with the specified position.
     *
     * @param i index of item whose data we want.
     * @return the item at the specified position.
     */
    public T getItem(int i) {
        if (realmResults == null) {
            return null;
        }
        return realmResults.get(i);
    }

    /**
     * Returns the current ID for an item. Note that item IDs are not stable so you cannot rely on the item ID being the
     * same after {@link #notifyDataSetChanged()} or {@link #updateRealmResults(RealmResults)} has been called.
     *
     * @param i index of item in the adapter.
     * @return current item ID.
     */
    @Override
    public long getItemId(int i) {
        // TODO: find better solution once we have unique IDs
        return i;
    }

    /**
     * Updates the RealmResults associated to the Adapter. Useful when the query has been changed.
     * If the query does not change you might consider using the automaticUpdate feature.
     *
     * @param queryResults the new RealmResults coming from the new query.
     */
    public void updateRealmResults(RealmResults<T> queryResults) {
        if (listener != null) {
            // Making sure that Adapter is refreshed correctly if new RealmResults come from another Realm
            if (this.realmResults != null) {
                this.realmResults.realm.removeChangeListener(listener);
            }
            if (queryResults != null) {
                queryResults.realm.addChangeListener(listener);
            }
        }

        this.realmResults = queryResults;
        notifyDataSetChanged();
    }

    public void addChangeListenerAsWeakReference(RealmChangeListener realmChangeListener) {
        if(realmResults != null) {
            realmResults.realm.handlerController.addChangeListenerAsWeakReference(realmChangeListener);
        }
    }
}