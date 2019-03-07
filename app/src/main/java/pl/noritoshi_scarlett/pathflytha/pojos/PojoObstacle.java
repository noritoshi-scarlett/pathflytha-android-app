package pl.noritoshi_scarlett.pathflytha.pojos;

import com.google.maps.android.geometry.Point;

import java.util.ArrayList;
import java.util.List;

import pl.noritoshi_scarlett.pathflytha.algorithm_utilities.geo_objects.GraphConnector;

public class PojoObstacle {

    private int item_obs_id;
    private float item_obs_latitude;
    private float item_obs_longitude;
    private int item_obs_x;
    private int item_obs_y;
    private int item_obs_height;
    private int item_obs_elevation;
    private int item_obs_name_id;
    private String item_obs_name;
    private float item_obs_range;
    private boolean isChecked = false;
    private boolean isSelected = false;
    private boolean isTooClose = false;
    private Point point;

    public Point getPoint() {
        if (point == null) {
            point = new Point(item_obs_x, item_obs_y);
        }
        return point;
    }

    public boolean isChecked() { return isChecked; }
    public void setChecked(boolean checked) { isChecked = checked; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }

    public boolean isTooClose() { return isTooClose; }
    public void setTooClose(boolean tooClose) { isTooClose = tooClose; }

    public int getItem_obs_id() {return item_obs_id;}
    public void setItem_obs_id(int item_obs_id) {this.item_obs_id = item_obs_id;}

    public float getItem_obs_latitude() {return item_obs_latitude;}
    public void setItem_obs_latitude(float item_obs_latitude) {this.item_obs_latitude = item_obs_latitude;}

    public float getItem_obs_longitude() {return item_obs_longitude;}
    public void setItem_obs_longitude(float item_obs_longitude) {this.item_obs_longitude = item_obs_longitude;}

    public int getItem_obs_x() {return item_obs_x;}
    public void setItem_obs_x(int item_obs_x) {this.item_obs_x = item_obs_x;}

    public int getItem_obs_y() {return item_obs_y;}
    public void setItem_obs_y(int item_obs_y) {this.item_obs_y = item_obs_y;}

    public int getItem_obs_height() {return item_obs_height;}
    public void setItem_obs_height(int item_obs_height) {this.item_obs_height = item_obs_height;}

    public int getItem_obs_elevation() {return item_obs_elevation;}
    public void setItem_obs_elevation(int item_obs_elevation) {this.item_obs_elevation = item_obs_elevation;}

    public int getItem_obs_name_id() {return item_obs_name_id;}
    public void setItem_obs_name_id(int item_obs_name_id) {this.item_obs_name_id = item_obs_name_id;}

    public String getItem_obs_name() {return item_obs_name;}
    public void setItem_obs_name(String item_obs_name) {this.item_obs_name = item_obs_name;}

    public float getItem_obs_range() { return item_obs_range; }
    public void setItem_obs_range(float item_obs_range) { this.item_obs_range = item_obs_range; }
}
