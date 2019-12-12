package com.ai.controller;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.pojo.ChargeTree;
import com.ai.pojo.JudicialDocument;
import com.ai.service.ChargeTreeService;
import com.ai.service.JudicialDocumentService;
import com.ai.util.HBaseUtil;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
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
    public  Map<String,Map<String,String>>  getContent() throws IOException {


        ResultScanner hbaseResults = hBaseUtil.getScanner("tq", "2019-12-12 00:09:04.0_f891ea7d-53e3-473b-8b6e-a8d400b6351f", "2019-12-12 00:09:04.0_f891ea7d-53e3-473b-8b6e-a8d400b6351f");

        //返回的是如下结构  map（rowkey，map（列名，值））
        Map<String,Map<String,String>> resultMap = new HashMap<>();

        for (Result result:hbaseResults){
            //每一行数据
            Map<String,String> columnMap = new HashMap<>();
            String rowkey =null;
            for (Cell cell:result.listCells()){
                if(rowkey==null){
                    rowkey= Bytes.toString(cell.getRowArray(),cell.getRowOffset(),cell.getRowLength());
                }
//                list.add(rowkey+" "+Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength())+" : "+Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
                columnMap.put(Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength()), Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength()));
            }
            if(rowkey!=null){
                resultMap.put(rowkey,columnMap);
            }
        }
        return resultMap;
    }

}
