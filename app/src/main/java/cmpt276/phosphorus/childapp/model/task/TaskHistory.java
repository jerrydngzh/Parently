package cmpt276.phosphorus.childapp.model.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TaskHistory {
    private final UUID child;
    private final LocalDateTime date;

    public TaskHistory(UUID child){
        this.child = child;
        this.date = LocalDateTime.now();
    }

    public UUID getChild() {
        return this.child;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getFormattedDate(){
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMMM dd HH:mm a");
        return date.format(format);
    }
}
