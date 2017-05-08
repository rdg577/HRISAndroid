package helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import ph.gov.davaodelnorte.hris.R;

/**
 * Created by Reden D. Gallera on 14/03/2017.
 */

public class SwipeListAdapter extends BaseAdapter {
    private final Activity activity;
    private LayoutInflater inflater;
    private final List<Menu> menuList;
    private final String[] bgColors;

    public SwipeListAdapter(Activity activity, List<Menu> menuList) {
        this.activity = activity;
        this.menuList = menuList;
        bgColors = activity.getApplicationContext().getResources().getStringArray(R.array.menu_bg_colors);
    }

    @Override
    public int getCount() {
        return menuList.size();
    }

    @Override
    public Object getItem(int location) {
        return menuList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.menu_item, parent,false);

        TextView serial = (TextView) convertView.findViewById(R.id.serial);
        TextView title = (TextView) convertView.findViewById(R.id.title);

        serial.setText(String.valueOf(menuList.get(position).getTotalApplications()));
        title.setText(menuList.get(position).getTitle());

        String color = bgColors[position % bgColors.length];
        serial.setBackgroundColor(Color.parseColor(color));

        return convertView;
    }
}
