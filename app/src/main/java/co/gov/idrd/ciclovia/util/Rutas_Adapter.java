package co.gov.idrd.ciclovia.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by daniel on 2/01/18.
 */

    public class Rutas_Adapter extends ArrayAdapter<Datamodel_rutas> implements View.OnClickListener{

        private ArrayList<Datamodel_rutas> dataSet;
        Context mContext;

        // View lookup cache
        private static class ViewHolder {
            TextView txtFecha;
            TextView txtMedio;
            //ImageView info;
        }

        public CustomAdapter(ArrayList<Datamodel_rutas> data, Context context) {
            super(context, R.layout.row_item, data);
            this.dataSet = data;
            this.mContext=context;

        }

        @Override
        public void onClick(View v) {

            int position=(Integer) v.getTag();
            Object object= getItem(position);
            Datamodel_rutas dataModel=(Datamodel_rutas)object;

            switch (v.getId())
            {
                case R.id.item_info:
                    Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
                            .setAction("No action", null).show();
                    break;
            }
        }

        private int lastPosition = -1;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Datamodel_rutas dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.row_item, parent, false);
                viewHolder.txtName = (TextView) convertView.findViewById(R.id.name);
                viewHolder.txtType = (TextView) convertView.findViewById(R.id.type);
                viewHolder.txtVersion = (TextView) convertView.findViewById(R.id.version_number);
                viewHolder.info = (ImageView) convertView.findViewById(R.id.item_info);

                result=convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.txtName.setText(dataModel.getName());
            viewHolder.txtType.setText(dataModel.getType());
            viewHolder.txtVersion.setText(dataModel.getVersion_number());
            viewHolder.info.setOnClickListener(this);
            viewHolder.info.setTag(position);
            // Return the completed view to render on screen
            return convertView;
        }

}
