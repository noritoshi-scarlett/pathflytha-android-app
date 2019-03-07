package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.pojos.PojoPatches;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;

public class PatchesRecyclerViewAdapter extends
        RecyclerView.Adapter<PatchesRecyclerViewAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView item_patch_title;
        private TextView item_patch_date;
        private TextView item_patch_start;
        private TextView item_patch_end;
        private TextView item_patch_distance;
        private TextView item_patch_obstacles_count;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            item_patch_title = itemView.findViewById(R.id.item_patch_title);
            item_patch_date = itemView.findViewById(R.id.item_patch_date);
            item_patch_start = itemView.findViewById(R.id.item_patch_start);
            item_patch_end = itemView.findViewById(R.id.item_patch_end);
            item_patch_distance = itemView.findViewById(R.id.item_patch_distance);
            item_patch_obstacles_count = itemView.findViewById(R.id.item_patch_obstacles_count);

        }
    }

    private Context mContext;
    private ArrayList<PojoPatches> mPatches;

    public PatchesRecyclerViewAdapter(Context context, ArrayList<PojoPatches> patches) {
        mPatches = patches;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_recycler_obstances_list, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String patch_title = mPatches.get(position).getItem_patch_title();
        String patch_date = mPatches.get(position).getItem_patch_date();
        float patch_start_lat = mPatches.get(position).getItem_patch_start_lat();
        float patch_start_long = mPatches.get(position).getItem_patch_start_long();
        float patch_start_out_lat = mPatches.get(position).getItem_patch_start_out_lat();
        float patch_start_out_long = mPatches.get(position).getItem_patch_start_out_long();
        float patch_end_target_lat = mPatches.get(position).getItem_patch_end_target_lat();
        float patch_end_target_long = mPatches.get(position).getItem_patch_end_target_long();
        float patch_end_lat = mPatches.get(position).getItem_patch_end_lat();
        float patch_end_long = mPatches.get(position).getItem_patch_end_long();
         float patch_distance = mPatches.get(position).getItem_patch_distance();
        float patch_obstacles_count = mPatches.get(position).getItem_patch_obstacles_count();

        String[] textPickedCoordinates = LatLongConverter.writeAsString(
                mContext,
                new LatLng(patch_start_lat, patch_start_long),
                new LatLng(patch_start_out_lat, patch_start_out_long),
                new LatLng(patch_end_target_lat, patch_end_target_long),
                new LatLng(patch_end_lat, patch_end_long));

        String patch_start = textPickedCoordinates[0];
        String patch_end = textPickedCoordinates[1];

        TextView i_patch_title = holder.item_patch_title;
        TextView i_patch_date = holder.item_patch_date;
        TextView i_patch_start = holder.item_patch_start;
        TextView i_patch_end = holder.item_patch_end;
        TextView i_patch_distance = holder.item_patch_distance;
        TextView i_patch_obstacles_count = holder.item_patch_obstacles_count;

        i_patch_title.setText(patch_title);
        i_patch_date.setText(patch_date);
        i_patch_start.setText(patch_start);
        i_patch_end.setText(patch_end);
        i_patch_distance.setText(String.valueOf(patch_distance));
        i_patch_obstacles_count.setText(String.valueOf(patch_obstacles_count));
    }

    @Override
    public int getItemCount() {
        if (mPatches.isEmpty()) {
            return 0;
        } else {
            return mPatches.size();
        }
    }
}
