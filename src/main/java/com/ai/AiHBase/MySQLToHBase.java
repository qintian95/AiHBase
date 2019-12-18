package com.ai.AiHBase;

import cn.huacloud.platform.sdk.client.AIOpenClient;
import com.ai.util.HBaseUtil;
import com.ai.util.MySQLUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.alibaba.fastjson.serializer.SerializerFeature;
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
 * @date 2019/12/10 14-24
 * <p>
 * 抽取每天的增量数据调用ai接口解析后存入hbase
 */
public class MySQLToHBase {
    public static void main(String[] args) throws SQLException, IOException {
        Connection connection = MySQLUtil.getConnection();

        Statement statement = connection.createStatement();

        //查找今天的增量数据
//        ResultSet resultSet = statement.executeQuery("select a.doc_id,a.content,a.create_time from judicial_document a  where a.trial_procedures='0201' and substr(a.create_time,1,10)=curdate() ");

        //全量数据 2019-12-12 00-09-12.0_f892bccf-1881-4d80-96ec-aa5d00d6b147
        ResultSet resultSet = statement.executeQuery("select a.doc_id,a.content,a.create_time from judicial_document a  where a.doc_id='00003f8b-7260-4c17-a1e8-dc6e38222f2a'");
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

            System.out.println(result);
            System.out.println("================================================================");
            //把null值，[]数组都替换成 无
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue).replaceAll("[\\[]]", "\"无\"").replaceAll("null", "\"无\""));

            System.out.println(JSON.toJSONString(result,SerializerFeature.WriteMapNullValue));

            Object[] jsonObjectArr = jsonObject.keySet().toArray();
            for (int a = 0; a < jsonObjectArr.length; a++) {

                String k = jsonObjectArr[a].toString();

                if ("document_reference".equalsIgnoreCase(k)) {
                    //document_reference
                    JSONObject document_referenceObj = jsonObject.getJSONObject("document_reference");
                    Object[] document_referenceK = document_referenceObj.keySet().toArray();
                    for (int i = 0; i < document_referenceK.length; i++) {
                        put.addColumn(family, Bytes.toBytes("document_reference-" + document_referenceK[i]), Bytes.toBytes(document_referenceObj.getString(document_referenceK[i].toString())));
                    }
                } else if ("defendants".equalsIgnoreCase(k)) {
                    //defendants
                    JSONArray defendantsArr = jsonObject.getJSONArray("defendants");
                    int defendantsLength = defendantsArr.toArray().length;
                    for (int i = 0; i < defendantsLength; i++) {
                        JSONObject defendantsObj = defendantsArr.getJSONObject(i);
                        JSONObject defendant_base = defendantsObj.getJSONObject("defendant_base");
                        Object[] defendant_baseK = defendant_base.keySet().toArray();
                        for (int j = 0; j < defendant_baseK.length; j++) {
                            if ("special_identity".equalsIgnoreCase(defendant_baseK[j].toString())) {
                                JSONObject special_identityObj = defendant_base.getJSONObject("special_identity");
                                Object[] special_identityArr = special_identityObj.keySet().toArray();
                                for (int k1 = 0; k1 < special_identityArr.length; k1++) {
                                    put.addColumn(family, Bytes.toBytes("defendants" + i + "-defendant_base-special_identity-" + special_identityArr[k1]), Bytes.toBytes(special_identityObj.getString(special_identityArr[k1].toString())));
                                }
                            }else {
                                put.addColumn(family, Bytes.toBytes("defendants" + i + "-defendant_base-" + defendant_baseK[j]), Bytes.toBytes(defendant_base.getString(defendant_baseK[j].toString())));
                            }
                        }

                        JSONArray defendant_preConvictionsArr = defendantsObj.getJSONArray("defendant_preConvictions");
                        int defendant_preConvictionsArrLen = defendant_preConvictionsArr.toArray().length;
                        for (int j = 0; j < defendant_preConvictionsArrLen; j++) {
                            JSONObject defendant_preConvictionsObj = defendant_preConvictionsArr.getJSONObject(j);
                            Object[] defendant_preConvictionsK = defendant_preConvictionsObj.keySet().toArray();
                            for (int k1 = 0; k1 < defendant_preConvictionsK.length; k1++) {
                                put.addColumn(family, Bytes.toBytes("defendants" + i + "-defendant_preConvictions" + j + "-" + defendant_preConvictionsK[k1]), Bytes.toBytes(defendant_preConvictionsObj.getString(defendant_preConvictionsK[k1].toString())));
                            }
                        }

                        JSONArray chargesArr = defendantsObj.getJSONArray("charges");
                        int chargesArrLen = chargesArr.toArray().length;
                        //charges数组
                        for (int j = 0; j < chargesArrLen; j++) {
                            //数组中第j个json
                            JSONObject chargesObj = chargesArr.getJSONObject(j);
                            //第j个json的key组成的数组
                            Object[] chargesK = chargesObj.keySet().toArray();
                            //循环遍历第j个json的key组成的数组
                            for (int k1 = 0; k1 < chargesK.length; k1++) {

                                if ("charge_enforcements".equalsIgnoreCase(chargesK[k1].toString())) {
                                    //charge_enforcements数组
                                    JSONArray charge_enforcementsArr = chargesObj.getJSONArray("charge_enforcements");
                                    int charge_enforcementsArrLen = charge_enforcementsArr.size();
                                    //遍历charge_enforcements数组
                                    for (int l = 0; l < charge_enforcementsArrLen; l++) {
                                        //charge_enforcements数组中第l个json
                                        JSONObject charge_enforcementsObj = charge_enforcementsArr.getJSONObject(l);
                                        //charge_enforcements数组中第l个json的keyt组成的数组
                                        Object[] charge_enforcementsK = charge_enforcementsObj.keySet().toArray();
                                        //遍历charge_enforcements数组中第l个json的keyt组成的数组
                                        for (int m = 0; m < charge_enforcementsK.length; m++) {

                                            put.addColumn(family, Bytes.toBytes("defendants" + i + "-charges" + j + "-charge_enforcements" + l + "-" + charge_enforcementsK[m]), Bytes.toBytes(charge_enforcementsObj.getString(charge_enforcementsK[m].toString())));
                                        }
                                    }
                                } else {
                                    put.addColumn(family, Bytes.toBytes("defendants" + i + "-charges" + j + "-" + chargesK[k1]), Bytes.toBytes(chargesObj.getString(chargesK[k1].toString())));
                                }
                            }
                        }

                        JSONObject detainObj = defendantsObj.getJSONObject("detain");
                        Object[] detainK = detainObj.keySet().toArray();
                        for (int j = 0; j < detainK.length; j++) {
                            put.addColumn(family, Bytes.toBytes("defendants" + i + "-detain-" + detainK[j]), Bytes.toBytes(detainObj.getString(detainK[j].toString())));
                        }

                        //attorneys
                        JSONArray attorneysArr = defendantsObj.getJSONArray("attorneys");
                        for (int j = 0; j < attorneysArr.size(); j++) {

                            JSONObject attorneysObj = attorneysArr.getJSONObject(j);
                            Object[] attorneysK = attorneysObj.keySet().toArray();
                            for (int k1 = 0; k1 < attorneysK.length; k1++) {
                                put.addColumn(family, Bytes.toBytes("defendants" + i + "-attorneys" + j + "-" + attorneysK[k1]), Bytes.toBytes(attorneysObj.getString(attorneysK[k1].toString())));
                            }
                        }

                        //judgment
                        if (defendantsObj.containsKey("judgment")) {
                            JSONObject judgmentObj = defendantsObj.getJSONObject("judgment");
                            Object[] judgmentK = judgmentObj.keySet().toArray();
                            for (int q = 0; q < judgmentK.length; q++) {
                                if ("penalties".equalsIgnoreCase(judgmentK[q].toString())) {
                                    JSONArray penaltiesArr = judgmentObj.getJSONArray("penalties");
                                    for (int j = 0; j < penaltiesArr.size(); j++) {
                                        JSONObject penaltiesObj = penaltiesArr.getJSONObject(j);
                                        Object[] penaltiesK = penaltiesObj.keySet().toArray();
                                        for (int k1 = 0; k1 < penaltiesK.length; k1++) {
                                            if ("penalty_supplementaries".equalsIgnoreCase(penaltiesK[k1].toString())) {
                                                JSONArray penalty_supplementariesArr = penaltiesObj.getJSONArray("penalty_supplementaries");
                                                for (int l = 0; l < penalty_supplementariesArr.size(); l++) {
                                                    JSONObject penalty_supplementariesObj = penalty_supplementariesArr.getJSONObject(l);
                                                    Object[] penalty_supplementariesK = penalty_supplementariesObj.keySet().toArray();
                                                    for (int m = 0; m < penalty_supplementariesK.length; m++) {
                                                        put.addColumn(family, Bytes.toBytes("defendants" + i + "-judgment-penalties" + j + "-penalty_supplementaries" + l + "-" + penalty_supplementariesK[m]), Bytes.toBytes(penalty_supplementariesObj.getString(penalty_supplementariesK[m].toString())));
                                                    }
                                                }
                                            } else {

                                                put.addColumn(family, Bytes.toBytes("defendants" + i + "-judgment-penalties-" + j + penaltiesK[k1]), Bytes.toBytes(penaltiesObj.getString(penaltiesK[k1].toString())));
                                            }
                                        }
                                    }
                                } else {
                                    put.addColumn(family, Bytes.toBytes("defendants" + i + "-" + judgmentK[q]), Bytes.toBytes(judgmentObj.getString(judgmentK[q].toString())));

                                }
                            }
                        }
                    }
                } else if ("accusation".equalsIgnoreCase(k)) {
                    //accusation
                    JSONObject accusationObj = jsonObject.getJSONObject("accusation");
                    Object[] accusationK = accusationObj.keySet().toArray();
                    for (int i = 0; i < accusationK.length; i++) {
                        put.addColumn(family, Bytes.toBytes("accusation-" + accusationK[i]), Bytes.toBytes(accusationObj.getString(accusationK[i].toString())));
                    }
                } else if ("hear_info".equalsIgnoreCase(k)) {
                    //hear_info
                    JSONObject hear_infoObj = jsonObject.getJSONObject("hear_info");
                    Object[] hear_infoK = hear_infoObj.keySet().toArray();
                    for (int i = 0; i < hear_infoK.length; i++) {
                        if ("hear_prosecutors".equalsIgnoreCase(hear_infoK[i].toString())) {
                            if (hear_infoObj.getString("hear_prosecutors").contains("[")) {
                                JSONArray hear_prosecutorsArr = hear_infoObj.getJSONArray("hear_prosecutors");
                                for (int j = 0; j < hear_prosecutorsArr.size(); j++) {
                                    if (hear_prosecutorsArr.get(j).toString().contains("-")) {
                                        JSONObject hear_prosecutorsObj = hear_prosecutorsArr.getJSONObject(j);
                                        Object[] hear_prosecutorsK = hear_prosecutorsObj.keySet().toArray();
                                        for (int k1 = 0; k1 < hear_prosecutorsK.length; k1++) {
                                            put.addColumn(family, Bytes.toBytes("hear_info-" + "hear_prosecutors" + j + "-" + hear_prosecutorsK[k1]), Bytes.toBytes(hear_prosecutorsObj.getString(hear_prosecutorsK[k1].toString())));
                                        }
                                    }
                                }
                            }
                        } else {
                            put.addColumn(family, Bytes.toBytes("hear_info-" + hear_infoK[i]), Bytes.toBytes(hear_infoObj.getString(hear_infoK[i].toString())));
                        }
                    }
                } else if ("evidences".equalsIgnoreCase(k)) {
                    //evidences
                    JSONObject evidencesObj = jsonObject.getJSONObject("evidences");
                    Object[] evidencesK = evidencesObj.keySet().toArray();
                    for (int i = 0; i < evidencesK.length; i++) {
                        put.addColumn(family, Bytes.toBytes("evidences-" + evidencesK[i]), Bytes.toBytes(evidencesObj.getString(evidencesK[i].toString())));
                    }
                } else if ("judgment".equalsIgnoreCase(k)) {
                    //judgment
                    JSONObject judgmentObj = jsonObject.getJSONObject("judgment");
                    Object[] judgmentK = judgmentObj.keySet().toArray();
                    for (int i = 0; i < judgmentK.length; i++) {
                        String key = judgmentK[i].toString();
                        if ("notaffirm_reasons".equalsIgnoreCase(key)) {
                            JSONObject notaffirm_reasonsObj = judgmentObj.getJSONObject("notaffirm_reasons");
                            Object[] notaffirm_reasonsK = notaffirm_reasonsObj.keySet().toArray();
                            for (int j = 0; j < notaffirm_reasonsK.length; j++) {
                                put.addColumn(family, Bytes.toBytes("judgment-notaffirm_reasons-" + notaffirm_reasonsK[j]), Bytes.toBytes(notaffirm_reasonsObj.getString(notaffirm_reasonsK[j].toString())));
                            }
                        } else if ("laws".equalsIgnoreCase(key)) {
                            JSONArray lawsArr = judgmentObj.getJSONArray("laws");
                            for (int j = 0; j < lawsArr.size(); j++) {
                                JSONObject lawsObj = lawsArr.getJSONObject(j);
                                Object[] lawsK = lawsObj.keySet().toArray();
                                for (int k1 = 0; k1 < lawsK.length; k1++) {
                                    put.addColumn(family, Bytes.toBytes("judgment-laws" + j + "-" + lawsK[k1]), Bytes.toBytes(lawsObj.getString(lawsK[k1].toString())));
                                }
                            }
                        } else if ("appeal".equalsIgnoreCase(key)) {
                            JSONObject appealObj = judgmentObj.getJSONObject("appeal");
                            Object[] appealK = appealObj.keySet().toArray();
                            for (int j = 0; j < appealK.length; j++) {
                                put.addColumn(family, Bytes.toBytes("judgment-appeal-" + appealK[j]), Bytes.toBytes(appealObj.getString(appealK[j].toString())));
                            }
                        } else if ("staff".equalsIgnoreCase(key)) {
                            JSONObject staffObj = judgmentObj.getJSONObject("staff");
                            Object[] staffK = staffObj.keySet().toArray();
                            for (int j = 0; j < staffK.length; j++) {
                                put.addColumn(family, Bytes.toBytes("judgment-staff-" + staffK[j]), Bytes.toBytes(staffObj.getString(staffK[j].toString())));
                            }
                        } else {
                            put.addColumn(family, Bytes.toBytes("judgment-" + key), Bytes.toBytes(judgmentObj.getString(key)));
                        }
                    }
                } else {
                    //document_name,document_producer,document_type,document_category,prosecutors
                    put.addColumn(family, Bytes.toBytes(jsonObjectArr[a].toString()), Bytes.toBytes(jsonObject.getString(jsonObjectArr[a].toString())));
                }
            }


            putsList.add(put);

            HBaseUtil.putRows("tq", putsList);
            System.out.println("已经有" + putsList.size() + "行");
        }
        HBaseUtil.putRows("tq", putsList);
        connection.close();

    }
}
