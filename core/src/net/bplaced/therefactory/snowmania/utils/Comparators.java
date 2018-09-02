package net.bplaced.therefactory.snowmania.utils;

import net.bplaced.therefactory.snowmania.model.actors.Fridge;
import net.bplaced.therefactory.snowmania.model.actors.Snowman;

import java.util.Comparator;

/**
 * Created by Christian on 17.01.2018.
 */

public class Comparators {

    public static Comparator<? super Fridge> SortFridgesDescByYpos = new Comparator<Fridge>() {
        @Override
        public int compare(Fridge o1, Fridge o2) {
            return (int) (o2.y - o1.y);
        }
    };

    public static Comparator<? super Snowman> SortSnowmanDescByYpos = new Comparator<Snowman>() {
        @Override
        public int compare(Snowman o1, Snowman o2) {
            return (int) (o2.y - o1.y);
        }
    };
}
