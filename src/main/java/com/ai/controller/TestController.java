package com.ai.controller;

import com.ai.util.DateUtil;
import com.ai.util.HBaseUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.*;

/**
 * @author tq
 * @date 2019/12/9 14:45
 */
@RestController
public class TestController {



    @Autowired
    HBaseUtil hBaseUtil;

    @GetMapping("/query")
    public Map<String, Map<String, String>> getContent(String startDate, String stopDate) throws IOException {


        ResultScanner hbaseResults = hBaseUtil.getScanner("tq", startDate, stopDate);
        //返回的是如下结构  map（rowkey，map（列名，值））
        Map<String, Map<String, String>> resultMap = new HashMap<>();

        for (Result result : hbaseResults) {
            //每一行数据
            Map<String, String> columnMap = new HashMap<>();
            String rowkey = null;
            for (Cell cell : result.listCells()) {
                if (rowkey == null) {
                    rowkey = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                }
                columnMap.put(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()), Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
            if (rowkey != null) {
                resultMap.put(rowkey, columnMap);
            }
        }
        return resultMap;
    }
}