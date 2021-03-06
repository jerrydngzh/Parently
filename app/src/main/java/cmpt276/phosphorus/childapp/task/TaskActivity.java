package cmpt276.phosphorus.childapp.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.model.child.Child;
import cmpt276.phosphorus.childapp.model.child.ChildManager;
import cmpt276.phosphorus.childapp.model.data.DataManager;
import cmpt276.phosphorus.childapp.model.data.DataType;
import cmpt276.phosphorus.childapp.model.task.Task;
import cmpt276.phosphorus.childapp.model.task.TaskManager;
import cmpt276.phosphorus.childapp.task.utils.TaskListAdapter;

// ==============================================================================================
//
// Displays all tasks and allows user to add/delete/edit tasks
//
// ==============================================================================================
public class TaskActivity extends AppCompatActivity {

    private TaskManager taskManager;

    private AlertDialog alertDialog;

    public static Intent makeIntent(Context context) {
        return new Intent(context, TaskActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        this.setTitle(getString(R.string.task_activity_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.taskManager = TaskManager.getInstance();

        this.createConfigureTaskBtn();
        this.populateTaskListView();
    }

    // If user select the top left back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.populateTaskListView();
    }

    private void createConfigureTaskBtn() {
        FloatingActionButton addTaskFab = findViewById(R.id.addTaskFab);
        addTaskFab.setOnClickListener(view -> startActivity(ConfigureTaskActivity.makeIntent(this)));
    }

    // https://stackoverflow.com/questions/13341560/how-to-create-a-custom-dialog-box-in-android
    private void displayTaskDialog(Task selected) {
        AlertDialog.Builder taskDialog = new AlertDialog.Builder(this);

        View dialogView = this.getLayoutInflater().inflate(R.layout.task_dialog, null);
        taskDialog.setView(dialogView);

        String dialogTitle = getResources().getString(R.string.task_info_title).replace("%name%", selected.getName());
        TextView textTaskDialogName = dialogView.findViewById(R.id.textTaskDialogName);
        textTaskDialogName.setText(dialogTitle);

        Child currChild = ChildManager.getInstance().getChildByUUID(selected.getCurrentChild());
        ImageView taskChildIcon = dialogView.findViewById(R.id.imgTaskChildIcon);
        if (currChild != null) {
            if (currChild.getChildPortraitPath() != null) {
                Glide.with(this)
                        .load(currChild.getChildPortraitPath())
                        .into(taskChildIcon);
            }
        } else {
            taskChildIcon.setVisibility(View.GONE);
        }

        Button btnTaskComplete = dialogView.findViewById(R.id.btnTaskComplete);
        if (!selected.isEmptyChildList()) {
            btnTaskComplete.setOnClickListener(view -> {
                selected.cycleChildren();
                DataManager.getInstance(this).saveData(DataType.TASKS);
                this.populateTaskListView();
                this.alertDialog.dismiss();
            });
        } else {
            btnTaskComplete.setVisibility(View.GONE);
        }

        Button btnTaskEdit = dialogView.findViewById(R.id.btnTaskEdit);
        btnTaskEdit.setOnClickListener(view -> {
            this.alertDialog.dismiss();
            startActivity(ConfigureTaskActivity.makeIntent(this, selected));
        });

        Button btnTaskClose = dialogView.findViewById(R.id.btnTaskClose);
        btnTaskClose.setOnClickListener(view -> this.alertDialog.dismiss());

        Button viewHistory = dialogView.findViewById(R.id.viewHistory);
        viewHistory.setOnClickListener(v -> {
            Intent taskHist = TaskHistoryActivity.makeIntent(this, selected);
            startActivity(taskHist);
        });

        TextView textCurrentTurn = dialogView.findViewById(R.id.textCurrentTurn);
        String childName = (currChild == null)
                ? getString(R.string.child_not_available)
                : currChild.getName();

        String currentChild = getResources().getString(R.string.task_info_current_child).replace("%name%", childName);
        textCurrentTurn.setText(currentChild);

        this.alertDialog = taskDialog.create();
        this.alertDialog.show();
    }

    private void populateTaskListView() {
        ListView listView = findViewById(R.id.taskListView);
        ListAdapter listAdapter = new TaskListAdapter(this, taskManager.getAllTasks());
        listView.setOnItemClickListener((adapter, view, position, arg) -> {
            Task selectedTask = TaskManager.getInstance()
                    .getAllTasks()
                    .get(position);

            displayTaskDialog(selectedTask);
        });
        listView.setAdapter(listAdapter);
    }

}