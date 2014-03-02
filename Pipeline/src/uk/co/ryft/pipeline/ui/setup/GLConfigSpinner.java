package uk.co.ryft.pipeline.ui.setup;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import uk.co.ryft.pipeline.R;

public class GLConfigSpinner extends Spinner {
    
    public GLConfigSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}

class GLConfigAdapter<E> extends ArrayAdapter<E> {

    final LayoutInflater mInflater;
    final GLConfig<E> mConfig;
    
    // View holder stores references to the view components
    static class ViewHolder {
        TextView typeView;
    }

    public GLConfigAdapter(Context context, int textViewResourceId, GLConfig<E> config) {
        super(context, textViewResourceId, config.mValues);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mConfig = config;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder viewHolder;

        // Recycle view if possible
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_config_selection, null);

            // Implement the View Holder pattern
            viewHolder = new ViewHolder();
            viewHolder.typeView = (TextView) convertView.findViewById(R.id.spinner_config_selection);
            convertView.setTag(viewHolder);
            
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        TextView typeView = viewHolder.typeView;
        typeView.setText(mConfig.mNames[position]);

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        // Recycle view if possible
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_config_item, null);
        }

        TextView typeView = (TextView) convertView.findViewById(R.id.spinner_config_item_name);
        TextView descView = (TextView) convertView.findViewById(R.id.spinner_config_item_value);

        typeView.setText(mConfig.mNames[position]);
        descView.setText(String.valueOf(mConfig.mValues[position]));

        return convertView;
    }
}