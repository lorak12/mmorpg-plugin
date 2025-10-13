package org.nakii.mmorpg.quest.conversation;

import net.kyori.adventure.text.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface LineView {

    List<Line> getLines();
    int getSize();

    class Line {
        private final Component component;
        public Line(Component component) { this.component = component; }
        public Component getComponent() { return component; }
    }

    class Holder implements LineView {
        private final List<Line> lines;
        public Holder(Line... lines) { this.lines = List.of(lines); }
        public List<Line> getLines() { return new ArrayList<>(lines); }
        public int getSize() { return lines.size(); }
    }

    class Excerpt implements LineView {
        private final LineView source;
        private final int height;
        private final Supplier<Integer> cursor;
        private final Line scrollUp, scrollDown;

        public Excerpt(LineView source, int height, Supplier<Integer> cursor, Line scrollUp, Line scrollDown){
            this.source = source; this.height = height; this.cursor = cursor;
            this.scrollUp = scrollUp; this.scrollDown = scrollDown;
        }

        public List<Line> getLines(){
            List<Line> all = source.getLines();
            if(all.size() <= height) return all;
            int pos = Math.min(cursor.get(), all.size()-height);
            List<Line> slice = new ArrayList<>(all.subList(pos,pos+height));
            if(pos>0) slice.set(0, scrollUp);
            if(pos+height<all.size()) slice.set(height-1, scrollDown);
            return slice;
        }
        public int getSize(){ return height; }
    }
}
