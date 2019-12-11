package com.ai.AiHBase;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.util.HBaseUtil;
import com.ai.util.MySQLUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author tq
 * @date 2019/12/10 14:24
 *
 * 抽取每天的增量数据调用ai接口解析后存入hbase
 */
public class MySQLToHBase {
    public static void main(String[] args) throws SQLException, IOException {
        Connection connection=MySQLUtil.getConnection();

        Statement statement =connection.createStatement();

        //查找今天的增量数据
        ResultSet resultSet=statement.executeQuery("select a.doc_id,a.content,a.create_time from judicial_document a  where a.trial_procedures='0201' and substr(a.create_time,1,10)=curdate() ");
        AIOpenClient aiOpenClient = new AIOpenClient("http://ai.hua-cloud.com.cn:8072/api", "test", "123456");
        Map<String, Object> result =null;
        List<Put> putsList=new ArrayList<>();

        while (resultSet.next()){
            String text=resultSet.getString("content");
            String doc_id=resultSet.getString("doc_id");
            String create_time=resultSet.getString("create_time");
            result=aiOpenClient.nerAggregation("xsyspj",text);
            JSONObject jsonObject=JSON.parseObject(JSON.toJSONString(result));
            Put put=new Put(Bytes.toBytes(create_time+"_"+doc_id));
            byte[] family=Bytes.toBytes("info");



//                    HBaseUtil.putRow("tq",resultSet.getString("doc_id"),"info",key,result.get(key).toString());

        }
        connection.close();

    }
}
