package com.ai.AiHBase;

import com.ai.util.HBaseUtil;

/**
 * @author tq
 * @date 2019/12/12 11:28
 */
public class CreateHBaseTable {
    public static void main(String[] args) {

        //hbase表名自己写
        HBaseUtil.createTable("","info");
    }
}
