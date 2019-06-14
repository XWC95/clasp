package com.vea.safecatcher.rt;

import android.util.Log;

/**
 * @author Vea
 * @since 2019-01
 */
public class BlockManager {
    private static IBlockHandler iBlockHandler = new IBlockHandler() {

        private static final String TAG = "MethodTime";

        private static final int BLOCK_THRESHOLD = 0;

        @Override
        public void timingMethod(String method, int cost) {
            if(cost >= threshold()) {
                Log.i(TAG, method + " costed " + cost);
            }
        }

        @Override
        public String dump() {
            return "";
        }

        @Override
        public void clear() {

        }

        @Override
        public int threshold() {
            return BLOCK_THRESHOLD;
        }
    };

    public static void installBlockManager(IBlockHandler custom) {
        BlockManager.iBlockHandler = custom;
    }

    public static void timingMethod(String method, long cost) {
        iBlockHandler.timingMethod(method, (int) cost);
    }
}
