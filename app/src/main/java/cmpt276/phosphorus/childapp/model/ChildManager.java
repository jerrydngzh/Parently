package cmpt276.phosphorus.childapp.model;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// ==============================================================================================
//
// Manager to keep track of all children data
//
// ==============================================================================================
public class ChildManager {

    private static ChildManager instance;
    private final File file;
    private List<Child> allChildren;
    private Child lastCoinChooserChild;

    private ChildManager(Context context) {
        this.allChildren = new ArrayList<>();
        this.lastCoinChooserChild = null;
        File dir = context.getFilesDir();
        this.file = new File(dir, "child.json");//use this to create new directory that can be written to
        this.getFromFile();
    }

    public static ChildManager getInstance(Context context) {
        if (instance == null) {
            instance = new ChildManager(context);
        }
        return instance;
    }

    public void addChild(@NotNull Child child) {
        this.allChildren.add(Objects.requireNonNull(child));
        saveToFile();
    }

    public Child getChild(@NotNull CoinFlipResult targetCoinFlip) {
        return this.allChildren.stream()
                .filter(child -> child.getCoinFlipResults().stream().anyMatch(targetCoinFlip::equals))
                .findFirst()
                .orElse(null);
    }


    public void addChildren(@NotNull Child... children) {
        Arrays.asList(children).forEach(this::addChild); // Add children already checks for null
    }

    public Child getChildByUUID(@NotNull UUID uuid) {
        return this.allChildren.stream().filter(child -> child.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public Child getChildByPos(@NotNull int position) {
        return this.allChildren.get(position);
    }

    public int getChildPosition(@NotNull Child child) {
        return this.allChildren.indexOf(child);
    }

    public List<Child> getAllChildren() {
        return this.allChildren;
    }

    public boolean removeChild(UUID byChildUUID) {
        Child target = this.getChildByUUID(byChildUUID);
        return this.removeChild(target);
    }

    public boolean removeChild(Child child) {
        boolean isRemoved = this.allChildren.remove(child); // We make sure we do this before saving cause it might err
        this.saveToFile();
        return isRemoved;
    }

    public Child getNextCoinFlipper() {
        if (this.allChildren.isEmpty()) return null;

        if (this.lastCoinChooserChild == null)
            return this.getChildByPos(0);

        int nextPosition = this.getChildPosition(this.lastCoinChooserChild) + 1;
        if (nextPosition >= this.allChildren.size())
            nextPosition = 0;

        return this.getChildByPos(nextPosition);
    }

    public void setLastCoinChooserChild(Child lastCoinChooserChild) {
        this.lastCoinChooserChild = lastCoinChooserChild;
    }

    private boolean isEmpty() {
        return this.allChildren.isEmpty();
    }

    //https://docs.oracle.com/javase/7/docs/api/java/io/FileWriter.html
    //saves children to file
    private void saveToFile() {
        Gson gson = getGson();
        try {
            Writer writer = new FileWriter(file);//writes to designated file
            gson.toJson(this.allChildren, writer);//writes this.allChildren to the file
            writer.close();
        } catch (IOException ignored) {
        }
    }

    //https://attacomsian.com/blog/gson-write-json-file
    private void getFromFile() {
        Gson gson = getGson();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));//reads from designated file
            Type childType = new TypeToken<List<Child>>() {
            }.getType();//gson uses this to parse the Child type
            this.allChildren = gson.fromJson(bufferedReader, childType);//loads contents as allChildren
            if (this.allChildren == null) {
                this.allChildren = new ArrayList<>();
            }//if file is empty
            bufferedReader.close();
        } catch (IOException e) {
            this.allChildren = new ArrayList<>();
        }
    }

    //got from https://stackoverflow.com/questions/39192945/serialize-java-8-localdate-as-yyyy-mm-dd-with-gson
    //gets a Gson object capable of interacting with a LocalDateTime object
    private Gson getGson() {
        return new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
                new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter jsonWriter,
                                      LocalDateTime localDateTime) throws IOException {
                        jsonWriter.value(localDateTime.toString());
                    }

                    @Override
                    public LocalDateTime read(JsonReader jsonReader) throws IOException {
                        return LocalDateTime.parse(jsonReader.nextString());
                    }
                }).create();
    }

}