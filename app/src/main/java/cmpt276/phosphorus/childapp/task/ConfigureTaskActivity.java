package cmpt276.phosphorus.childapp.task;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.model.ChildManager;
import cmpt276.phosphorus.childapp.model.DataManager;
import cmpt276.phosphorus.childapp.model.DataType;
import cmpt276.phosphorus.childapp.model.Task;
import cmpt276.phosphorus.childapp.model.TaskManager;
import cmpt276.phosphorus.childapp.utils.Intents;

public class ConfigureTaskActivity extends AppCompatActivity {

    private boolean isEditing;
    private String initalName;
    private Task task;

    public static Intent makeIntent(Context context, Task editTask) {
        Intent intent = new Intent(context, ConfigureTaskActivity.class);
        intent.putExtra(Intents.TASK_NAME_TAG, (editTask != null ? editTask.getName() : null));
        return intent;
    }

    public static Intent makeIntent(Context context) {
        return makeIntent(context, null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_task);

        this.setTitle(getString(R.string.configure_task_activity_title));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        this.extractIntent();

        this.updateDisplay();
        this.updateTitle();
        this.btnConfirm();
        this.btnDelete();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void extractIntent() {
        Intent packageInfo = getIntent();
        String intentTaskName = packageInfo.getStringExtra(Intents.TASK_NAME_TAG);
        Task editedTask = TaskManager.getInstance().getTaskByName(intentTaskName);
        this.isEditing = (editedTask != null);
        this.initalName = intentTaskName;
        this.task = this.isEditing ? editedTask : new Task("", ChildManager.getInstance().getAllChildren());
    }

    private void updateDisplay() {
        EditText childNameEditText = findViewById(R.id.inputEditTaskName);
        childNameEditText.setText(this.task.getName());
    }

    private void updateTitle() {
        TextView taskTitle = findViewById(R.id.task_title);
        taskTitle.setText(this.isEditing ? R.string.task_title_edit : R.string.task_title_new);
    }

    private void btnConfirm() {
        Button confirmBtn = findViewById(R.id.btnConfirmTask);
        EditText childNameEditText = findViewById(R.id.inputEditTaskName);
        confirmBtn.setOnClickListener(view -> {
            String newName = childNameEditText.getText().toString().trim();

            if (TaskManager.getInstance().containsName(newName) && (!this.initalName.equals(newName))) {
                this.showDialogAlert(R.string.task_alert_duplicate_title, R.string.task_alert_duplicate_dec);
                return;
            }

            if (newName.isEmpty()) {
                this.showDialogAlert(R.string.dialog_title_invalid_name, R.string.dialog_msg_invalid_name);
                return;
            }

            final int MAX_CHAR_LENGTH = 88;
            if (newName.length() >= MAX_CHAR_LENGTH) {
                this.showDialogAlert(R.string.dialog_title_name_too_large, R.string.dialog_msg_name_too_large);
                return;
            }

            this.task.setName(newName);
            if (!this.isEditing) {
                TaskManager.getInstance().addTask(this.task);
            }

            DataManager.getInstance(this).saveData(DataType.TASKS);
            finish();
        });
    }

    private void btnDelete() {
        Button btnDeleteTask = findViewById(R.id.btnDeleteTask);
        if (!this.isEditing) { // Is creating
            btnDeleteTask.setVisibility(View.INVISIBLE);
            return;
        }

        btnDeleteTask.setOnClickListener(view -> {
            // todo task delete
        });
    }

    private void showDialogAlert(@StringRes int title, @StringRes int dec) {
        AlertDialog.Builder dialogWarning = new AlertDialog.Builder(this);
        dialogWarning.setTitle(title);
        dialogWarning.setMessage(dec);
        dialogWarning.setPositiveButton(R.string.dialog_confirm, null);
        dialogWarning.show();
    }
}