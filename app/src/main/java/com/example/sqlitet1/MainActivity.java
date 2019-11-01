/*
package com.example.sqlitet1;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}
*/
package com.example.sqlitet1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sqlitet1.R;

public class MainActivity extends Activity {
    private EditText username, password;
    private SQLiteDatabase DB;

    private ListView values;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.userpwd);
        values = (ListView) findViewById(R.id.values_list);
        // 获取SQLiteDatabase以操作SQL语句
        DB = SQLiteDatabase.openOrCreateDatabase(getFilesDir() + "/info.db",
                null);
        // 长按删除
        values.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {
                // 获取所点击项的_id
                TextView tv = (TextView) arg1.findViewById(R.id.tv_id);
                final String id = tv.getText().toString();
                // 通过Dialog提示是否删除
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);
                builder.setMessage("确定要删除吗？");
                // 确定按钮点击事件
                builder.setPositiveButton("确定", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(id);
                        replaceList();// 删除后刷新列表
                    }
                });
                // 取消按钮点击事件
                builder.setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

                return true;
            }
        });
        // 点击更新
        values.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // 获取_id,username,password项
                TextView tvId = (TextView) arg1.findViewById(R.id.tv_id);
                TextView tvName = (TextView) arg1
                        .findViewById(R.id.tv_username);
                TextView tvPass = (TextView) arg1
                        .findViewById(R.id.tv_password);
                final String id = tvId.getText().toString();
                String username = tvName.getText().toString();
                String password = tvPass.getText().toString();
                // 通过Dialog弹出修改界面
                AlertDialog.Builder builder = new Builder(MainActivity.this);
                builder.setTitle("修改");
                // 自定义界面包括两个文本输入框
                View v = View.inflate(MainActivity.this, R.layout.alertdialog,
                        null);
                final EditText etName = (EditText) v
                        .findViewById(R.id.alert_name);
                final EditText etPass = (EditText) v
                        .findViewById(R.id.alert_pass);
                // Dialog弹出就显示原内容
                etName.setText(username);
                etPass.setText(password);

                builder.setView(v);
                // 确定按钮点击事件
                builder.setPositiveButton("保存", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = etName.getText().toString();
                        String newPass = etPass.getText().toString();
                        updata(newName, newPass, id);
                        replaceList();// 更新后刷新列表
                    }
                });
                // 取消按钮点击事件
                builder.setNegativeButton("取消", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    /**
     * 关闭程序关闭数据库
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        DB.close();
    }

    /**
     * 保存按钮点击事件，首次插入由于没有表必然报错，简化程序利用try-catch在catch中创建表
     */
    public void save(View v) {
        String name = username.getText().toString();
        String pass = password.getText().toString();
        try {
            insert(name, pass);
        } catch (Exception e) {
            create();
            insert(name, pass);
        }
        Toast.makeText(this, "Save Success", Toast.LENGTH_SHORT).show();
        username.setText("");
        password.setText("");
    }

    /**
     * 读取按钮点击事件，以列表的形式显示所有内容
     */
    public void read(View v) {
        replaceList();
    }

    /**
     * ListView的适配器
     */
    public void replaceList() {
        Cursor cursor = select();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.values_item, cursor, new String[] { "_id", "username",
                "password" }, new int[] { R.id.tv_id, R.id.tv_username,
                R.id.tv_password },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        values.setAdapter(adapter);
    }

    /**
     * 建表
     */
    public void create() {
        String createSql = "create table user(_id integer primary key autoincrement,username,password)";
        DB.execSQL(createSql);
    }

    /**
     * 插入
     */
    public void insert(String username, String password) {
        String insertSql = "insert into user(username,password) values(?,?)";
        DB.execSQL(insertSql, new String[] { username, password });
    }

    /**
     * 查询
     */
    public Cursor select() {
        String selectSql = "select _id,username,password from user";
        Cursor cursor = DB.rawQuery(selectSql, null);// 我们需要查处所有项故不需要查询条件
        return cursor;
    }

    /**
     * 删除
     */
    public void delete(String id) {
        String deleteSql = "delete from user where _id=?";
        DB.execSQL(deleteSql, new String[] { id });
    }

    /**
     * 更新
     */
    public void updata(String username, String password, String id) {
        String updataSql = "update user set username=?,password=? where _id=?";
        DB.execSQL(updataSql, new String[] { username, password, id });
    }
}
