package ru.ifmo.md.lesson5;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * Created by Svet on 21.10.2014.
 */
public class EditResourceDialog extends Dialog {
    ListView listView;
    Context context;
    DataStorage data;
    ListView lv;
    ListAdapter listAdapter;
    EditListAdapter adapter;
    public EditResourceDialog(Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_edit);
        listView = (ListView) findViewById(R.id.edit_list_view);
        Button delete = (Button) findViewById(R.id.deleting);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteAll();
                dismiss();
            }
        });
        setTitle("Your resources");
    }

    public void prepare(DataStorage data, ListView lv, ListAdapter listAdapter) {
        this.data = data;
        this.lv = lv;
        this.listAdapter = listAdapter;

        adapter = new EditListAdapter(context, data);
        listView.setAdapter(adapter);

        addListeners();
    }

    private void addListeners() {
        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String name = data.readResourceData().get(position).name;
                data.deleteResource(name);
                adapter.notifyDataSetChanged();
                listView.setAdapter(adapter);
                return true;
            }
        });
    }

    public void deleteAll() {
        data.deleteTables();
        listAdapter.notifyDataSetChanged();
        lv.setAdapter(listAdapter);
    }
}
