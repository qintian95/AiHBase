package com.ai.controller;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.pojo.ChargeTree;
import com.ai.pojo.JudicialDocument;
import com.ai.service.ChargeTreeService;
import com.ai.service.JudicialDocumentService;
import com.ai.util.HBaseUtil;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
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
    ChargeTreeService chargeTreeService;

    @Autowired
    JudicialDocumentService judicialDocumentService;



    @GetMapping
    public  byte[] getContent() throws IOException {


        ResultScanner hbaseResults = HBaseUtil.getScanner("tq","0","99999999999999999");

        Result hr= hbaseResults.next();

        return hr.value();
    }

//    @GetMapping
//    public List<ChargeTree> getChargeTree() {
//        return chargeTreeService.getChargeTree();
//    }


}
