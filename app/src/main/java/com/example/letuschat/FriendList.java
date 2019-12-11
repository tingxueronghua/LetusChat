package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FriendList extends AppCompatActivity {
    public ListView friend_listview;
    private String id_number;
    public FriendMsgAdapter adapter;
    public List<Friend_Msg> friend_msgList = new ArrayList<Friend_Msg>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Intent intent = getIntent();
        friend_msgList.add(new Friend_Msg("test_list", 1));
        id_number = intent.getStringExtra("id");
        adapter = new FriendMsgAdapter(FriendList.this, R.layout.chat_alone, friend_msgList);
        friend_listview = findViewById(R.id.list_view2);
        friend_listview.setAdapter(adapter);
    }

    public void add_friend_msg(View v)
    {
//        TODO: add the friend data into the sqlite dataset.
        EditText edittext = findViewById(R.id.editText2);
        String content = edittext.getText().toString();
        if(content.equals(""))
            return;
        FriendList.Friend_Msg msg=new FriendList.Friend_Msg(content, 1);
        friend_msgList.add(msg);
        adapter.notifyDataSetChanged();
        friend_listview.setSelection(friend_msgList.size());
        edittext.setText("");
    }
    public class Friend_Msg
    {
        public String content;
        public int type;
        public Friend_Msg(String content, int type){
            this.content = content;
            this.type = type;
        }
    }

    public class FriendMsgAdapter extends ArrayAdapter<FriendList.Friend_Msg> {
        public int resourceId;
        public FriendMsgAdapter(Context context, int textViewId, List<FriendList.Friend_Msg> objects){
            super(context, textViewId, objects);
            resourceId = textViewId;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            FriendList.Friend_Msg msg = getItem(position);
            View view;
            FriendList.FriendMsgAdapter.ViewHolder viewHolder;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.leftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
                viewHolder.rightLayout = (LinearLayout)view.findViewById(R.id.right_layout);
                viewHolder.blankLayout = (LinearLayout)view.findViewById(R.id.blank_layout);
                viewHolder.blankMsg = (TextView)view.findViewById(R.id.blank_msg);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (FriendList.FriendMsgAdapter.ViewHolder) view.getTag();
            }
            viewHolder.blankLayout.setVisibility(View.VISIBLE);
            viewHolder.blankMsg.setText(msg.content);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.GONE);

            return view;
        }
        public class ViewHolder{
            LinearLayout leftLayout;
            LinearLayout rightLayout;
            LinearLayout blankLayout;
            TextView blankMsg;
            TextView leftMsg;
            TextView rightMsg;
        }
    }
}
