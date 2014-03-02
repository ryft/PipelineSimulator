package uk.co.ryft.pipeline.ui.setup;

import android.content.Context;

public class GLConfig<E> {

    public final E[] mValues;
    public final String[] mNames;
    public final String[] mDescriptions;

    public GLConfig(E[] values, String[] names, String[] descriptions) {
        assert (values.length == names.length && values.length == descriptions.length);

        mValues = values;
        mNames = names;
        mDescriptions = descriptions;
    }

    public GLConfig(Context context, E[] values, String[] names, int[] descriptions) {
        assert (values.length == names.length && values.length == descriptions.length);

        mValues = values;
        mNames = names;
        String[] strDescriptions = new String[descriptions.length];
        for (int i = 0; i < descriptions.length; i++)
            strDescriptions[i] = context.getString(descriptions[i]);
        mDescriptions = strDescriptions;
    }

}
