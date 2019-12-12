package com.ai.AiHBase;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.util.HBaseUtil;
import com.ai.util.MySQLUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
 * <p>
 * 抽取每天的增量数据调用ai接口解析后存入hbase
 */
public class MySQLToHBase {
    public static void main(String[] args) throws SQLException, IOException {
        Connection connection = MySQLUtil.getConnection();

        Statement statement = connection.createStatement();

        //查找今天的增量数据
        ResultSet resultSet = statement.executeQuery("select a.doc_id,a.content,a.create_time from judicial_document a  where a.trial_procedures='0201' and substr(a.create_time,1,10)=curdate() ");
        AIOpenClient aiOpenClient = new AIOpenClient("http://ai.hua-cloud.com.cn:8072/api", "test", "123456");
        Map<String, Object> result = null;
        List<Put> putsList = new ArrayList<>();

        while (resultSet.next()) {
            String text = resultSet.getString("content");
            String doc_id = resultSet.getString("doc_id");
            String create_time = resultSet.getString("create_time");

            Put put = new Put(Bytes.toBytes(create_time + "_" + doc_id));
            byte[] family = Bytes.toBytes("info");

            result = aiOpenClient.nerAggregation("xsyspj", text);
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(result).replaceAll("[\\[]]", "null"));

            JSONObject document_reference = jsonObject.getJSONObject("document_reference");
            JSONArray defendantsArr = jsonObject.getJSONArray("defendants");
            int defendantsLength = defendantsArr.toArray().length;
            if (defendantsLength > 0) {
                for (int i = 0; i < defendantsLength; i++) {
                    JSONObject defendantsObj = defendantsArr.getJSONObject(i);
                    JSONObject defendant_base = defendantsObj.getJSONObject("defendant_base");
                    Object[] defendant_baseK = defendant_base.keySet().toArray();
                    for (int j = 0; j < defendant_baseK.length; j++) {
                        if ("special_identity".equalsIgnoreCase(defendant_baseK[i].toString())) {
                            JSONObject special_identity = defendant_base.getJSONObject("special_identity");
                            Object[] array = special_identity.keySet().toArray();
                            if (array.length > 0) {
                                for (int k = 0; k < array.length; k++) {
                                    put.addColumn(family, Bytes.toBytes("defendants" + i + ":defendant_base:special_identity:" + array[k]), Bytes.toBytes(special_identity.getString(array[k].toString())));
                                }
                            }
                        }

                        put.addColumn(family, Bytes.toBytes("defendants" + i + ":defendant_base:" + defendant_baseK[j]), Bytes.toBytes(defendant_base.getString(defendant_baseK[j].toString())));
                    }

                    JSONArray defendant_preConvictionsArr = defendantsObj.getJSONArray("defendant_preConvictions");
                    int defendant_preConvictionsArrLen = defendant_preConvictionsArr.toArray().length;
                    if (defendant_preConvictionsArrLen > 0) {
                        for (int j = 0; j < defendant_preConvictionsArrLen; j++) {
                            JSONObject defendant_preConvictionsObj = defendant_preConvictionsArr.getJSONObject(j);
                            Object[] defendant_preConvictionsK = defendant_preConvictionsObj.keySet().toArray();
                            for (int k = 0; k < defendant_preConvictionsK.length; k++) {
                                put.addColumn(family, Bytes.toBytes("defendants" + i + ":defendant_preConvictions" + j + ":" + defendant_preConvictionsK[k]), Bytes.toBytes(defendant_preConvictionsObj.getString(defendant_preConvictionsK[k].toString())));
                            }
                        }
                    }

                    JSONArray  chargesArr=defendantsObj.getJSONArray("charges");
                    int chargesArrLen=chargesArr.toArray().length;
                    if (chargesArrLen>0){
                        for (int j = 0; j <chargesArrLen ; j++) {
                            JSONObject chargesObj=chargesArr.getJSONObject(j);
                            Object[] chargesK=chargesObj.keySet().toArray();
                            for (int k = 0; k <chargesK.length ; k++) {
                                if("charge_enforcements".equalsIgnoreCase(chargesK[k].toString())){
                                    JSONArray charge_enforcementsArr = chargesObj.getJSONArray("charge_enforcements");
                                    int charge_enforcementsArrLen = charge_enforcementsArr.size();
                                    for (int l = 0; l <charge_enforcementsArrLen ; l++) {
                                        JSONObject charge_enforcementsObj = charge_enforcementsArr.getJSONObject(l);
                                        Object[] charge_enforcementsK = charge_enforcementsObj.keySet().toArray();
                                        for (int m = 0; m <charge_enforcementsK.length ; m++) {

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            JSONObject defendants0 = jsonObject.getJSONArray("defendants").getJSONObject(0);

            JSONObject accusation = jsonObject.getJSONObject("accusation");
            JSONObject judgment = jsonObject.getJSONObject("judgment");
            JSONObject hear_info = jsonObject.getJSONObject("hear_info");
            JSONObject evidences = jsonObject.getJSONObject("evidences");

            //每条数据查询出来的结构可能都不一样，每个值需要加上判断，好麻烦
            String document_reference2trail = document_reference.getString("trail");
            String document_reference2JDN = document_reference.getString("JDN");

            String document_name = jsonObject.getString("document_name");

            String defendants02defendant_preConvictions2preConviction_time = defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_time");
            String defendants02defendant_preConvictions2preConviction_chargeName = defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_chargeName");
            String defendants02defendant_preConvictions2preConviction_release = defendants0.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_release");

            if (defendants0.getJSONArray("charges").getJSONObject(0).containsKey("suspect_charge_name")) {
                String defendants02charges2suspect_charge_name2index = defendants0.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("index");
                String defendants02charges2suspect_charge_name2text = defendants0.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("text");

                if (defendants02charges2suspect_charge_name2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2suspect_charge_name2index"), Bytes.toBytes(defendants02charges2suspect_charge_name2index));
                }
                if (defendants02charges2suspect_charge_name2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2suspect_charge_name2text"), Bytes.toBytes(defendants02charges2suspect_charge_name2text));
                }
            }

            String defendants02charges2charge_enforcements2charge_enforcement_type2index0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("index");
            String defendants02charges2charge_enforcements2charge_enforcement_type2text0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("text");

            String defendants02charges2charge_enforcements2charge_enforcement_time2index0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("index");
            String defendants02charges2charge_enforcements2charge_enforcement_time2text0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("text");


            if (defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).containsKey("charge_enforcement_organ")) {
                String defendants02charges2charge_enforcements2charge_enforcement_organ2index0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("index");
                String defendants02charges2charge_enforcements2charge_enforcement_organ2text0 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("text");

                if (defendants02charges2charge_enforcements2charge_enforcement_organ2index0 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_organ2index0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_organ2index0));
                }
                if (defendants02charges2charge_enforcements2charge_enforcement_organ2text0 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_organ2text0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_organ2text0));
                }
            }

            if (defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).containsKey("charge_enforcement_type")) {
                String defendants02charges2charge_enforcements2charge_enforcement_type2index1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("index");
                String defendants02charges2charge_enforcements2charge_enforcement_type2text1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("text");

                if (defendants02charges2charge_enforcements2charge_enforcement_type2index1 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_type2index1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_type2index1));
                }
                if (defendants02charges2charge_enforcements2charge_enforcement_type2text1 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_type2text1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_type2text1));
                }

            }

            String defendants02charges2charge_enforcements2charge_enforcement_time2index1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("index");
            String defendants02charges2charge_enforcements2charge_enforcement_time2text1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("text");

            if (defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).containsKey("charge_enforcement_organ")) {
                String defendants02charges2charge_enforcements2charge_enforcement_organ2index1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("index");
                String defendants02charges2charge_enforcements2charge_enforcement_organ2text1 = defendants0.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("text");

                if (defendants02charges2charge_enforcements2charge_enforcement_organ2index1 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_organ2index1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_organ2index1));
                }
                if (defendants02charges2charge_enforcements2charge_enforcement_organ2text1 != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_organ2text1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_organ2text1));
                }
            }

            String defendants02defendant_base2NN = defendants0.getJSONObject("defendant_base").getString("NN");

            String defendants02defendant_base2BD2index = defendants0.getJSONObject("defendant_base").getJSONObject("BD").getString("index");
            String defendants02defendant_base2BD2text = defendants0.getJSONObject("defendant_base").getJSONObject("BD").getString("text");

            String defendants02defendant_base2PS = defendants0.getJSONObject("defendant_base").getString("PS");
            String defendants02defendant_base2defendant_nativePlace = defendants0.getJSONObject("defendant_base").getString("defendant_nativePlace");

            String defendants02defendant_base2E2index = defendants0.getJSONObject("defendant_base").getJSONObject("E").getString("index");
            String defendants02defendant_base2E2text = defendants0.getJSONObject("defendant_base").getJSONObject("E").getString("text");


            if (defendants0.getJSONObject("defendant_base").containsKey("FN")) {
                String defendants02defendant_base2FN2index = defendants0.getJSONObject("defendant_base").getJSONObject("FN").getString("index");
                String defendants02defendant_base2FN2text = defendants0.getJSONObject("defendant_base").getJSONObject("FN").getString("text");

                if (defendants02defendant_base2FN2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2FN2index"), Bytes.toBytes(defendants02defendant_base2FN2index));
                }
                if (defendants02defendant_base2FN2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2FN2text"), Bytes.toBytes(defendants02defendant_base2FN2text));
                }
            }


            String defendants02defendant_base2defendant_capacity = defendants0.getJSONObject("defendant_base").getString("defendant_capacity");

            String defendants02defendant_base2defendant_occupation2index = defendants0.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("index");
            String defendants02defendant_base2defendant_occupation2text = defendants0.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("text");

            if (defendants0.getJSONObject("defendant_base").containsKey("DR")) {
                String defendants02defendant_base2DR2index = defendants0.getJSONObject("defendant_base").getJSONObject("DR").getString("index");
                String defendants02defendant_base2DR2text = defendants0.getJSONObject("defendant_base").getJSONObject("DR").getString("text");

                if (defendants02defendant_base2DR2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2DR2index"), Bytes.toBytes(defendants02defendant_base2DR2index));
                }
                if (defendants02defendant_base2DR2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2DR2text"), Bytes.toBytes(defendants02defendant_base2DR2text));
                }
            }


            String defendants02defendant_base2S2index = defendants0.getJSONObject("defendant_base").getJSONObject("S").getString("index");
            String defendants02defendant_base2S2text = defendants0.getJSONObject("defendant_base").getJSONObject("S").getString("text");

            String defendants02defendant_base2defendant_identity = defendants0.getJSONObject("defendant_base").getString("defendant_identity");

            String defendants02defendant_base2defendant_name2index = defendants0.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("index");
            String defendants02defendant_base2defendant_name2text = defendants0.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("text");


            if (defendants0.getJSONObject("defendant_base").containsKey("FA")) {
                String defendants02defendant_base2FA2index = defendants0.getJSONObject("defendant_base").getJSONObject("FA").getString("index");
                String defendants02defendant_base2FA2text = defendants0.getJSONObject("defendant_base").getJSONObject("FA").getString("text");

                if (defendants02defendant_base2FA2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2FA2index"), Bytes.toBytes(defendants02defendant_base2FA2index));
                }
                if (defendants02defendant_base2FA2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants02defendant_base2FA2text"), Bytes.toBytes(defendants02defendant_base2FA2text));
                }
            }


            String defendants02defendant_base2special_identity2identity_others = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_others");
            String defendants02defendant_base2special_identity2identity_star = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_star");
            String defendants02defendant_base2special_identity2identity_partygroupings = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_partygroupings");
            String defendants02defendant_base2special_identity2identity_intellectual = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_intellectual");
            String defendants02defendant_base2special_identity2identity_cppcc_members = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_cppcc_members");
            String defendants02defendant_base2special_identity2identity_sportstar = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_sportstar");
            String defendants02defendant_base2special_identity2identity_officer = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_officer");
            String defendants02defendant_base2special_identity2identity_npc_member = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_npc_member");
            String defendants02defendant_base2special_identity2identity_foreigner = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_foreigner");
            String defendants02defendant_base2special_identity2identity_taiwan = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_taiwan");
            String defendants02defendant_base2special_identity2identity_leader = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_leader");
            String defendants02defendant_base2special_identity2identity_hongkongmacau = defendants0.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_hongkongmacau");


            String defendants02defendant_base2PN2index = defendants0.getJSONObject("defendant_base").getJSONObject("PN").getString("index");
            String defendants02defendant_base2PN2text = defendants0.getJSONObject("defendant_base").getJSONObject("PN").getString("text");

            String defendants02defendant_base2BA = defendants0.getJSONObject("defendant_base").getString("BA");

            String defendants02judgment2penalties2penalty_notimprison = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_notimprison");
            String defendants02judgment2penalties2penalty_principal = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_principal");
            String defendants02judgment2penalties2penalty_suspend = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_suspend");
            String defendants02judgment2penalties2JA = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("JA");
            String defendants02judgment2penalties2penalty_duration = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_duration");


            JSONObject penalty_supplementaries = defendants0.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getJSONArray("penalty_supplementaries").getJSONObject(0);
            String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_money = penalty_supplementaries.getString("penalty_supplementary_money");
            String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_content = penalty_supplementaries.getString("penalty_supplementary_content");
            String defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_type = penalty_supplementaries.getString("penalty_supplementary_type");

            String defendants02judgment2defendant_joint_crime = defendants0.getJSONObject("judgment").getString("defendant_joint_crime");

            JSONObject attorneys = defendants0.getJSONArray("attorneys").getJSONObject(0);
            String defendants02attorneys2attorney_type = attorneys.getString("attorney_type");
            String defendants02attorneys2attorney_workplace = attorneys.getString("attorney_workplace");
            String defendants02attorneys2attorney_relation = attorneys.getString("attorney_relation");
            String defendants02attorneys2attorney_name = attorneys.getString("attorney_name");
            String defendants02attorneys2attorney_occupation = attorneys.getString("attorney_occupation");

            String defendants02detain2inPrison = defendants0.getJSONObject("detain").getString("inPrison");
            String defendants02detain2inPrison_place = defendants0.getJSONObject("detain").getString("inPrison_place");


            if (jsonObject.getJSONArray("defendants").toArray().length == 2) {
                JSONObject defendants1 = jsonObject.getJSONArray("defendants").getJSONObject(1);

                String defendants12defendant_preConvictions2preConviction_time = defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_time");
                String defendants12defendant_preConvictions2preConviction_chargeName = defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_chargeName");
                String defendants12defendant_preConvictions2preConviction_release = defendants1.getJSONArray("defendant_preConvictions").getJSONObject(0).getString("preConviction_release");

//        String defendants12charges2suspect_charge_name2index=defendants1.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("index");
//        String defendants12charges2suspect_charge_name2text=defendants1.getJSONArray("charges").getJSONObject(0).getJSONObject("suspect_charge_name").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_type2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_type2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_type").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_time2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_time2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_time").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_organ2index0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_organ2text0=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(0).getJSONObject("charge_enforcement_organ").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_type2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_type2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_type").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_time2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_time2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_time").getString("text");

//        String defendants12charges2charge_enforcements2charge_enforcement_organ2index1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("index");
//        String defendants12charges2charge_enforcements2charge_enforcement_organ2text1=defendants1.getJSONArray("charges").getJSONObject(0).getJSONArray("charge_enforcements").getJSONObject(1).getJSONObject("charge_enforcement_organ").getString("text");

                String defendants12defendant_base2NN = defendants1.getJSONObject("defendant_base").getString("NN");

                if (defendants1.getJSONObject("defendant_base").containsKey("BD")) {
                    String defendants12defendant_base2BD2index = defendants1.getJSONObject("defendant_base").getJSONObject("BD").getString("index");
                    String defendants12defendant_base2BD2text = defendants1.getJSONObject("defendant_base").getJSONObject("BD").getString("text");

                    if (defendants12defendant_base2BD2index != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2BD2index"), Bytes.toBytes(defendants12defendant_base2BD2index));
                    }
                    if (defendants12defendant_base2BD2text != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2BD2text"), Bytes.toBytes(defendants12defendant_base2BD2text));
                    }
                }


                String defendants12defendant_base2PS = defendants1.getJSONObject("defendant_base").getString("PS");
                String defendants12defendant_base2defendant_nativePlace = defendants1.getJSONObject("defendant_base").getString("defendant_nativePlace");

                if (defendants1.getJSONObject("defendant_base").containsKey("E")) {
                    String defendants12defendant_base2E2index = defendants1.getJSONObject("defendant_base").getJSONObject("E").getString("index");
                    String defendants12defendant_base2E2text = defendants1.getJSONObject("defendant_base").getJSONObject("E").getString("text");

                    if (defendants12defendant_base2E2index != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2E2index"), Bytes.toBytes(defendants12defendant_base2E2index));
                    }
                    if (defendants12defendant_base2E2text != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2E2text"), Bytes.toBytes(defendants12defendant_base2E2text));
                    }
                }


//        String defendants12defendant_base2FN2index=defendants1.getJSONObject("defendant_base").getJSONObject("FN").getString("index");
//        String defendants12defendant_base2FN2text=defendants1.getJSONObject("defendant_base").getJSONObject("FN").getString("text");

                String defendants12defendant_base2defendant_capacity = defendants1.getJSONObject("defendant_base").getString("defendant_capacity");

                String defendants12defendant_base2defendant_occupation2index = defendants1.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("index");
                String defendants12defendant_base2defendant_occupation2text = defendants1.getJSONObject("defendant_base").getJSONObject("defendant_occupation").getString("text");

                if (defendants1.getJSONObject("defendant_base").containsKey("DR")) {
                    String defendants12defendant_base2DR2index = defendants1.getJSONObject("defendant_base").getJSONObject("DR").getString("index");
                    String defendants12defendant_base2DR2text = defendants1.getJSONObject("defendant_base").getJSONObject("DR").getString("text");

                    if (defendants12defendant_base2DR2index != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2DR2index"), Bytes.toBytes(defendants12defendant_base2DR2index));
                    }
                    if (defendants12defendant_base2DR2text != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2DR2text"), Bytes.toBytes(defendants12defendant_base2DR2text));
                    }
                }


                String defendants12defendant_base2S2index = defendants1.getJSONObject("defendant_base").getJSONObject("S").getString("index");
                String defendants12defendant_base2S2text = defendants1.getJSONObject("defendant_base").getJSONObject("S").getString("text");

                String defendants12defendant_base2defendant_identity = defendants1.getJSONObject("defendant_base").getString("defendant_identity");

//        String defendants12defendant_base2defendant_name2index=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("index");
//        String defendants12defendant_base2defendant_name2text=defendants1.getJSONObject("defendant_base").getJSONObject("defendant_name").getString("text");

                if (defendants1.getJSONObject("defendant_base").containsKey("FA")) {
                    String defendants12defendant_base2FA2index = defendants1.getJSONObject("defendant_base").getJSONObject("FA").getString("index");
                    String defendants12defendant_base2FA2text = defendants1.getJSONObject("defendant_base").getJSONObject("FA").getString("text");
                    if (defendants12defendant_base2FA2index != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2FA2index"), Bytes.toBytes(defendants12defendant_base2FA2index));
                    }
                    if (defendants12defendant_base2FA2text != null) {
                        put.addColumn(family, Bytes.toBytes("defendants12defendant_base2FA2text"), Bytes.toBytes(defendants12defendant_base2FA2text));
                    }
                }


                String defendants12defendant_base2special_identity2identity_others = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_others");
                String defendants12defendant_base2special_identity2identity_star = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_star");
                String defendants12defendant_base2special_identity2identity_partygroupings = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_partygroupings");
                String defendants12defendant_base2special_identity2identity_intellectual = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_intellectual");
                String defendants12defendant_base2special_identity2identity_cppcc_members = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_cppcc_members");
                String defendants12defendant_base2special_identity2identity_sportstar = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_sportstar");
                String defendants12defendant_base2special_identity2identity_officer = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_officer");
                String defendants12defendant_base2special_identity2identity_npc_member = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_npc_member");
                String defendants12defendant_base2special_identity2identity_foreigner = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_foreigner");
                String defendants12defendant_base2special_identity2identity_taiwan = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_taiwan");
                String defendants12defendant_base2special_identity2identity_leader = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_leader");
                String defendants12defendant_base2special_identity2identity_hongkongmacau = defendants1.getJSONObject("defendant_base").getJSONObject("special_identity").getString("identity_hongkongmacau");


                String defendants12defendant_base2PN2index = defendants1.getJSONObject("defendant_base").getJSONObject("PN").getString("index");
                String defendants12defendant_base2PN2text = defendants1.getJSONObject("defendant_base").getJSONObject("PN").getString("text");

                String defendants12defendant_base2BA = defendants1.getJSONObject("defendant_base").getString("BA");

                String defendants12judgment2penalties2penalty_notimprison = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_notimprison");
                String defendants12judgment2penalties2penalty_principal = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_principal");
                String defendants12judgment2penalties2penalty_suspend = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_suspend");
                String defendants12judgment2penalties2JA = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("JA");
                String defendants12judgment2penalties2penalty_duration = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getString("penalty_duration");


                JSONObject penalty_supplementaries1 = defendants1.getJSONObject("judgment").getJSONArray("penalties").getJSONObject(0).getJSONArray("penalty_supplementaries").getJSONObject(0);
                String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_money = penalty_supplementaries1.getString("penalty_supplementary_money");
                String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_content = penalty_supplementaries1.getString("penalty_supplementary_content");
                String defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_type = penalty_supplementaries1.getString("penalty_supplementary_type");

                String defendants12judgment2defendant_joint_crime = defendants1.getJSONObject("judgment").getString("defendant_joint_crime");

                JSONObject attorneys1 = defendants1.getJSONArray("attorneys").getJSONObject(0);
                String defendants12attorneys2attorney_type = attorneys1.getString("attorney_type");
                String defendants12attorneys2attorney_workplace = attorneys1.getString("attorney_workplace");
                String defendants12attorneys2attorney_relation = attorneys1.getString("attorney_relation");
                String defendants12attorneys2attorney_name = attorneys1.getString("attorney_name");
                String defendants12attorneys2attorney_occupation = attorneys1.getString("attorney_occupation");

                String defendants12detain2inPrison = defendants1.getJSONObject("detain").getString("inPrison");
                String defendants12detain2inPrison_place = defendants1.getJSONObject("detain").getString("inPrison_place");

                if (defendants12defendant_preConvictions2preConviction_time != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_preConvictions2preConviction_time"), Bytes.toBytes(defendants12defendant_preConvictions2preConviction_time));
                }
                if (defendants12defendant_preConvictions2preConviction_chargeName != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_preConvictions2preConviction_chargeName"), Bytes.toBytes(defendants12defendant_preConvictions2preConviction_chargeName));
                }
                if (defendants12defendant_preConvictions2preConviction_release != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_preConvictions2preConviction_release"), Bytes.toBytes(defendants12defendant_preConvictions2preConviction_release));
                }
                if (defendants12defendant_base2NN != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2NN"), Bytes.toBytes(defendants12defendant_base2NN));
                }

                if (defendants12defendant_base2PS != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2PS"), Bytes.toBytes(defendants12defendant_base2PS));
                }
                if (defendants12defendant_base2defendant_nativePlace != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2defendant_nativePlace"), Bytes.toBytes(defendants12defendant_base2defendant_nativePlace));
                }

                if (defendants12defendant_base2defendant_capacity != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2defendant_capacity"), Bytes.toBytes(defendants12defendant_base2defendant_capacity));
                }
                if (defendants12defendant_base2defendant_occupation2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2defendant_occupation2index"), Bytes.toBytes(defendants12defendant_base2defendant_occupation2index));
                }
                if (defendants12defendant_base2defendant_occupation2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2defendant_occupation2text"), Bytes.toBytes(defendants12defendant_base2defendant_occupation2text));
                }

                if (defendants12defendant_base2S2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2S2index"), Bytes.toBytes(defendants12defendant_base2S2index));
                }
                if (defendants12defendant_base2S2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2S2text"), Bytes.toBytes(defendants12defendant_base2S2text));
                }
                if (defendants12defendant_base2defendant_identity != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2defendant_identity"), Bytes.toBytes(defendants12defendant_base2defendant_identity));
                }

                if (defendants12defendant_base2special_identity2identity_others != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_others"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_others));
                }
                if (defendants12defendant_base2special_identity2identity_star != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_star"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_star));
                }
                if (defendants12defendant_base2special_identity2identity_partygroupings != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_partygroupings"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_partygroupings));
                }
                if (defendants12defendant_base2special_identity2identity_intellectual != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_intellectual"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_intellectual));
                }
                if (defendants12defendant_base2special_identity2identity_cppcc_members != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_cppcc_members"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_cppcc_members));
                }
                if (defendants12defendant_base2special_identity2identity_sportstar != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_sportstar"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_sportstar));
                }
                if (defendants12defendant_base2special_identity2identity_officer != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_officer"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_officer));
                }
                if (defendants12defendant_base2special_identity2identity_npc_member != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_npc_member"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_npc_member));
                }
                if (defendants12defendant_base2special_identity2identity_foreigner != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_foreigner"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_foreigner));
                }
                if (defendants12defendant_base2special_identity2identity_taiwan != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_taiwan"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_taiwan));
                }
                if (defendants12defendant_base2special_identity2identity_leader != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_leader"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_leader));
                }
                if (defendants12defendant_base2special_identity2identity_hongkongmacau != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2special_identity2identity_hongkongmacau"), Bytes.toBytes(defendants12defendant_base2special_identity2identity_hongkongmacau));
                }
                if (defendants12defendant_base2PN2index != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2PN2index"), Bytes.toBytes(defendants12defendant_base2PN2index));
                }
                if (defendants12defendant_base2PN2text != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2PN2text"), Bytes.toBytes(defendants12defendant_base2PN2text));
                }
                if (defendants12defendant_base2BA != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12defendant_base2BA"), Bytes.toBytes(defendants12defendant_base2BA));
                }
                if (defendants12judgment2penalties2penalty_notimprison != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_notimprison"), Bytes.toBytes(defendants12judgment2penalties2penalty_notimprison));
                }
                if (defendants12judgment2penalties2penalty_principal != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_principal"), Bytes.toBytes(defendants12judgment2penalties2penalty_principal));
                }
                if (defendants12judgment2penalties2penalty_suspend != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_suspend"), Bytes.toBytes(defendants12judgment2penalties2penalty_suspend));
                }
                if (defendants12judgment2penalties2JA != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2JA"), Bytes.toBytes(defendants12judgment2penalties2JA));
                }
                if (defendants12judgment2penalties2penalty_duration != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_duration"), Bytes.toBytes(defendants12judgment2penalties2penalty_duration));
                }
                if (defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_money != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_money"), Bytes.toBytes(defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_money));
                }
                if (defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_content != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_content"), Bytes.toBytes(defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_content));
                }
                if (defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_type != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_type"), Bytes.toBytes(defendants12judgment2penalties2penalty_supplementaries2penalty_supplementary_type));
                }
                if (defendants12judgment2defendant_joint_crime != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12judgment2defendant_joint_crime"), Bytes.toBytes(defendants12judgment2defendant_joint_crime));
                }
                if (defendants12attorneys2attorney_type != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12attorneys2attorney_type"), Bytes.toBytes(defendants12attorneys2attorney_type));
                }
                if (defendants12attorneys2attorney_workplace != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12attorneys2attorney_workplace"), Bytes.toBytes(defendants12attorneys2attorney_workplace));
                }
                if (defendants12attorneys2attorney_relation != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12attorneys2attorney_relation"), Bytes.toBytes(defendants12attorneys2attorney_relation));
                }
                if (defendants12attorneys2attorney_name != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12attorneys2attorney_name"), Bytes.toBytes(defendants12attorneys2attorney_name));
                }
                if (defendants12attorneys2attorney_occupation != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12attorneys2attorney_occupation"), Bytes.toBytes(defendants12attorneys2attorney_occupation));
                }
                if (defendants12detain2inPrison != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12detain2inPrison"), Bytes.toBytes(defendants12detain2inPrison));
                }
                if (defendants12detain2inPrison_place != null) {
                    put.addColumn(family, Bytes.toBytes("defendants12detain2inPrison_place"), Bytes.toBytes(defendants12detain2inPrison_place));
                }
            }
            String accusation2accusation_reference = accusation.getString("accusation_reference");
            String accusation2accusation_procurator = accusation.getString("accusation_procurator");
            String accusation2prosecute_charge_name = accusation.getString("prosecute_charge_name");
            String accusation2accusation_time = accusation.getString("accusation_time");
            String accusation2accusation_type = accusation.getString("accusation_type");

            JSONObject laws = judgment.getJSONArray("laws").getJSONObject(0);
            String judgment2laws2law_name = laws.getString("law_name");
            String judgment2laws2article_names = laws.getString("article_names");

            String judgment2judgement_date = laws.getString("judgement_date");

            JSONObject notaffirm_reasons = judgment.getJSONObject("notaffirm_reasons");
            String judgment2notaffirm_reasons2fact_norelevance = notaffirm_reasons.getString("fact_norelevance");
            String judgment2notaffirm_reasons2procedure_illegal = notaffirm_reasons.getString("procedure_illegal");
            String judgment2notaffirm_reasons2fakecopy = notaffirm_reasons.getString("fakecopy");
            String judgment2notaffirm_reasons2other = notaffirm_reasons.getString("other");
            String judgment2notaffirm_reasons2evidence_old = notaffirm_reasons.getString("evidence_old");
            String judgment2notaffirm_reasons2form_illegal = notaffirm_reasons.getString("form_illegal");
            String judgment2notaffirm_reasons2fakeevidence = notaffirm_reasons.getString("fakeevidence");
            String judgment2notaffirm_reasons2doubt = notaffirm_reasons.getString("doubt");
            String judgment2notaffirm_reasons2evidence_unverify = notaffirm_reasons.getString("evidence_unverify");
            String judgment2notaffirm_reasons2means_illegal = notaffirm_reasons.getString("means_illegal");
            String judgment2notaffirm_reasons2evidence_flaw = notaffirm_reasons.getString("evidence_flaw");
            String judgment2notaffirm_reasons2evidence_suspected = notaffirm_reasons.getString("evidence_suspected");

            String document_type = jsonObject.getString("document_type");
            String prosecutors = jsonObject.getString("prosecutors");


            if (document_reference2trail != null) {
                put.addColumn(family, Bytes.toBytes("document_reference2trail"), Bytes.toBytes(document_reference2trail));
            }
            if (document_reference2JDN != null) {
                put.addColumn(family, Bytes.toBytes("document_reference2JDN"), Bytes.toBytes(document_reference2JDN));
            }
            if (document_name != null) {
                put.addColumn(family, Bytes.toBytes("document_name"), Bytes.toBytes(document_name));
            }
            if (defendants02defendant_preConvictions2preConviction_time != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_preConvictions2preConviction_time"), Bytes.toBytes(defendants02defendant_preConvictions2preConviction_time));
            }
            if (defendants02defendant_preConvictions2preConviction_chargeName != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_preConvictions2preConviction_chargeName"), Bytes.toBytes(defendants02defendant_preConvictions2preConviction_chargeName));
            }
            if (defendants02defendant_preConvictions2preConviction_release != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_preConvictions2preConviction_release"), Bytes.toBytes(defendants02defendant_preConvictions2preConviction_release));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_type2index0 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_type2index0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_type2index0));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_type2text0 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_type2text0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_type2text0));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_time2index0 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_time2index0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_time2index0));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_time2text0 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_time2text0"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_time2text0));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_time2index1 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_time2index1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_time2index1));
            }
            if (defendants02charges2charge_enforcements2charge_enforcement_time2text1 != null) {
                put.addColumn(family, Bytes.toBytes("defendants02charges2charge_enforcements2charge_enforcement_time2text1"), Bytes.toBytes(defendants02charges2charge_enforcements2charge_enforcement_time2text1));
            }

            if (defendants02defendant_base2NN != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2NN"), Bytes.toBytes(defendants02defendant_base2NN));
            }
            if (defendants02defendant_base2BD2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2BD2index"), Bytes.toBytes(defendants02defendant_base2BD2index));
            }
            if (defendants02defendant_base2BD2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2BD2text"), Bytes.toBytes(defendants02defendant_base2BD2text));
            }
            if (defendants02defendant_base2PS != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2PS"), Bytes.toBytes(defendants02defendant_base2PS));
            }
            if (defendants02defendant_base2defendant_nativePlace != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_nativePlace"), Bytes.toBytes(defendants02defendant_base2defendant_nativePlace));
            }
            if (defendants02defendant_base2E2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2E2index"), Bytes.toBytes(defendants02defendant_base2E2index));
            }
            if (defendants02defendant_base2E2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2E2text"), Bytes.toBytes(defendants02defendant_base2E2text));
            }
            if (defendants02defendant_base2defendant_capacity != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_capacity"), Bytes.toBytes(defendants02defendant_base2defendant_capacity));
            }
            if (defendants02defendant_base2defendant_occupation2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_occupation2index"), Bytes.toBytes(defendants02defendant_base2defendant_occupation2index));
            }
            if (defendants02defendant_base2defendant_occupation2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_occupation2text"), Bytes.toBytes(defendants02defendant_base2defendant_occupation2text));
            }

            if (defendants02defendant_base2S2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2S2index"), Bytes.toBytes(defendants02defendant_base2S2index));
            }
            if (defendants02defendant_base2S2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2S2text"), Bytes.toBytes(defendants02defendant_base2S2text));
            }
            if (defendants02defendant_base2defendant_identity != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_identity"), Bytes.toBytes(defendants02defendant_base2defendant_identity));
            }
            if (defendants02defendant_base2defendant_name2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_name2index"), Bytes.toBytes(defendants02defendant_base2defendant_name2index));
            }
            if (defendants02defendant_base2defendant_name2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2defendant_name2text"), Bytes.toBytes(defendants02defendant_base2defendant_name2text));
            }
            if (defendants02defendant_base2special_identity2identity_others != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_others"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_others));
            }
            if (defendants02defendant_base2special_identity2identity_star != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_star"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_star));
            }
            if (defendants02defendant_base2special_identity2identity_partygroupings != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_partygroupings"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_partygroupings));
            }
            if (defendants02defendant_base2special_identity2identity_intellectual != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_intellectual"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_intellectual));
            }
            if (defendants02defendant_base2special_identity2identity_cppcc_members != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_cppcc_members"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_cppcc_members));
            }
            if (defendants02defendant_base2special_identity2identity_sportstar != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_sportstar"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_sportstar));
            }
            if (defendants02defendant_base2special_identity2identity_officer != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_officer"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_officer));
            }
            if (defendants02defendant_base2special_identity2identity_npc_member != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_npc_member"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_npc_member));
            }
            if (defendants02defendant_base2special_identity2identity_foreigner != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_foreigner"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_foreigner));
            }
            if (defendants02defendant_base2special_identity2identity_taiwan != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_taiwan"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_taiwan));
            }
            if (defendants02defendant_base2special_identity2identity_leader != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_leader"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_leader));
            }
            if (defendants02defendant_base2special_identity2identity_hongkongmacau != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2special_identity2identity_hongkongmacau"), Bytes.toBytes(defendants02defendant_base2special_identity2identity_hongkongmacau));
            }
            if (defendants02defendant_base2PN2index != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2PN2index"), Bytes.toBytes(defendants02defendant_base2PN2index));
            }
            if (defendants02defendant_base2PN2text != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2PN2text"), Bytes.toBytes(defendants02defendant_base2PN2text));
            }
            if (defendants02defendant_base2BA != null) {
                put.addColumn(family, Bytes.toBytes("defendants02defendant_base2BA"), Bytes.toBytes(defendants02defendant_base2BA));
            }
            if (defendants02judgment2penalties2penalty_notimprison != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_notimprison"), Bytes.toBytes(defendants02judgment2penalties2penalty_notimprison));
            }
            if (defendants02judgment2penalties2penalty_principal != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_principal"), Bytes.toBytes(defendants02judgment2penalties2penalty_principal));
            }
            if (defendants02judgment2penalties2penalty_suspend != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_suspend"), Bytes.toBytes(defendants02judgment2penalties2penalty_suspend));
            }
            if (defendants02judgment2penalties2JA != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2JA"), Bytes.toBytes(defendants02judgment2penalties2JA));
            }
            if (defendants02judgment2penalties2penalty_duration != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_duration"), Bytes.toBytes(defendants02judgment2penalties2penalty_duration));
            }
            if (defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_money != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_money"), Bytes.toBytes(defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_money));
            }
            if (defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_content != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_content"), Bytes.toBytes(defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_content));
            }
            if (defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_type != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_type"), Bytes.toBytes(defendants02judgment2penalties2penalty_supplementaries2penalty_supplementary_type));
            }
            if (defendants02judgment2defendant_joint_crime != null) {
                put.addColumn(family, Bytes.toBytes("defendants02judgment2defendant_joint_crime"), Bytes.toBytes(defendants02judgment2defendant_joint_crime));
            }
            if (defendants02attorneys2attorney_type != null) {
                put.addColumn(family, Bytes.toBytes("defendants02attorneys2attorney_type"), Bytes.toBytes(defendants02attorneys2attorney_type));
            }
            if (defendants02attorneys2attorney_workplace != null) {
                put.addColumn(family, Bytes.toBytes("defendants02attorneys2attorney_workplace"), Bytes.toBytes(defendants02attorneys2attorney_workplace));
            }
            if (defendants02attorneys2attorney_relation != null) {
                put.addColumn(family, Bytes.toBytes("defendants02attorneys2attorney_relation"), Bytes.toBytes(defendants02attorneys2attorney_relation));
            }
            if (defendants02attorneys2attorney_name != null) {
                put.addColumn(family, Bytes.toBytes("defendants02attorneys2attorney_name"), Bytes.toBytes(defendants02attorneys2attorney_name));
            }
            if (defendants02attorneys2attorney_occupation != null) {
                put.addColumn(family, Bytes.toBytes("defendants02attorneys2attorney_occupation"), Bytes.toBytes(defendants02attorneys2attorney_occupation));
            }
            if (defendants02detain2inPrison != null) {
                put.addColumn(family, Bytes.toBytes("defendants02detain2inPrison"), Bytes.toBytes(defendants02detain2inPrison));
            }
            if (defendants02detain2inPrison_place != null) {
                put.addColumn(family, Bytes.toBytes("defendants02detain2inPrison_place"), Bytes.toBytes(defendants02detain2inPrison_place));
            }
            if (accusation2accusation_reference != null) {
                put.addColumn(family, Bytes.toBytes("accusation2accusation_reference"), Bytes.toBytes(accusation2accusation_reference));
            }
            if (accusation2accusation_procurator != null) {
                put.addColumn(family, Bytes.toBytes("accusation2accusation_procurator"), Bytes.toBytes(accusation2accusation_procurator));
            }
            if (accusation2prosecute_charge_name != null) {
                put.addColumn(family, Bytes.toBytes("accusation2prosecute_charge_name"), Bytes.toBytes(accusation2prosecute_charge_name));
            }
            if (accusation2accusation_time != null) {
                put.addColumn(family, Bytes.toBytes("accusation2accusation_time"), Bytes.toBytes(accusation2accusation_time));
            }
            if (accusation2accusation_type != null) {
                put.addColumn(family, Bytes.toBytes("accusation2accusation_type"), Bytes.toBytes(accusation2accusation_type));
            }
            if (judgment2laws2law_name != null) {
                put.addColumn(family, Bytes.toBytes("judgment2laws2law_name"), Bytes.toBytes(judgment2laws2law_name));
            }
            if (judgment2laws2article_names != null) {
                put.addColumn(family, Bytes.toBytes("judgment2laws2article_names"), Bytes.toBytes(judgment2laws2article_names));
            }
            if (judgment2judgement_date != null) {
                put.addColumn(family, Bytes.toBytes("judgment2judgement_date"), Bytes.toBytes(judgment2judgement_date));
            }
            if (judgment2notaffirm_reasons2fact_norelevance != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2fact_norelevance"), Bytes.toBytes(judgment2notaffirm_reasons2fact_norelevance));
            }
            if (judgment2notaffirm_reasons2procedure_illegal != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2procedure_illegal"), Bytes.toBytes(judgment2notaffirm_reasons2procedure_illegal));
            }
            if (judgment2notaffirm_reasons2fakecopy != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2fakecopy"), Bytes.toBytes(judgment2notaffirm_reasons2fakecopy));
            }
            if (judgment2notaffirm_reasons2other != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2other"), Bytes.toBytes(judgment2notaffirm_reasons2other));
            }
            if (judgment2notaffirm_reasons2evidence_old != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2evidence_old"), Bytes.toBytes(judgment2notaffirm_reasons2evidence_old));
            }
            if (judgment2notaffirm_reasons2form_illegal != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2form_illegal"), Bytes.toBytes(judgment2notaffirm_reasons2form_illegal));
            }
            if (judgment2notaffirm_reasons2fakeevidence != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2fakeevidence"), Bytes.toBytes(judgment2notaffirm_reasons2fakeevidence));
            }
            if (judgment2notaffirm_reasons2doubt != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2doubt"), Bytes.toBytes(judgment2notaffirm_reasons2doubt));
            }
            if (judgment2notaffirm_reasons2evidence_unverify != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2evidence_unverify"), Bytes.toBytes(judgment2notaffirm_reasons2evidence_unverify));
            }
            if (judgment2notaffirm_reasons2means_illegal != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2means_illegal"), Bytes.toBytes(judgment2notaffirm_reasons2means_illegal));
            }
            if (judgment2notaffirm_reasons2evidence_flaw != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2evidence_flaw"), Bytes.toBytes(judgment2notaffirm_reasons2evidence_flaw));
            }
            if (judgment2notaffirm_reasons2evidence_suspected != null) {
                put.addColumn(family, Bytes.toBytes("judgment2notaffirm_reasons2evidence_suspected"), Bytes.toBytes(judgment2notaffirm_reasons2evidence_suspected));
            }
            if (document_type != null) {
                put.addColumn(family, Bytes.toBytes("document_type"), Bytes.toBytes(document_type));
            }
            if (prosecutors != null) {
                put.addColumn(family, Bytes.toBytes("prosecutors"), Bytes.toBytes(prosecutors));
            }


            putsList.add(put);

            HBaseUtil.putRows("tq", putsList);
            System.out.println("已经有" + putsList.size() + "行");
        }
        HBaseUtil.putRows("tq", putsList);
        connection.close();

    }
}
