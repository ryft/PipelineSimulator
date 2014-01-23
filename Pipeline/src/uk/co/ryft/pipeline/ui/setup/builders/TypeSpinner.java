package uk.co.ryft.pipeline.ui.setup.builders;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import uk.co.ryft.pipeline.R;
import uk.co.ryft.pipeline.model.shapes.Primitive;
import uk.co.ryft.pipeline.model.shapes.Primitive.Type;

public class TypeSpinner extends Spinner {
    
    public TypeSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public void setSelection(Type type) {
        
        // Select the specified type in the spinner
        for (int i = 0; i < this.getCount(); i++)
            if (this.getAdapter().getItem(i) == type)
                this.setSelection(i);
    }
}

class TypeSpinnerAdapter extends ArrayAdapter<Type> {

    final LayoutInflater mInflater;
    
    // View holder stores references to the view components
    static class ViewHolder {
        TextView typeView;
    }

    public TypeSpinnerAdapter(Context context, int textViewResourceId, Primitive.Type[] objects) {
        super(context, textViewResourceId, objects);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        
        ViewHolder viewHolder;

        // Recycle view if possible
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_type_selection, null);

            // Implement the View Holder pattern
            viewHolder = new ViewHolder();
            viewHolder.typeView = (TextView) convertView.findViewById(R.id.spinner_type_selection);
            convertView.setTag(viewHolder);
            
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Primitive.Type thisType = getItem(position);
        TextView typeView = viewHolder.typeView;
        typeView.setText(thisType.toString());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        // Recycle view if possible
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spinner_type_item, null);
        }

        Primitive.Type thisType = getItem(position);
        TextView typeView = (TextView) convertView.findViewById(R.id.spinner_type_item);
        TextView descView = (TextView) convertView.findViewById(R.id.spinner_type_item_desc);

        typeView.setText(thisType.toString());
        descView.setText(thisType.getDescription());

        return convertView;
    }
}