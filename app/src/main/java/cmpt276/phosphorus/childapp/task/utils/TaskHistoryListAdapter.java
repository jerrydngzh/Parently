package cmpt276.phosphorus.childapp.task.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.List;

import cmpt276.phosphorus.childapp.R;
import cmpt276.phosphorus.childapp.model.task.TaskHistory;

// ==============================================================================================
//
// ListAdapter to display all the task's
//
// ==============================================================================================
public class TaskHistoryListAdapter extends ArrayAdapter<TaskHistory> {

    public TaskHistoryListAdapter(Context context, List<TaskHistory> taskHist) {
        super(context, R.layout.task_history_item, taskHist);
    }

    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View taskView = convertView;

        if (taskView == null) {
            taskView = LayoutInflater.from(getContext())
                    .inflate(R.layout.task_history_item, parent, false);
        }

        TaskHistory currentTask = getItem(position);

        TextView taskName = taskView.findViewById(R.id.taskChildName);
        taskName.setText(currentTask.getChildName());
        taskName.setTextColor(taskView.getResources().getColor(R.color.black, null));
        taskName.setTypeface(null, Typeface.BOLD);

        // Set the name of the child assigned to the task
        TextView taskTime = taskView.findViewById(R.id.taskTime);
        String dateTime = currentTask.getFormattedDate();
        String dialogTitle = taskView.getResources().getString(R.string.task_history_completed_turn).replace("%date%", dateTime);

        taskTime.setText(dialogTitle);
        taskTime.setTextColor(taskView.getResources().getColor(R.color.black, null));

        ImageView childIconTaskHistory = taskView.findViewById(R.id.childIconTaskHistory);
        if (currentTask.getChildIcon() != null) {
            Glide.with(this.getContext())
                    .load(currentTask.getChildIcon())
                    .into(childIconTaskHistory);
        } else {
            childIconTaskHistory.setImageResource(R.drawable.child_portrait_default);
        }

        return taskView;
    }
}
