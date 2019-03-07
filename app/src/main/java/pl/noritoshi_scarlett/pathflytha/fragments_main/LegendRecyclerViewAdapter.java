package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;
import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;

public class LegendRecyclerViewAdapter extends
        RecyclerView.Adapter<LegendRecyclerViewAdapter.ViewHolder> {

    private Context context;

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private ImageView item_image;
        private TextView item_title;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            item_image = itemView.findViewById(R.id.rectColor);
            item_title = itemView.findViewById(R.id.text);
        }
    }

    private List<Pair<Integer, String>> mLegendItems;

    public LegendRecyclerViewAdapter(Context context, List<Pair<Integer, String>> legendItems) {
        this.context = context;
        mLegendItems = legendItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_legend, parent, false);
        // Return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        Integer color = ContextCompat.getColor(context, (mLegendItems.get(position).first));
        String title = mLegendItems.get(position).second;
        ImageView i_color = holder.item_image;
        TextView i_title = holder.item_title;
        i_color.setBackgroundColor(color);
        i_title.setText(title);

    }

    @Override
    public int getItemCount() {
        if (mLegendItems.isEmpty()) {
            return 0;
        } else {
            return mLegendItems.size();
        }
    }



}
