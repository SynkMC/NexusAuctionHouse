package cc.synkdev.nah.gui;

import cc.synkdev.nah.objects.ItemSort;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class MainGuiSnapshot {
    private int page;
    private String search;
    private int firstSort;
    private ItemSort itSort;
}
