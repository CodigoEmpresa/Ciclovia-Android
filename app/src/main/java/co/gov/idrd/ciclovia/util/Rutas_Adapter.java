package co.gov.idrd.ciclovia.util;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import co.gov.idrd.ciclovia.R;

/**
 * Created by daniel on 2/01/18.
 */

    public class Rutas_Adapter extends ArrayAdapter<Datamodel_rutas> implements View.OnClickListener{

        private ArrayList<Datamodel_rutas> dataSet;
        Context mContext;

        private static class ViewHolder {
            TextView txtFecha;
            TextView txtMedio;
            ImageView imagen;
        }

        public Rutas_Adapter(ArrayList<Datamodel_rutas> data, Context context) {
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
                case R.id.fecha:
                    Snackbar.make(v, "Fecha de ruta " +dataModel.getfecha(), Snackbar.LENGTH_LONG)
                            .setAction("Si acción", null).show();
                    break;
            }
        }

        private int lastPosition = -1;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Datamodel_rutas dataModel = getItem(position);

            ViewHolder viewHolder;

            final View result;

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.row_item, parent, false);
                viewHolder.txtFecha = (TextView) convertView.findViewById(R.id.fecha);
                viewHolder.txtMedio = (TextView) convertView.findViewById(R.id.medio);
                viewHolder.imagen = (ImageView) convertView.findViewById(R.id.imagen);

                result=convertView;

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                result=convertView;
            }

            Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
            result.startAnimation(animation);
            lastPosition = position;

            viewHolder.txtFecha.setText(dataModel.getfecha());
            viewHolder.txtMedio.setText(dataModel.getmedio());
            viewHolder.imagen.setOnClickListener(this);
            viewHolder.imagen.setTag(position);
            return convertView;
        }

}
