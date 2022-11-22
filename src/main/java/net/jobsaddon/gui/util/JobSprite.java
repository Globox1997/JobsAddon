package net.jobsaddon.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class JobSprite extends WSprite {

    private List<OrderedText> text = Lists.newArrayList();

    public JobSprite(Identifier image, float u1, float v1, float u2, float v2) {
        super(image, u1, v1, u2, v2);
    }

    @Override
    public void addTooltip(TooltipBuilder tooltip) {
        for (int i = 0; i < text.size(); i++) {
            tooltip.add(text.get(i));
        }
    }

    public void addText(String... string) {
        for (String s : string) {
            text.add(Text.of(s).asOrderedText());
        }
    }
}
