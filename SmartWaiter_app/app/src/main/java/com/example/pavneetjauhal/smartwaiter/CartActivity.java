package com.example.pavneetjauhal.smartwaiter;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

public class CartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        popualteCartListView();
    }

    public void popualteCartListView(){
        ArrayAdapter<UserItems> adapter = new CategoryListAdapter();
        ListView list = (ListView) findViewById(R.id.cartList);
        list.setAdapter(adapter);
    }

    public void confirmOrder(View view) {
        Intent intent = new Intent("com.example.pavneetjauhal.smartwaiter.ConfirmOrderActivity");
        startActivity(intent);
    }


    private class CategoryListAdapter extends ArrayAdapter<UserItems> {
        public CategoryListAdapter() {
            super(CartActivity.this, R.layout.cart_view, MainActivity.user.userItems);
        }

        public View getView(int position, View convertView, ViewGroup parent){
            // Make sure we have a view to work with, may have been given null
            View itemView = convertView;
            if(itemView == null){
                itemView = getLayoutInflater().inflate(R.layout.cart_view, parent, false);
            }

            //Find the menu item to work with
            UserItems currentItem = MainActivity.user.userItems.get(position);

            //Fill the view
            //ImageView imageView = (ImageView) itemView.findViewById(R.id.itemIcon);
            //imageView.setImageResource(currentItem.getId());

            //Make text view
            TextView makeText = (TextView) itemView.findViewById(R.id.txtItemName);
            makeText.setText(currentItem.getItemName());

            TextView makeText2 = (TextView) itemView.findViewById(R.id.txtItemPrice);
            makeText2.setText(currentItem.getItemPrice());

            Button btn = (Button)itemView.findViewById(R.id.deleteItem);
            btn.setTag(position);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Integer index = (Integer) v.getTag();
                    MainActivity.user.removeUserItem(index);
                    notifyDataSetChanged();

                }
            });

            return  itemView;
            //return super.getView(position, convertView, parent);
        }

    }
}