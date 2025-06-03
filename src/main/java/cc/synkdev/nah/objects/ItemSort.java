package cc.synkdev.nah.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class ItemSort {
    private String name;
    private Material icon;
    private List<Material> contents;
    public ItemSort(String name) {
        this.name = name;
        this.icon = Material.DIRT;
        this.contents = new ArrayList<>();
    }

    public JSONObject export() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        obj.put("icon", icon.toString());

        JSONArray arr = new JSONArray();
        contents.forEach(mat -> arr.put(mat.name()));
        obj.put("contents", arr);
        return obj;
    }
}
