/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.util;

import java.util.AbstractList;
import java.util.List;

/**
 *
 * @author timbo
 */
public class CollectionUtils {

    public static List<Integer> asIntegerList(final int[] is) {
        return new AbstractList<Integer>() {
            @Override
            public Integer get(int i) {
                return is[i];
            }

            @Override
            public int size() {
                return is.length;
            }
        };
    }

}
