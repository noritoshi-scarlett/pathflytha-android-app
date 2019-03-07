package pl.noritoshi_scarlett.pathflytha.fragments_main;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import pl.noritoshi_scarlett.pathflytha.pojos.PojoObstacle;
import pl.noritoshi_scarlett.pathflytha.R;
import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.small_quests.LatLongConverter;

public class ObstaclesListRecyclerViewAdapter extends
        RecyclerView.Adapter<ObstaclesListRecyclerViewAdapter.ViewHolder> {

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView item_obs_latitude;
        private TextView item_obs_longitude;
        private TextView item_obs_height;
        private TextView item_obs_elevation;
        private ImageView item_obs_image;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            item_obs_latitude = itemView.findViewById(R.id.item_obs_latitude);
            item_obs_longitude = itemView.findViewById(R.id.item_obs_longitude);
            item_obs_height = itemView.findViewById(R.id.item_obs_height);
            item_obs_elevation = itemView.findViewById(R.id.item_obs_elevation);
            item_obs_image = itemView.findViewById(R.id.item_obs_image);
        }
    }

    private Context mContext;
    private ArrayList<PojoObstacle> mObstances;
    private SparseBooleanArray selectedItems;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void setOnItemClickListener(View view, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public ObstaclesListRecyclerViewAdapter(Context context, ArrayList<PojoObstacle> obstances) {
        mContext = context;
        mObstances = obstances;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the custom layout
        View view = inflater.inflate(R.layout.item_recycler_obstances_list, parent, false);
        // Return a new holder instance
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        float obs_latitude = mObstances.get(position).getItem_obs_latitude();
        float obs_longitude = mObstances.get(position).getItem_obs_longitude();
        float obs_height = mObstances.get(position).getItem_obs_height();
        float obs_elevation = mObstances.get(position).getItem_obs_elevation();
        String obs_name = mObstances.get(position).getItem_obs_name();

        TextView item_latitude = holder.item_obs_latitude;
        TextView item_longitude = holder.item_obs_longitude;
        TextView item_height = holder.item_obs_height;
        TextView item_elevation = holder.item_obs_elevation;
        ImageView item_view = holder.item_obs_image;

        item_latitude.setText(LatLongConverter.convertLatitudeToDegrees(obs_latitude));
        item_longitude.setText(LatLongConverter.convertLongitudeToDegrees(obs_longitude));
        item_height.setText(String.format("%s m", String.valueOf(obs_height)));
        item_elevation.setText(String.format("%s m n.p.m.", String.valueOf(obs_elevation)));

        holder.itemView.setBackgroundColor(
                selectedItems.get(position, false)
                        ? mContext.getResources().getColor(R.color.buttonPrimaryActive)
                        : Color.TRANSPARENT);

        int idImage = R.drawable.icon_obstacle_other_48;
        switch (obs_name) {
            case "Church with tower":
            case "Church with spire":
                idImage = R.drawable.icon_obstacle_church_48;
                break;
            case "Chimney":
                idImage = R.drawable.icon_obstacle_chimney_48;
                break;
            case "Wind-power station":
                idImage = R.drawable.icon_obstacle_wind_power_48;
                break;
            case "Pylon":
            case "Bridge pylon":
                idImage = R.drawable.icon_obstacle_pylon_48;
                break;
            case "Mast":
            case "Measuring mast":
            case "Meteorological tower":
                idImage = R.drawable.icon_obstacle_radio_tower_48;
                break;
            case "Power line pylon":
                idImage = R.drawable.icon_obstacle_transmission_tower_48;
                break;
            case "Building":
            case "Building with mast":
            case "Building with antenna":
            case "Building with masts":
            case "Building with spire":
                idImage = R.drawable.icon_obstacle_building_48;
                break;
            case "Cooling tower":
                idImage = R.drawable.icon_obstacle_power_plant_48;
                break;
            case "Mobile crane":
                idImage = R.drawable.icon_obstacle_crane_48;
                break;
            case "Power plant chimney":
            case "Power plant chimney with antenna":
            case "Power plant chimney - Pabianice":
            case "Flare stack with flame":
            case "Flare":
            case "Tower":
                idImage = R.drawable.icon_obstacle_other_48;
                break;

        }
        item_view.setImageDrawable(mContext.getDrawable(idImage));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.setOnItemClickListener(v, holder.getAdapterPosition());
                selectItem(holder.getAdapterPosition());
            }
        });
    }

    public void selectItem(int pos){
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mObstances.isEmpty()) {
            return 0;
        } else {
            return mObstances.size();
        }
    }
}
